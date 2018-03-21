package com.codehusky.huskycrates.exception;

public class SlotSelectionError extends RuntimeException {
    private String message;

    public SlotSelectionError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
