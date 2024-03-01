package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

class Util {
    public static <K, V> void addToListInMap(Map<K, List<V>> map, K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            map.put(key, new ArrayList<>() {{ add(value); }});
        }
    }

    public static <K1, K2, M extends Map<K2, V>, V> void putInMapInMap(Map<K1, M> map, Supplier<M> newM, K1 key1, K2 key2, V value) {
        if (map.containsKey(key1)) {
            map.get(key1).put(key2, value);
        } else {
            M val = newM.get();
            val.put(key2, value);
            map.put(key1, val);
        }
    }

    public static <K, V> List<V> getListInMap(Map<K, List<V>> map, K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return new ArrayList<>();
        }
    }

    public static <K1, K2, M extends Map<K2, List<V>>, V> void addToListInMapMap(Map<K1, M> map, Supplier<M> newM, K1 key1, K2 key2, V value) {
        if (!map.containsKey(key1)) {
            map.put(key1, newM.get());
        }
        if (map.get(key1).containsKey(key2)) {
            map.get(key1).get(key2).add(value);
        } else {
            map.get(key1).put(key2, new ArrayList<>() {{ add(value); }});
        }
    }

    public static <K1, K2, K3, M1 extends Map<K2, M2>, M2 extends Map<K3, V>, V> void putInMapInMapMap(Map<K1, M1> map,
                                                                                                        Supplier<M1> newM1, Supplier<M2> newM2,
                                                                                                        K1 key1, K2 key2, K3 key3, V value) {
        if (!map.containsKey(key1)) {
            map.put(key1, newM1.get());
        }
        if (map.get(key1).containsKey(key2)) {
            map.get(key1).get(key2).put(key3, value);
        } else {
            M2 val = newM2.get();
            val.put(key3, value);
            map.get(key1).put(key2, val);
        }
    }

    public static <K1, K2, M extends Map<K2, TreeSet<V>>, V> void addToTreeSetInMapMap(Map<K1, M> map, Supplier<M> newM, K1 key1, K2 key2, V value) {
        if (!map.containsKey(key1)) {
            map.put(key1, newM.get());
        }
        if (map.get(key1).containsKey(key2)) {
            map.get(key1).get(key2).add(value);
        } else {
            map.get(key1).put(key2, new TreeSet<>() {{ add(value); }});
        }
    }

    public static <E> void ensureCapacity(List<E> list, Supplier<E> newM, int index) {
        while (list.size() <= index) {
            list.add(newM.get());
        }
    }

    public static <K, V> void ensureKey(Map<K, V> map, Supplier<V> newM, K key) {
        if (!map.containsKey(key)) {
            map.put(key, newM.get());
        }
    }

    private Util() {}
}
