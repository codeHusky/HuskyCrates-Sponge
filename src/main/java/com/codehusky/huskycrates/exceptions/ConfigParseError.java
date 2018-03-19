package com.codehusky.huskycrates.exceptions;

public class ConfigParseError extends ConfigError {
    public ConfigParseError(final String message, final Object[] path){
        super(message,path);
    }
}
