package com.codehusky.huskycrates.exception;

public class RewardDeliveryError extends RuntimeException {
    private String message;

    public RewardDeliveryError(final String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
