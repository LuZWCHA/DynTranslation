package com.nowandfuture.translation.core;

import java.io.UnsupportedEncodingException;

public interface ITranslateApi {
    String getTransResult(String query, String from, String to) throws UnsupportedEncodingException;
}
