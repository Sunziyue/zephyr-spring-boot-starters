package xyz.szy.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapBuilder<K, V> {
    private final Map<K, V> map;
    private boolean ignoreNullValue = false;

    private MapBuilder() {
        this.map = new HashMap<>();
    }

    private MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    public static <K, V> MapBuilder<K, V> of() {
        return new MapBuilder<>();
    }

    public static <K, V> MapBuilder<K, V> of(Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    public static <K, V> MapBuilder<K, V> newHashMap() {
        return of(new HashMap<>());
    }

    public static <K, V> MapBuilder<K, V> newTreeMap() {
        return of(new TreeMap<>());
    }

    public MapBuilder<K, V> ignoreNullValue() {
        this.ignoreNullValue = true;
        return this;
    }

    public MapBuilder<K, V> put(K key, V value) {
        if (this.ignoreNullValue && value == null) {
            return this;
        } else {
            this.map.put(key, value);
            return this;
        }
    }

    public MapBuilder<K, V> put(K k1, V v1, K k2, V v2) {
        return this.put(k1, v1).put(k2, v2);
    }

    public MapBuilder<K, V> put(K k1, V v1, K k2, V v2, K k3, V v3) {
        return this.put(k1, v1).put(k2, v2).put(k3, v3);
    }

    public MapBuilder<K, V> put(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return this.put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4);
    }

    public MapBuilder<K, V> put(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return this.put(k1, v1).put(k2, v2).put(k3, v3).put(k4, v4).put(k5, v5);
    }

    public Map<K, V> map() {
        return this.map;
    }
}
