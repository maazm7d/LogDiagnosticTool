#include <jni.h>
#include <string>
#include <regex>
#include <vector>
#include <unordered_set>

// Predefined system components
static const std::unordered_set<std::string> SYSTEM_COMPONENTS = {
    "SystemUI", "SurfaceFlinger", "WindowManager", "ActivityManager", 
    "PackageManager", "InputManager", "PowerManager"
};

extern "C" JNIEXPORT jint JNICALL
Java_com_diagtool_loganalyzer_LogProcessor_categorizeLogNative(
        JNIEnv* env,
        jobject /* this */,
        jstring logLine) {

    const char *nativeLine = env->GetStringUTFChars(logLine, 0);
    std::string line(nativeLine);
    
    // Kernel emergency patterns
    const std::regex kernelPanicRegex(
        "(Kernel panic|Watchdog timeout|CPU \\d+ Unable to handle kernel|"
        "kernel BUG at|Oops|Unable to handle kernel NULL pointer)"
    );
    
    // Application package pattern (e.g., com.example.app)
    const std::regex appRegex("\\b([a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+)\\b");
    
    // Check for kernel emergencies
    if (std::regex_search(line, kernelPanicRegex)) {
        env->ReleaseStringUTFChars(logLine, nativeLine);
        return 0; // Kernel emergency
    }
    
    // Check for application packages
    std::smatch appMatch;
    if (std::regex_search(line, appMatch, appRegex)) {
        env->ReleaseStringUTFChars(logLine, nativeLine);
        return 1; // Application log
    }
    
    // Check for system components
    for (const auto& comp : SYSTEM_COMPONENTS) {
        if (line.find(comp) != std::string::npos) {
            env->ReleaseStringUTFChars(logLine, nativeLine);
            return 2; // System component log
        }
    }
    
    env->ReleaseStringUTFChars(logLine, nativeLine);
    return 3; // Uncategorized
}
