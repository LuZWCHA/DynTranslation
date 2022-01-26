package com.nowandfuture.mod.vanillaopt;

import java.util.HashMap;
import java.util.Map;

public enum ExtraLanguageMap {
    INSTANCE;

    private final Map<String, String> map;

    ExtraLanguageMap(){
        map = new HashMap<>();
    }

    public void put(String key, String value, String id){
        map.put(id + "." + key, value);
    }

    public String get(String key, String id){
        return map.getOrDefault(id + "." + key, key);
    }

    public boolean contains(String key, String id){
        return map.containsKey(key + "." + id);
    }
}
