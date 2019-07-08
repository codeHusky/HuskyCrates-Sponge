package com.codehusky.huskycrates.exception;

public class ConfigError extends RuntimeException {

    private String message;

    ConfigError(final String message, final Object[] path){
        this.message = message;
        this.message += " Issue can be located at " + readablePath(path);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static String readablePath(Object[] path){
        StringBuilder readablePath = new StringBuilder();
        for(int i = 0; i < path.length; i++){
            readablePath.append(path[i]);
            if(i+1 < path.length){
                readablePath.append(".");
            }
        }
        return readablePath.toString();
    }
}
