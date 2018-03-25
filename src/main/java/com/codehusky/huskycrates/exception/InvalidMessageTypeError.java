package com.codehusky.huskycrates.exception;

public class InvalidMessageTypeError extends RuntimeException {
    private String message;
    public InvalidMessageTypeError(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
