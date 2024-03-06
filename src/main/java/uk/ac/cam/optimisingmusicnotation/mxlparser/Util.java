package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * A class for performing common collection manipulations in the parser.
 */
class Util {
    /**
     * Adds to a list in a map. Will create a new ArrayList if the required list does not exist.
     * @param map the map to add to
     * @param key the key of the list to add to
     * @param value the value being added
     * @param <K> the type of the key
     * @param <V> the type of the value being added
     */
    public static <K, V> void addToListInMap(Map<K, List<V>> map, K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            map.put(key, new ArrayList<>() {{ add(value); }});
        }
    }

    /**
     * Puts a key-value pair into a map in a map. Will create a new map if the required map does not exist.
     * @param map the map to put in to
     * @param newM the supplier for new inner maps
     * @param key1 the first key to get the map
     * @param key2 the second key for the value
     * @param value the value being put in the map
     * @param <K1> the type of the first key
     * @param <K2> the type of the second key
     * @param <M> the type of the map stored in the highest level map
     * @param <V> the type of the values stored in the map
     */
    public static <K1, K2, M extends Map<K2, V>, V> void putInMapInMap(Map<K1, M> map, Supplier<M> newM, K1 key1, K2 key2, V value) {
        if (map.containsKey(key1)) {
            map.get(key1).put(key2, value);
        } else {
            M val = newM.get();
            val.put(key2, value);
            map.put(key1, val);
        }
    }

    /**
     * Puts a key-value pair into a map in a map in a map. Will create a new maps if the required map does not exist.
     * @param map the map of a map of a mpa to put the value in
     * @param newM1 the supplier for a new middle map
     * @param newM2 the supplier for a new inner map
     * @param key1 the key of the outer map
     * @param key2 the key of the middle map
     * @param key3 the key of the inner map
     * @param value the value being put in the key
     * @param <K1> the type of the first key
     * @param <K2> the type of the second key
     * @param <K3> the type of the third key
     * @param <M1> the type of the middle map
     * @param <M2> the type of the inner map
     * @param <V> the type fo the value being stored
     */
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

    /**
     * Adds to a TreeSet in a map in a map. It creates new maps and sets if a given key does not have a value.
     * @param map the map to be added to
     * @param newM the supplier for a new inner map
     * @param key1 the first key for the outer map
     * @param key2 the second key for the inner map
     * @param value the value being added
     * @param <K1> the type of the first key
     * @param <K2> the type of the second key
     * @param <M> the type of the inner map
     * @param <V> the type of the value being added
     */
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

    /**
     * Ensures a list has a given index
     * @param list the list
     * @param newM the supplier for the elements to be added
     * @param index the index to ensure
     * @param <E> the type of the element in the list
     */
    public static <E> void ensureCapacity(List<E> list, Supplier<E> newM, int index) {
        while (list.size() <= index) {
            list.add(newM.get());
        }
    }

    private Util() {}
}
