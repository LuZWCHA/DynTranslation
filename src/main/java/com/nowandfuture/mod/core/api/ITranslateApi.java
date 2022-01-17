package com.nowandfuture.mod.core.api;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface ITranslateApi<RESULT> {
    TranslateResult<RESULT> getTransResult(String query, String from, String to) throws UnsupportedEncodingException;



    enum STATE{
        SUCCESS,
        FAILED
    }

    class TranslateResult<RESULT>{
        private String result;
        private STATE state;
        private Map<String,String> others;
        //todo
        private RESULT body;


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

        public static <RESULT>TranslateResult<RESULT> createSuccessResult(String result){
            return new <RESULT>TranslateResult<RESULT>(result, STATE.SUCCESS);
        }

        public static <RESULT>TranslateResult<RESULT> createFailedResult(){
            return new <RESULT>TranslateResult<RESULT>("null", STATE.FAILED);
        }

        public static <RESULT>TranslateResult<RESULT> createFailedResult(String failMessage){
            return new <RESULT>TranslateResult<RESULT>(failMessage, STATE.FAILED);
        }

        public static <RESULT>TranslateResult<RESULT> createResult(String result, Map<String, String> others){
            return new <RESULT>TranslateResult<RESULT>(result, STATE.SUCCESS, others);
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
