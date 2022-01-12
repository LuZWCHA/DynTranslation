package com.baidu.translate.demo;



import com.nowandfuture.mod.core.api.ITranslateApi;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaiduTransApi implements ITranslateApi<TranslateData> {
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    private String appid;
    private String securityKey;

    public BaiduTransApi(String appid, String securityKey) {
        this.appid = appid;
        this.securityKey = securityKey;
    }

    @Override
    public TranslateResult<TranslateData> getTransResult(String query, String from, String to) throws UnsupportedEncodingException {
        Map<String, String> params = buildParams(query, from, to);
        String res = HttpGet.get(TRANS_API_HOST, params);
        if(!Objects.isNull(res)){
            if(res.contains("error"))
                return TranslateResult.createFailedResult();
            else
                return TranslateResult.createSuccessResult(res);
        }else
            return TranslateResult.createFailedResult();
    }

    private Map<String, String> buildParams(String query, String from, String to) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }

}
