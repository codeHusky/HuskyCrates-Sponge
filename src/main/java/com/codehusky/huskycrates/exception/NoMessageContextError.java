package com.codehusky.huskycrates.exception;

public class NoMessageContextError extends RuntimeException {
    private String message;
    public NoMessageContextError(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
