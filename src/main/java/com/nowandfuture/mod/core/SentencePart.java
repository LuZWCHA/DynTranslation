package com.nowandfuture.mod.core;

public class SentencePart {
    public long start;
    public long end;
    public short flag;

    private SentencePart(long start, long end){
        this.start = start;
        this.end = end;
        this.flag = 0;
    }

    public static SentencePart newPart(long start, long end){
        return new SentencePart(start, end);
    }

    public static SentencePart newPartE(long end){
        return newPart(0, end);
    }

    public String getString(String orgText){
        return orgText.substring((int)start, (int)end);
    }

}
