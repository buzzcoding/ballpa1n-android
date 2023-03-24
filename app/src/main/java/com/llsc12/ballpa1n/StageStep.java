package com.llsc12.ballpa1n;

public class StageStep {
    public final String status;
    public final float argInterval;
    public final ConsoleStep[] consoleLogs;

    public StageStep(String status, float argInterval, ConsoleStep[] consoleLogs) {
        this.status = status;
        this.argInterval = argInterval;
        this.consoleLogs = consoleLogs;
    }
}
