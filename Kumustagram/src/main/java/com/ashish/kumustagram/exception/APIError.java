package com.ashish.kumustagram.exception;
import lombok.Data;

@Data
public class APIError {

    private boolean success = false;
    private String message;

    public APIError(String message){
        this.message = message;
    }

}
