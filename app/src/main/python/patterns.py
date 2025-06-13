import re
import json
from datetime import datetime

# Load patterns from configuration
PATTERNS = {
    "Kernel": [
        r"kernel\..*",
        r"\[.*\]\s+kernel:",
        r"<3>.*kernel",
        r"ION_heap"
    ],
    "Network": [
        r"wlan|netd|DHCP|Wifi|Bluetooth|NFC|Connectivity"
    ],
    "Power": [
        r"battery|charge|power|thermal"
    ],
    "Storage": [
        r"sd|emmc|ext4|f2fs|vold"
    ],
    "Security": [
        r"selinux|avc|permission|keystore"
    ]
}

def match_log(log_line, patterns=PATTERNS):
    """Match log line against predefined patterns"""
    for category, regex_list in patterns.items():
        for pattern in regex_list:
            if re.search(pattern, log_line, re.IGNORECASE):
                return category
    return "Uncategorized"

def export_logs(logs, file_path):
    """Export logs to file with timestamp"""
    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{file_path}/log_export_{timestamp}.txt"
        
        with open(filename, "w") as f:
            for log in logs:
                f.write(f"[{log['category']}] {log['message']}\n")
                
        return f"Exported to {filename}"
    except Exception as e:
        return f"Export failed: {str(e)}"
