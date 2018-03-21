package com.codehusky.huskycrates.exceptions;

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
        String readablePath = "";
        for(int i = 0; i < path.length; i++){
            readablePath += path[i];
            if(i+1 < path.length){
                readablePath += ".";
            }
        }
        return readablePath;
    }
}
