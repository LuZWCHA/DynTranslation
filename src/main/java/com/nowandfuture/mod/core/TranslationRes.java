package com.nowandfuture.mod.core;

public class TranslationRes {
    public ControlChars controlChars;
    public String text;
    public boolean isTranslated = false;

    public TranslationRes(String text){
        controlChars = ControlChars.EMPTY;
        this.text = text;
    }

    public TranslationRes(ControlChars controlChars,String text){
        this.controlChars = controlChars;
        this.text = text;
        this.isTranslated = true;
    }

    public TranslationRes set(ControlChars controlChars,String text){
        this.text = text;
        this.controlChars = controlChars;
        this.isTranslated = true;
        return this;
    }

    public TranslationRes set(String text){
        this.text = text;
        this.controlChars = ControlChars.EMPTY;
        this.isTranslated = true;
        return this;
    }

    public TranslationRes notTranslated(){
        this.isTranslated = false;
        return this;
    }

    public TranslationRes set(TranslationRes res) {
        return set(res.controlChars, res.text);
    }
}
