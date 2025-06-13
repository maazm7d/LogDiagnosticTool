package com.diagtool.loganalyzer;

import android.content.Context;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class LogProcessor {
    static {
        System.loadLibrary("log_processor");
    }
    
    private final PyObject pyModule;
    
    public LogProcessor(Context context) {
        Python py = Python.getInstance();
        pyModule = py.getModule("patterns");
    }
    
    public native int categorizeLogNative(String logLine);
    
    public String categorizeLog(String logLine) {
        int categoryCode = categorizeLogNative(logLine);
        
        switch (categoryCode) {
            case 0: return "Kernel Emergency";
            case 1: return "Application";
            case 2: return "System";
            default:
                // Use Python for uncategorized logs
                return pyModule.callAttr("match_log", logLine).toString();
        }
    }
    
    public String exportLogs(String filePath, String logs) {
        PyObject result = pyModule.callAttr("export_logs", logs, filePath);
        return result.toString();
    }
}
