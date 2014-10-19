package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class StatsRecorder {
    private static final HashMap<String, LinkedList<Double>> stats = new HashMap<>();

    public static void record(String key, double value) {
        LinkedList<Double> list;
        if (!stats.containsKey(key)) {
            list = new LinkedList<>();
            stats.put(key, list);
        } else {
            list = stats.get(key);
        }
        list.add(value);
    }

    public static void printStats() {
        for (String key : stats.keySet()) {
            List<Double> list = stats.get(key);
            System.out.println(String.format("[key: %s, elements: %s]", key, list.size()));


            for (int i=0; i < list.size(); i++) {
                System.out.print(i + ",");
            }

            System.out.println();

            ListIterator iterator = list.listIterator();

            while(iterator.hasNext()) {
                System.out.printf("%.0f,", iterator.next());
            }
            System.out.println();
            System.out.println();
        }
    }
}
