package com.nowandfuture.mod.core;

import com.baidu.translate.demo.BaiduTransApi;
import com.nowandfuture.translate.MyNMTTransApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

public class NetworkTranslateHelper {
    private static Logger logger = LoggerFactory.getLogger(NetworkTranslateHelper.class);
    private static ITranslateApi api;
    private static MyNMTTransApi myNMTApi;

    @Nonnull
    public static <RESULT>ITranslateApi.TranslateResult<RESULT> translate(String text, String to) throws IOException {
        if(api != null)
            return api.getTransResult(text,"auto", to);
        return ITranslateApi.TranslateResult.createFailedResult();
    }

    public static ITranslateApi.TranslateResult<MyNMTTransApi.MyNMTTransRes> translateByNMT(String text, String from, String to) throws IOException {
        if(myNMTApi != null){
            return myNMTApi.getTransResult(text, from, to);
        }
        return ITranslateApi.TranslateResult.createFailedResult();
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