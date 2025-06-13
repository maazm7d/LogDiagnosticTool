package com.diagtool.loganalyzer;

import java.util.Date;

public class LogEntry {
    public final String category;
    public final String message;
    public final long timestamp;
    public final String packageName;
    
    public LogEntry(String category, String message, String packageName) {
        this.category = category;
        this.message = message;
        this.packageName = packageName;
        this.timestamp = new Date().getTime();
    }
}
