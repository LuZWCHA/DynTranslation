package com.nowandfuture.mod.core;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface ITranslateApi {
    TranslateResult getTransResult(String query, String from, String to) throws UnsupportedEncodingException;



    enum STATE{
        SUCCESS,
        FAILED
    }

    class TranslateResult{
        private String result;
        private STATE state;
        private Map<String,String> others;


        public TranslateResult(@NotNull String result,@NotNull STATE state) {
            this.result = result;
            this.state = state;
            this.others = null;
        }

        public TranslateResult(@NotNull String result, @NotNull STATE state,@NotNull Map<String, String> others) {
            this.result = result;
            this.state = state;
            this.others = others;
        }

        public static TranslateResult createSuccessResult(String result){
            return new TranslateResult(result,STATE.SUCCESS);
        }

        public static TranslateResult createFailedResult(){
            return new TranslateResult("null",STATE.FAILED);
        }

        public static TranslateResult createResult(String result, Map<String, String> others){
            return new TranslateResult(result,STATE.SUCCESS, others);
        }

        public Map<String, String> getOthers() {
            return others;
        }

        public STATE getState() {
            return state;
        }

        public String getResult() {
            return result;
        }
    }
}
