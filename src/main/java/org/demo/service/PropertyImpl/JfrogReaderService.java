package org.demo.service.PropertyImpl;

import lombok.extern.slf4j.Slf4j;
import org.demo.service.ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

@Slf4j
@Service("JfrogReaderService")
public class JfrogReaderService implements ReaderService {

    private final Logger logger = LoggerFactory.getLogger(JfrogReaderService.class);
    @Autowired
    private FileDownloadService fileDownloadService;
    @Value("${jfrog.group.id:}")
    private String jfrogGroupName;
    @Value("${jfrog.artifact.id:}")
    private String jfrogArtifactId;
    @Value("${config.file.name:}")
    private String fileName;
    @Value("${jfrog.version:}")
    private String jfrogVersion;
    @Value("${jfrog.zip.name:}")
    private String zipFileName;
    @Value("${jfrog.base.url:}")
    private String jfrogBaseUrl;
    @Value("${jfrog.username:}")
    private String username;
    @Value("${jfrog.password:}")
    private String password;


    @Override
    public StringBuilder getFileContent(String encodedAuth){

        StringBuilder fileContent = new StringBuilder();
        Scanner fileScanner = null;
        try{
//            StringBuilder urlBuilder = new StringBuilder(jfrogBaseUrl).append("/").append(jfrogArtifactId)
//                    .append("/").append(jfrogVersion).append("/").append(zipFileName);
            String urlString = jfrogBaseUrl + "/" + jfrogArtifactId + "/" + jfrogVersion + "/" + zipFileName;
            logger.info("jfrog download url {} and fileName ::{}", urlString, this.fileName);
            String jfrogEncodedAuth = Base64.getEncoder().encodeToString((username+":"+password).getBytes());
            HttpsURLConnection connection = (HttpsURLConnection) new URL(urlString).openConnection();
            connection.setRequestProperty("Authorization", "Basic" + jfrogEncodedAuth);

            StringBuilder propertiesFilePath = fileDownloadService.getFileFromJfrog(connection, this.fileName);
            File file = new File(propertiesFilePath.toString());
            if(file != null && file.exists()){
                logger.info("file got from server {}", file.getAbsolutePath());
                fileScanner = new Scanner(file);
                while(fileScanner.hasNext()){
                    fileContent.append(fileScanner.nextLine().trim());
                    fileContent.append("\n");
                }
            }else{
                logger.info("given file not found in path");
            }
        }catch(Exception e){
            logger.error("Error while processing file:: {}", e.getMessage());
        } finally {
            if(fileScanner != null){
                fileScanner.close();
            }
        }
        return fileContent;
    }

    public StringBuilder downloadFromJfrog(String encodedAuth){
//        StringBuilder urlBuilder = new StringBuilder(jfrogBaseUrl).append("/").append(jfrogArtifactId)
//                .append("/").append(jfrogVersion).append("/").append(zipFileName);
        String urlString = jfrogBaseUrl + "/" + jfrogArtifactId +
                "/" + jfrogVersion + "/" + zipFileName;
        String jfrogEncodedAuth = Base64.getEncoder().encodeToString((username+":"+password).getBytes());
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new URL(urlString).openConnection();
            connection.setRequestProperty("Authorization", "Basic " + jfrogEncodedAuth);
        } catch (Exception e){
            logger.error("Error while processing file:: {}", e.getMessage());
        }

        return fileDownloadService.getFileFromJfrog(connection, this.fileName);
    }

}
