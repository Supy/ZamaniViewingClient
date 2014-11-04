package utils;

import java.util.concurrent.ConcurrentHashMap;

public class Stopwatch {
    private static ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Long> durations = new ConcurrentHashMap<>();

    public static void start(String key) {
        startTimes.put(key, System.currentTimeMillis());
    }

    public static void stop(String key) {
        if(!startTimes.containsKey(key)) {
            throw new IllegalArgumentException("No stopwatch with key '" + key +"' was started.");
        } else {
            if (durations.containsKey(key)) {
                durations.replace(key, getTime(key) + System.currentTimeMillis() - startTimes.get(key));
            } else {
                durations.put(key, System.currentTimeMillis() - startTimes.get(key));
            }
        }
    }

    public static long getTime(String key) {
        return durations.get(key);
    }

    public static void stopAndPrint(String key) {
        stop(key);
        printTime(key);
    }

    public static void printTime(String key) {
        System.out.println("[" + key + "] took " + durations.get(key) + "ms.");
    }
}
