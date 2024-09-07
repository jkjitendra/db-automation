package org.demo.service.PropertyImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.demo.constants.Constants;
import org.demo.dao.PropertiesConfigRepository;
import org.demo.domain.PropertiesConfig;
import org.demo.service.ProcessService;
import org.demo.service.PropertyVerifyService;
import org.demo.service.SelectPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class ProcessServiceImpl implements ProcessService {

    private final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);
    @Autowired
    private BitbucketReaderService bitbucketReaderService;
    @Autowired
    private JfrogReaderService jfrogReaderService;
    @Autowired
    private PropertyVerifyService propertyVerifyService;
    @Autowired
    private SelectPropertyService selectPropertyService;
    @Autowired
    private PropertiesConfigRepository propertiesConfigRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${login.user.name:}")
    private String bitbucketUsername;
    @Value("${login.user.password:}")
    private String bitbucketPassword;
    @Value("${spring.datasource.url:}")
    private String databaseUrl;
    @Value("${spring.datasource.username:}")
    private String databaseUsername;
    @Value("${spring.datasource.password:}")
    private String databasePassword;
    @Value("${application.mode:}")
    private String appMode;
    private Connection connection;
    @Value("${property.delimiter:}")
    private String propertyDelimiter;
    @Value("${property.source.system:jfrog}")
    private String getFileFrom;

    @Override
    @PostConstruct
    public void startDBAutomationProcess() {

        if(StringUtils.isNotBlank(appMode) && appMode.equalsIgnoreCase(Constants.DEPLOYMENT.getFieldName())){
            return;
        }
        String encodedAuth = Base64.getEncoder().encodeToString((bitbucketUsername + ":" + bitbucketPassword).getBytes());

        String zipFilePath = getZipFilePath(encodedAuth);
        try{
            unzipFile(zipFilePath);
            processFiles();
        }catch (IOException e){
            logger.error("Error while processing file: {}", e.getMessage());
        }


//        List<String> queries = getQueries(encodedAuth);
//        propertyVerifyService.verifyCurrentDBData(queries, "Before");
//        executeScript(queries);
//        propertyVerifyService.verifyCurrentDBData(queries, "After");
//        logger.info("** SQL script executed successfully..**");
    }

    private String getZipFilePath(String encodedAuth){
        StringBuilder builder = new StringBuilder();
        if(StringUtils.equalsAnyIgnoreCase("jfrog", getFileFrom)){
            builder = jfrogReaderService.downloadFromJfrog(encodedAuth);
        } else if (StringUtils.equalsAnyIgnoreCase("bitbucket", getFileFrom)) {
            builder = bitbucketReaderService.getFileContent(encodedAuth);
        }
        return builder.toString();
    }

    private void unzipFile(String zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String filePath = "unzipped/" + zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    extractFile(zis, filePath);
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void processFiles() throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("unzipped"))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(path.toFile()));

                    String json = objectMapper.writeValueAsString(properties);
                    String appProfile = path.getFileName().toString().replace(".properties", "");

                    PropertiesConfig config = new PropertiesConfig();
                    config.setAppProfile(appProfile);
                    config.setProperties(json);
                    propertiesConfigRepository.save(config);
                }
            }
        }
    }

    private void executeScript(final List<String> queries) throws InterruptedException, SQLException{
        log.info(Constants.SEPARATOR.getFieldName());
        log.info("=========== Executing Queries==============");
        log.info(Constants.SEPARATOR.getFieldName());
        Thread.sleep(100);
        executeSQLScriptTransactional(queries.toArray(new String[]{}));
        log.info("**sleeping for 5 seconds to digest the data**");
        Thread.sleep(5000);
    }

    private List<String> getQueries(final String encodedAuth) throws InterruptedException{

        StringBuilder fileContent = new StringBuilder();
        String[] queriesArray = fileContent.toString().split(propertyDelimiter);
        List<String> queries = new ArrayList<>(Arrays.asList(queriesArray));
        log.info("Queries read from the properties file are: {} and count :: {}", queries, queries.size());
        Thread.sleep(500);
        return queries;
    }

    private StringBuilder getFileContent(String encodedAuth){
        StringBuilder builder = new StringBuilder();
        if(StringUtils.equalsAnyIgnoreCase("jfrog", getFileFrom)){
            builder = jfrogReaderService.getFileContent(encodedAuth);
        } else if (StringUtils.equalsAnyIgnoreCase("bitbucket", getFileFrom)) {
            builder = bitbucketReaderService.getFileContent(encodedAuth);
        }
        return builder;
    }

    private void executeSQLScriptTransactional(String[] queries) throws SQLException, InterruptedException{

        try{
            connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            executeQueries(queries, statement);
            connection.commit();
        }catch (SQLException | InterruptedException e){
            log.error("Query execution failed");
            connection.rollback();
            e.printStackTrace();
            log.error(e.getMessage());
            throw e;
        }finally {
            connection.close();
        }
    }

    private void executeQueries(String[] queries, Statement statement) throws InterruptedException, SQLException{

        for(String query: queries){
            if(StringUtils.isEmpty(query.trim())){
                continue;
            }
            log.info("Executing : {}", query);
            if(query.trim().split(" ")[0].equalsIgnoreCase("SELECT")){
                selectPropertyService.printResultFromSelect(query);
            }else{
                statement.execute(query);
            }
            log.info("Query Execution successful");
        }
    }

    public void updateAppMode(String appMode){
        this.appMode = appMode;
    }


}
