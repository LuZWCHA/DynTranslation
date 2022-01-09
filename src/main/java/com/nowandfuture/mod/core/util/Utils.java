package com.nowandfuture.mod.core.util;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static  <T, R, S> Map<T, Map<R,S>> combineMaps(Map<T, Map<R,S>> src, Map<T, Map<R,S>> dst){
        src.forEach((t, rsMap) -> {
            if(!dst.containsKey(t)){
                dst.put(t, new HashMap<>());
            }

            dst.get(t).putAll(rsMap);
        });

        return dst;
    }
}
