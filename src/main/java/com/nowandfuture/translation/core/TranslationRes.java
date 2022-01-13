package com.nowandfuture.translation.core;

public class TranslationRes {
    public ControlChars controlChars;
    public String text;

    public TranslationRes(String text){
        controlChars = ControlChars.EMPTY;
        this.text = text;
    }

    public TranslationRes(ControlChars controlChars,String text){
        this.controlChars = controlChars;
        this.text = text;
    }

    public TranslationRes set(ControlChars controlChars,String text){
        this.text = text;
        this.controlChars = controlChars;
        return this;
    }

    public TranslationRes set(String text){
        this.text = text;
        this.controlChars = ControlChars.EMPTY;
        return this;
    }

    public TranslationRes set(TranslationRes res){
        this.text = res.text;
        this.controlChars = res.controlChars;
        return this;
    }
}
