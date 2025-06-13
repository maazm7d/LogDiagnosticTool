package com.diagtool.loganalyzer;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LogCollector.LogCallback {
    private LogCollector logCollector;
    private LogProcessor logProcessor;
    private LogAdapter logAdapter;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private List<LogEntry> allLogs = new ArrayList<>();
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        logCollector = new LogCollector();
        logProcessor = new LogProcessor(this);
        logAdapter = new LogAdapter();
        
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(logAdapter);
        
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                logAdapter.setFilter(s.toString());
            }
        });
        
        // Start monitoring automatically
        toggleMonitoring();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_toggle_monitor) {
            toggleMonitoring();
            return true;
        } else if (id == R.id.action_clear) {
            clearLogs();
            return true;
        } else if (id == R.id.action_export) {
            exportLogs();
            return true;
        } else if (id == R.id.action_capture_kernel) {
            captureKernelLogs();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void toggleMonitoring() {
        if (isMonitoring) {
            logCollector.stopLogcat();
            isMonitoring = false;
            Toast.makeText(this, R.string.monitoring_stopped, Toast.LENGTH_SHORT).show();
        } else {
            logCollector.startLiveLogcat(this);
            isMonitoring = true;
            Toast.makeText(this, R.string.monitoring_started, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearLogs() {
        allLogs.clear();
        logAdapter.clearLogs();
    }
    
    private void exportLogs() {
        File exportDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS), "LogDiagnostics");
        
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String result = logProcessor.exportLogs(
            exportDir.getAbsolutePath(),
            allLogs.toString()
        );
        
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }
    
    private void captureKernelLogs() {
        new Thread(() -> {
            String kernelLogs = logCollector.captureLogs(
                logCollector.hasRootAccess() ? "dmesg" : "cat /proc/last_kmsg"
            );
            
            if (kernelLogs.isEmpty()) {
                runOnUiThread(() -> 
                    Toast.makeText(this, R.string.no_kernel_logs, Toast.LENGTH_SHORT).show()
                );
                return;
            }
            
            // Process each line
            for (String line : kernelLogs.split("\n")) {
                onNewLogLine(line);
            }
        }).start();
    }

    @Override
    public void onNewLogLine(String line) {
        String category = logProcessor.categorizeLog(line);
        
        // Extract package name from app logs
        String packageName = null;
        if (category.equals("Application")) {
            packageName = extractPackageName(line);
        }
        
        LogEntry entry = new LogEntry(category, line, packageName);
        allLogs.add(entry);
        
        runOnUiThread(() -> logAdapter.addLog(entry));
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> 
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        );
    }
    
    private String extractPackageName(String logLine) {
        // Simple pattern to extract package names
        int start = logLine.indexOf('(');
        int end = logLine.indexOf(')', start > 0 ? start : 0);
        
        if (start > 0 && end > start) {
            String candidate = logLine.substring(start + 1, end);
            if (candidate.contains(".") && !candidate.contains(" ")) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logCollector.stopLogcat();
    }
}
