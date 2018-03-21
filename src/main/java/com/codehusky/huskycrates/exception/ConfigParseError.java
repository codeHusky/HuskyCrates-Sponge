package com.codehusky.huskycrates.exception;

public class ConfigParseError extends ConfigError {
    public ConfigParseError(final String message, final Object[] path){
        super(message,path);
    }
}
