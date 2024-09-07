package org.demo.service;

import org.demo.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class SelectPropertyService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void printResultFromSelect(String selectQuery){
        List<Map<String, Object>> selectedRows = jdbcTemplate.queryForList(selectQuery);
        StringBuilder queryResult=new StringBuilder();
        for(Map<String, Object> row: selectedRows){
            queryResult.append(Constants.NEWLINE_CHAR.getFieldName());
            queryResult.append(row);
            queryResult.append(Constants.NEWLINE_CHAR.getFieldName());
        }
        System.out.println(queryResult);
    }
}
