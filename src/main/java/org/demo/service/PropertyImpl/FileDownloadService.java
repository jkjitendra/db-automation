package org.demo.service.PropertyImpl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class FileDownloadService {

    private final Logger logger = LoggerFactory.getLogger(FileDownloadService.class);
    @Value("${local.file.path:#{systemProperties['user.home']}/localfile.zip}")
    private String outputFileName;

    @Value("${local.file.property.path:#{systemProperties['user.home']}/localPath}")
    private String outputPropertyFilePath;

    public StringBuilder getFileFromJfrog(HttpsURLConnection url, String fileName){

        Path zipPath = Paths.get(outputFileName);
        StringBuilder finalReqPropertyPath = new StringBuilder(outputPropertyFilePath + "/test.txt");
//        File finalReqProperty = new File(finalReqPropertyPath);
        try(InputStream in = url.getInputStream();
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(outputFileName)){
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            logger.info("download zip file to Path :: {}, is zip file download in localPath ? {}",zipPath, Files.exists(zipPath));
//            File finalReqProperty = unzipFileAndRequiredPropertyFile(fileName);
//            logger.info("final property file size ::{}, is available ? {}", finalReqProperty.getTotalSpace(), finalReqProperty.exists());

        }catch (Exception e){
            logger.error("Error while downloading file :: {}", e.getMessage());
        }
        return finalReqPropertyPath;
    }

    private File unzipFileAndRequiredPropertyFile(String fileName) throws IOException {
        File destDir = new File(outputPropertyFilePath);
        File newFile = new File("test.txt");
        byte[] buffer = new byte[1024];
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(outputFileName))){
            logger.info("Going to read zip file to get property file :: {}", fileName);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null){
                newFile = newFile(destDir, zipEntry);
                if(zipEntry.isDirectory()){
                    logger.info("found a directory in zip hence skipping directory");
                    if(!newFile.isDirectory() && !newFile.mkdirs()){
                        throw new IOException("Failed to create directory "+ newFile);
                    }
                }else {
                    File parent = newFile.getParentFile();
                    if(!parent.isDirectory() && !parent.mkdirs()){
                        throw new IOException("Failed to create directory"+ parent);
                    }
                    if(zipEntry.getName().contains(fileName)){
                        logger.info("found the req property file in zip {} with given input {} hence writing this file to output property file",
                                zipEntry.getName(), fileName);
                        writeToFile(buffer, zis, newFile);
                        return newFile;
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }catch (Exception e){
            logger.error("Error while downloading file :: {}", e);
        }
        return newFile;
    }

    private void writeToFile(byte[] buffer, ZipInputStream zis, File newFile){
        try(FileOutputStream fos = new FileOutputStream(newFile)){
            int len;
            while((len = zis.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
        }catch (Exception e){
            logger.error("error writing to download :: {}", e);
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException{
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if(!destFilePath.startsWith(destDirPath + File.separator)){
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

}
