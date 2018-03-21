package com.codehusky.huskycrates.exceptions;

public class DoubleRegistrationError extends RuntimeException {
    private String message;

    public DoubleRegistrationError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
