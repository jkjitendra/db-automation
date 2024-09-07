package org.demo.constants;

public enum Constants {
    SEPARATOR("====================================="),
    DEPLOYMENT("deployment"),
    UPDATE("update"),
    DELETE("delete"),
    INSERT("insert"),
    TABLE_NAME("tablename"),
    CONDITION("condition"),
    COLUMNS("columns"),
    VALUES("values"),
    AND("AND"),
    COMMA(","),
    EQUAL_SIGN("="),
    REPO_FILE("repoFile"),
    PARSE_ERROR_MESSAGE("\n Error occurred while parsing the query\n"),
    NEWLINE_CHAR("\n"),
    GENERIC_SELECT_QUERY("SELECT %s FROM %s WHERE %s");

    private final String fieldName;
    Constants(String fieldName){
        this.fieldName=fieldName;
    }

    public String getFieldName(){   return this.fieldName;   }
}
