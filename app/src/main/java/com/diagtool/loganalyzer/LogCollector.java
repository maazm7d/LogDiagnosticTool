package com.diagtool.loganalyzer;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogCollector {
    private static final String TAG = "LogCollector";
    private static final int MAX_LINES = 1000;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread logcatThread;

    public interface LogCallback {
        void onNewLogLine(String line);
        void onError(String error);
    }

    public boolean hasRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String captureLogs(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(
                hasRootAccess() ? new String[]{"su", "-c", command} : 
                new String[]{"sh", "-c", command}
            );
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < MAX_LINES) {
                output.append(line).append("\n");
                count++;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error capturing logs: " + e.getMessage());
        }
        return output.toString();
    }

    public void startLiveLogcat(LogCallback callback) {
        if (isRunning.get()) return;
        
        isRunning.set(true);
        logcatThread = new Thread(() -> {
            try {
                String[] cmd = hasRootAccess() ? 
                    new String[]{"su", "-c", "logcat -v raw"} : 
                    new String[]{"logcat", "-v", "raw"};
                
                Process process = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                
                String line;
                while (isRunning.get() && (line = reader.readLine()) != null) {
                    callback.onNewLogLine(line);
                }
            } catch (IOException e) {
                callback.onError("Log stream error: " + e.getMessage());
            }
        });
        
        logcatThread.start();
    }

    public void stopLogcat() {
        isRunning.set(false);
        if (logcatThread != null && logcatThread.isAlive()) {
            logcatThread.interrupt();
        }
    }
}
