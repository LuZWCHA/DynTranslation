package com.nowandfuture.translation.core;

import com.baidu.translate.demo.BaiduTransApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NetworkTranslateHelper {
    private static Logger logger = LoggerFactory.getLogger(NetworkTranslateHelper.class);
    private static ITranslateApi api;

    public static String translate(String text,String from,String to) throws IOException {
        if(api != null)
            return api.getTransResult(text,from,to);
        return null;
    }

    public static void initApi(String name,String id,String key){
        if(name.equals("baidu")){
            api = new BaiduTransApi(id,key);
        }
    }
}