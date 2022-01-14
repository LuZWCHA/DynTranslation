package com.nowandfuture.translate;

import com.baidu.translate.demo.HttpGet;
import com.google.gson.GsonBuilder;
import com.nowandfuture.mod.core.api.ITranslateApi;
import com.nowandfuture.mod.core.util.NetworkTranslateHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MyNMTTransApiTest {

    @Test
    public void testTranslationApi() {
        final String query = "hi, world";
        NetworkTranslateHelper.initNMTApi();
        ITranslateApi.TranslateResult<MyNMTTransApi.MyNMTTransRes> res =
                assertDoesNotThrow(() -> NetworkTranslateHelper.translateByNMT(query, "en", "zh"));
        assertNotNull(res);

    }

}