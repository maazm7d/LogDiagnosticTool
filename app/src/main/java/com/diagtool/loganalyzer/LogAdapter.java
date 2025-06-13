package com.diagtool.loganalyzer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> implements Filterable {
    private List<LogEntry> logs = new ArrayList<>();
    private List<LogEntry> filteredLogs = new ArrayList<>();
    private String currentFilter = "";

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = filteredLogs.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return filteredLogs.size();
    }

    public void addLog(LogEntry entry) {
        logs.add(0, entry); // Add to beginning for reverse chronological order
        applyFilter();
    }

    public void clearLogs() {
        logs.clear();
        filteredLogs.clear();
        notifyDataSetChanged();
    }

    public void setFilter(String filter) {
        currentFilter = filter.toLowerCase();
        applyFilter();
    }

    private void applyFilter() {
        filteredLogs.clear();
        
        if (currentFilter.isEmpty()) {
            filteredLogs.addAll(logs);
        } else {
            for (LogEntry entry : logs) {
                if (entry.message.toLowerCase().contains(currentFilter) || 
                    entry.category.toLowerCase().contains(currentFilter) ||
                    (entry.packageName != null && entry.packageName.toLowerCase().contains(currentFilter))) {
                    filteredLogs.add(entry);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null; // Not used
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Not used
            }
        };
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvMessage;
        private final TextView tvPackage;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvPackage = itemView.findViewById(R.id.tvPackage);
        }

        public void bind(LogEntry entry) {
            tvCategory.setText(entry.category);
            tvMessage.setText(entry.message);
            
            if (entry.packageName != null) {
                tvPackage.setText(entry.packageName);
                tvPackage.setVisibility(View.VISIBLE);
            } else {
                tvPackage.setVisibility(View.GONE);
            }
            
            // Color code by category
            int color = Color.GRAY;
            switch (entry.category) {
                case "Kernel Emergency": color = itemView.getContext().getResources().getColor(R.color.colorKernel); break;
                case "Application": color = itemView.getContext().getResources().getColor(R.color.colorApp); break;
                case "System": color = itemView.getContext().getResources().getColor(R.color.colorSystem); break;
                case "Network": color = itemView.getContext().getResources().getColor(R.color.colorNetwork); break;
                case "Power": color = itemView.getContext().getResources().getColor(R.color.colorPower); break;
            }
            tvCategory.setTextColor(color);
        }
    }
}
