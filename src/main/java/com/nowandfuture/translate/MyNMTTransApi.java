package com.nowandfuture.translate;


import com.baidu.translate.demo.HttpGet;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.nowandfuture.mod.core.api.ITranslateApi;
import com.nowandfuture.mod.core.util.NetworkTranslateHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class MyNMTTransApi implements ITranslateApi<MyNMTTransApi.MyNMTTransRes> {

    public static class MyNMTTransRes implements Serializable {

        /**
         * translation : {"from":"hi, world","to":"嗨,世界"}
         */

        @SerializedName("translation")
        private TranslationBean translation;

        public TranslationBean getTranslation() {
            return translation;
        }

        public static class TranslationBean implements Serializable {
            /**
             * from : hi, world
             * to : 嗨,世界
             */

            @SerializedName("from")
            private String from;
            @SerializedName("to")
            private String to;

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }

            @Override
            public String toString() {
                return "TranslationBean{" +
                        "from='" + from + '\'' +
                        ", to='" + to + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "MyNMTTransRes{" +
                    "translation=" + translation +
                    '}';
        }
    }

    private static final String TRANS_API_HOST = "http://mc.nowandfuture.top:7002/nowai/api/v1.0/translate";

    public MyNMTTransApi(){

    }

    @Override
    public TranslateResult<MyNMTTransRes> getTransResult(String query, String from, String to) {

        String res = HttpGet.get(TRANS_API_HOST + '/' + HttpGet.encode(query).replace("+", "%20"), null);
        if(!Objects.isNull(res)){
            if(res.contains("error"))
                return TranslateResult.createFailedResult();
            else
                return TranslateResult.createSuccessResult(res);
        }else
            return TranslateResult.createFailedResult();
    }

    public static void main(String[] args) {
        String query = "hi, world";
        NetworkTranslateHelper.initNMTApi();
        ITranslateApi.TranslateResult<MyNMTTransRes> res;
        try {
            res = NetworkTranslateHelper.translateByNMT(query, "en", "zh");
            if(res != null && res.getState().equals(STATE.SUCCESS)) {
                if(res.getState().equals(STATE.SUCCESS)) {
                    String string = HttpGet.unicode2String(res.getResult());
                    MyNMTTransRes nmtTransRes = new GsonBuilder().create().fromJson(string, MyNMTTransRes.class);
                    System.out.println(nmtTransRes);
                }else{
                    System.out.println(res.getResult());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
