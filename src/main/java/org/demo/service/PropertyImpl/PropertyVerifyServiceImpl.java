package org.demo.service.PropertyImpl;

import lombok.extern.slf4j.Slf4j;
import org.demo.constants.Constants;
import org.demo.exception.AutomationException;
import org.demo.service.PropertyVerifyService;
import org.demo.service.SelectPropertyService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@ConditionalOnExpression("${query.verify:false}")
public class PropertyVerifyServiceImpl implements PropertyVerifyService {

    private Logger logger = LoggerFactory.getLogger(PropertyVerifyServiceImpl.class);
    @Autowired
    private SelectPropertyService selectPropertyService;

    @Override
    public void verifyCurrentDBData(final List<String> properties, final String beforeAfter) throws InterruptedException{

        logger.info(Constants.SEPARATOR.getFieldName());
        logger.info("========= Table Data " + beforeAfter + " Query Execution =========== ");
        logger.info(Constants.SEPARATOR.getFieldName());
        Thread.sleep(100);
        for(String query: properties){
            verify(query);
        }

    }

    private void verify(final String query){

        String queryType = "select";
        if(query.toLowerCase().trim().startsWith(Constants.UPDATE.getFieldName())){
            queryType = Constants.UPDATE.getFieldName();
        }else if(query.toLowerCase().trim().startsWith(Constants.INSERT.getFieldName())){
            queryType = Constants.INSERT.getFieldName();
        } else if(query.toLowerCase().trim().startsWith(Constants.DELETE.getFieldName())){
            queryType = Constants.DELETE.getFieldName();
        }

        switch (queryType){
            case "update": updateQueryProcessing(query); break;
            case "insert": insertQueryProcessing(query); break;
            case "delete": deleteQueryProcessing(query); break;
            default: log.info(Constants.PARSE_ERROR_MESSAGE.getFieldName());
        }
    }

    private void updateQueryProcessing(String query){

        Pattern updatePattern = Pattern.compile("update (?<tablename>[a-zA-Z_ ]+) set (?<setters>[\\w\\W\\S\\s]*) where (?<condition>[\\w\\W\\s\\S]+)[;]?", Pattern.CASE_INSENSITIVE);
        Matcher updateMatcher = updatePattern.matcher(query.toLowerCase().trim());
        if (updateMatcher.matches()){
            String tableName = updateMatcher.group(Constants.TABLE_NAME.getFieldName());
            String condition = updateMatcher.group(Constants.CONDITION.getFieldName());
            logTableData(tableName, condition);
        }
        else{
            log.info(Constants.PARSE_ERROR_MESSAGE.getFieldName());
        }
    }

    private void insertQueryProcessing(String query){

        Pattern insertPattern = Pattern.compile("insert into (?<tablename>[a-zA-Z0-9_]+)\\s*\\((?<columns>[\\w\\W\\s\\S]*)\\)\\s*values\\s*\\((?<values>[\\w\\W\\s\\S]*)\\)[;]", Pattern.CASE_INSENSITIVE);
        Matcher insertMatcher = insertPattern.matcher(query.toLowerCase().trim());
        if (insertMatcher.matches()){
            String tableName = insertMatcher.group(Constants.TABLE_NAME.getFieldName());
            String columns = insertMatcher.group(Constants.COLUMNS.getFieldName());
            String values = insertMatcher.group(Constants.VALUES.getFieldName());
            String condition = getCondition(columns, values);
            logTableData(tableName, condition);
        }
        else{
            log.info(Constants.PARSE_ERROR_MESSAGE.getFieldName());
        }
    }

    private void deleteQueryProcessing(String query){

        Pattern deletePattern = Pattern.compile("delete from (?<tablename>[a-zA-Z_ ]+) where (?<condition>[\\w\\W\\s\\S]+)[;]?", Pattern.CASE_INSENSITIVE);
        Matcher deleteMatcher = deletePattern.matcher(query.toLowerCase().trim());
        if (deleteMatcher.matches()){
            String tableName = deleteMatcher.group(Constants.TABLE_NAME.getFieldName());
            String condition = deleteMatcher.group(Constants.CONDITION.getFieldName());
            logTableData(tableName, condition);
        }
        else{
            log.info(Constants.PARSE_ERROR_MESSAGE.getFieldName());
        }
    }

    private void logTableData(Object tableName, Object condition){
        String selectQuery = String.format(Constants.GENERIC_SELECT_QUERY.getFieldName(), "*", tableName, condition);
        selectPropertyService.printResultFromSelect(selectQuery);
    }

    private String getCondition(String columns, String values){
        String[] columnNames = columns.trim().split(Constants.COMMA.getFieldName());
        String[] columnValues = values.trim().split(",(?=(?:[^']*'[^']*')*[^']*$)");
        if(columnNames.length != columnValues.length){
            throw new AutomationException("0110", String.format("Invalid query, Number of Columns %d, number of Values %d", columnNames.length, columnValues.length));
        }
        StringBuilder condition = new StringBuilder();
        for(int i=0; i< columnNames.length; i++){
            if(!columnValues[i].contains("now()")){
                if(i>0){
                    condition.append(Constants.AND.getFieldName());
                }
                condition.append(columnNames[i].trim());
                condition.append(Constants.EQUAL_SIGN.getFieldName());
                condition.append(columnValues[i].trim());
            }
        }
        return condition.toString();
    }

}
