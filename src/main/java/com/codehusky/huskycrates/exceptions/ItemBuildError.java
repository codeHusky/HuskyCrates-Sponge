package com.codehusky.huskycrates.exceptions;

public class ItemBuildError extends RuntimeException {
    private String message;
    public ItemBuildError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
