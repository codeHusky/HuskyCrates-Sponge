package com.codehusky.huskycrates.exception;

public class VirtualKeyStarvedError extends RuntimeException {
    private String message;
    public VirtualKeyStarvedError(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
