package com.nowandfuture.translation.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FIFOCache<K,V> extends LinkedHashMap<K,V> {
    private int cacheSize;

    public FIFOCache(int cacheSize) {
        super(cacheSize, 0.75f, false);
        this.cacheSize = cacheSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= cacheSize;
    }

}
