package org.demo.exception;

public class AutomationException extends RuntimeException{

    private final String code;
    private final String message;

    public AutomationException(String code, String message){
        this.code=code;
        this.message=message;
    }

    public AutomationException(){
        this.code="E1111";
        this.message="Exception occurred while automation execution";
    }

    public String getCode(){
        return code;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
