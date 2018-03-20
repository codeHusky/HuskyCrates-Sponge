package com.codehusky.huskycrates.exceptions;

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
