package com.codehusky.huskycrates.exception;

public class InjectionDataError extends RuntimeException {
    private String message;

    public InjectionDataError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
