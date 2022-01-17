package com.nowandfuture.translate;


import com.google.gson.annotations.SerializedName;
import com.nowandfuture.mod.core.api.ITranslateApi;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyNMTTransApi implements ITranslateApi<MyNMTTransApi.MyNMTTransRes> {

    public static class MyNMTTransRes implements Serializable {

        /**
         * translation : {"from":"hi, world","to":["嗨,世界", “嘿,世界”]}
         */

        @SerializedName("translation")
        private TranslationBean translation;

        public TranslationBean getTranslation() {
            return translation;
        }

        public static class TranslationBean implements Serializable {
            /**
             * from : hi, world
             * to : [“嗨,世界”, “嘿,世界”]
             */

            @SerializedName("from")
            private String from;
            @SerializedName("to")
            private List<String> to;

            public String getFrom() {
                return from;
            }

            public List<String> getTo() {
                return to;
            }

            @Override
            public String toString() {
                return "TranslationBean{" +
                        "from='" + from + '\'' +
                        ", to='" + to.toString() + '\'' +
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

    private static final String TRANS_API_HOST = "http://mc.nowandfuture.top:7002/nowai/api/v2.0/translate";

    public MyNMTTransApi() {

    }

    @Override
    public TranslateResult<MyNMTTransRes> getTransResult(String query, String from, String to) {

        Map<String, String> parms = new HashMap<>();
        parms.put("content", query);
        parms.put("ret_num", "3");

        HttpUtils.HttpStringRes res = HttpUtils.get(TRANS_API_HOST, parms);
        if (!Objects.isNull(res)) {
            if (res.getCode() != HttpURLConnection.HTTP_OK) {
                return TranslateResult.createFailedResult(res.getData());
            } else {
                return TranslateResult.createSuccessResult(res.getData());
            }
        } else
            return TranslateResult.createFailedResult();
    }


}
