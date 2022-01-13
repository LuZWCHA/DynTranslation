package com.nowandfuture.translation.core;

import com.baidu.translate.demo.BaiduTransApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class NetworkTranslateHelper {
    private static Logger logger = LoggerFactory.getLogger(NetworkTranslateHelper.class);
    private static ITranslateApi api;
    private static ITranslateApi myNMTApi;

    public static String translate(String text,String from,String to) throws IOException {
        if(api != null)
            return api.getTransResult(text,from,to);
        return null;
    }

    public static String translateByNMT(String text,String from,String to) throws IOException {
        if(myNMTApi != null){
            return myNMTApi.getTransResult(text, from, to);
        }
        return null;
    }

    public static void initNMTApi(){
        myNMTApi = new MyNMTTransApi();
    }

    public static void initApi(String name,String id,String key){
        if(name.equals("baidu")){
            api = new BaiduTransApi(id,key);
        }
    }
}