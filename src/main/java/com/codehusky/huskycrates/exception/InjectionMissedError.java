package com.codehusky.huskycrates.exception;

public class InjectionMissedError extends RuntimeException {
    private String message;

    public InjectionMissedError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
