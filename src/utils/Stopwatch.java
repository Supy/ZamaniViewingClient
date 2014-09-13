package utils;

import java.util.HashMap;

public class Stopwatch {
    private static HashMap<String, Long> startTimes = new HashMap<>();
    private static HashMap<String, Long> durations = new HashMap<>();

    public static void start(String key) {
        startTimes.put(key, System.currentTimeMillis());
    }

    public static void stop(String key) {
        if(!startTimes.containsKey(key)) {
            throw new IllegalArgumentException("No stopwatch with key '" + key +"' was started.");
        } else {
            durations.put(key, System.currentTimeMillis() - startTimes.get(key));
        }
    }

    public static void stopAndPrint(String key) {
        stop(key);
        System.out.println("[" + key + "] took " + durations.get(key) + "ms.");
    }
}
