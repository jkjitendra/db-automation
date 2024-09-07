package org.demo.service.PropertyImpl;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.demo.exception.AutomationException;
import org.demo.service.ConnectionUtilService;
import org.demo.service.ReaderService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("BitbucketReaderService")
@NoArgsConstructor
@AllArgsConstructor
public class BitbucketReaderService implements ReaderService {

    @Autowired
    private ConnectionUtilService connectionUtilService;
    @Value("${execution.environment:}")
    private String environment;
    @Value("${bitbucket.project:}")
    private String projectName;
    @Value("${bitbucket.repository:}")
    private String repoName;
    @Value("${bitbucket.branch:}")
    private String branchName;
    @Value("${config.file.name:}")
    private String fileName;

    public StringBuilder getFileContent(String encodedAuth){

        if(StringUtils.isEmpty(fileName)){
            log.info("** fileName is Empty");
            throw new AutomationException();
        }
        String url = "https://bitbucket.com:9090/rest/api/projects/"+projectName + "/repos" + repoName +
                "/browse" + environment + "/" +fileName + "?at=" + branchName;
        String repositoryLines = connectionUtilService.getContentFromConnection(url, encodedAuth);
        JSONObject jsonObject = new JSONObject(repositoryLines);
        JSONArray jsonArray = jsonObject.getJSONArray("lines");
        StringBuilder sb = new StringBuilder();
        for(int index = 0; index< jsonArray.length(); index++){
            sb.append(jsonArray.getJSONObject(index).getString("text"));
        }
        return sb;
    }
}
