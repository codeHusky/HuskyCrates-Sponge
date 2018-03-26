package com.codehusky.huskycrates.exception;

public class InvalidCrateIDError extends RuntimeException {
    private String message;

    public InvalidCrateIDError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
