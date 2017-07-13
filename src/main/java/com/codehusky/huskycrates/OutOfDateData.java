package com.codehusky.huskycrates;

/**
 * Created by lokio on 7/13/2017.
 */
public class OutOfDateData {
    private String newVersion;
    private boolean outOfDate = false;
    public OutOfDateData() {

    }
    public OutOfDateData(String newVersion){
        outOfDate = true;
        this.newVersion = newVersion;
    }

    public boolean isOutOfDate(){
        return outOfDate;
    }

    public String latestVersion() {
        return newVersion;
    }
}
