package com.baidu.translate.demo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TranslateData {

    /**
     * trans_result : [{"dst":"玩家567取得了进步[用钻石掩护我]","src":"Player567 has made the advancement [Cover Me With Diamonds]"}]
     * from : en
     * to : zh
     */
    @SerializedName("trans_result")
    private List<TransResultEntity> transResult;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;

    public void setTransResult(List<TransResultEntity> transResult) {
        this.transResult = transResult;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<TransResultEntity> getTransResult() {
        return transResult;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public class TransResultEntity {
        /**
         * dst : 玩家567取得了进步[用钻石掩护我]
         * src : Player567 has made the advancement [Cover Me With Diamonds]
         */
        @SerializedName("dst")
        private String dst;
        @SerializedName("src")
        private String src;

        public void setDst(String dst) {
            this.dst = dst;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getDst() {
            return dst;
        }

        public String getSrc() {
            return src;
        }
    }
}
