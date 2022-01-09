package com.nowandfuture.mod.core;

import com.baidu.translate.demo.BaiduTransApi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class NetworkTranslateHelper {
    private final static Logger logger = LoggerFactory.getLogger(NetworkTranslateHelper.class);
    private static ITranslateApi api;
    public static String AUTO = "auto";

    private final static String DEFAULT_API_NAME = "baidu";

    @NotNull
    public static ITranslateApi.TranslateResult translate(String text, String from, String to) throws IOException {
        if(!Objects.isNull(api))
            return api.getTransResult(text,from,to);
        return ITranslateApi.TranslateResult.createFailedResult();
    }

    @NotNull
    public static ITranslateApi.TranslateResult translate(String text,String to) throws IOException {
        if(!Objects.isNull(api))
            return api.getTransResult(text,AUTO,to);
        return ITranslateApi.TranslateResult.createFailedResult();
    }

    public static void initApi(String name,String id,String key){
        if(DEFAULT_API_NAME.equals(name)){
            api = createDefaultApi(id, key);
        }

        if(Objects.isNull(api)) logger.debug("create API failed!");
    }

    private static ITranslateApi createDefaultApi(String id, String key){
        return new BaiduTransApi(id,key);
    }
}