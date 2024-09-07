package org.demo.service.PropertyImpl;


import lombok.extern.slf4j.Slf4j;
import org.demo.exception.AutomationException;
import org.demo.service.ConnectionUtilService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;

@Slf4j
@Service
public class ConnectionUtilServiceImpl implements ConnectionUtilService {
    private final Logger logger = LoggerFactory.getLogger(ConnectionUtilServiceImpl.class);
    @Override
    public String getContentFromConnection(String url, String encodedAuth){

        StringBuilder repositoryLines = new StringBuilder();
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Authorization", "Basic" + encodedAuth);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = reader.readLine()) != null){
                repositoryLines.append(line);
            }
            connection.disconnect();
        }catch (Exception e){
            logger.info("Error occurred in the process {}", e.getMessage(), e);
            throw new AutomationException();
        }
        return repositoryLines.toString();
    }
}
