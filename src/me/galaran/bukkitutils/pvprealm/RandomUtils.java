package me.galaran.bukkitutils.pvprealm;

import java.util.Map;
import java.util.Random;

public class RandomUtils {

    public static final Random rnd = new Random();

    public static <V> V weightedChoose(Map<V, ? extends Number> map) {
        double weightSum = 0;
        for (Map.Entry<V, ? extends Number> entry : map.entrySet()) {
            weightSum += entry.getValue().doubleValue();
        }

        double num = rnd.nextDouble() * weightSum;
        for (Map.Entry<V, ? extends Number> entry : map.entrySet()) {
            double entryWeight = entry.getValue().doubleValue();
            if (num < entryWeight) {
                return entry.getKey();
            } else {
                num -= entryWeight;
            }
        }
        throw new IllegalStateException();
    }

//    public static void main(String[] args) {
//        Map<String, Number> testMap = new HashMap<String, Number>();
//        testMap.put("Common (100)", 100.0);
//        testMap.put("Rare (10)", 10.0);
//        testMap.put("Epic (1)", 1.0);
//
//        final int RUNS = 10000000;
//        Map<String, Integer> results = new HashMap<String, Integer>();
//        for (int i = 0; i < RUNS; i++) {
//            String choosen = choose(testMap);
//            Integer count = results.get(choosen);
//            if (count == null) {
//                results.put(choosen, 1);
//            } else {
//                results.put(choosen, count + 1);
//            }
//        }
//
//        System.out.println(results);
//    }
}
