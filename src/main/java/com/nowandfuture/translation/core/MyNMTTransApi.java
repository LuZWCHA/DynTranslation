package com.nowandfuture.translation.core;


import com.baidu.translate.demo.HttpGet;

import java.io.IOException;

public class MyNMTTransApi implements ITranslateApi{

    private static final String TRANS_API_HOST = "http://mc.nowandfuture.top:7002/nowai/api/v1.0/translate";

    public MyNMTTransApi(){

    }

    @Override
    public String getTransResult(String query, String from, String to) throws IOException {
        return HttpGet.get(TRANS_API_HOST + '/' + HttpGet.encode(query),null);
    }


    public static void main(String[] args) {
        System.out.println("start");
        String query = "hi, world";
        String res = HttpGet.get(TRANS_API_HOST + '/' + HttpGet.encode(query),null);
        System.out.println(res);
    }
}
