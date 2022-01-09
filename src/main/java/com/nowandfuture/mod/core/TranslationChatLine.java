package com.nowandfuture.mod.core;

import joptsimple.internal.Strings;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.Style;
import org.jetbrains.annotations.NotNull;

public class TranslationChatLine extends ChatLine<IReorderingProcessor> {
    private final ChatLine<IReorderingProcessor> orgChatLine;
    private String translationText = Strings.EMPTY;
    private ChatLine<IReorderingProcessor> translation;


    //func_238169_a_() is getStringComponent()
    public TranslationChatLine(ChatLine<IReorderingProcessor> orgChatLine) {
        super(0, orgChatLine.func_238169_a_(), 0);
        this.orgChatLine = orgChatLine;
    }

    public ChatLine<IReorderingProcessor> getOrgChatLine() {
        return orgChatLine;
    }

    public void setTranslationLine(ChatLine<IReorderingProcessor> translation) {
        this.translation = translation;
    }

    public void setTranslation(String text){
        if(!translationText.equals(text)) {
            translationText = text;
            IReorderingProcessor ire = IReorderingProcessor.func_242239_a(text, Style.field_240709_b_);
            translation = new ChatLine<>(orgChatLine.getUpdatedCounter(), ire, orgChatLine.getChatLineID());
        }
    }

    public ChatLine<IReorderingProcessor> getTranslation() {
        return translation;
    }

    public boolean hasTranslation(){
        return translation != null;
    }

    //func_238169_a_() is getStringComponent()
    @NotNull
    @Override
    public IReorderingProcessor func_238169_a_() {
        if(hasTranslation() && TranslationManager.INSTANCE.isEnable() && TranslationManager.INSTANCE.isEnableChatTranslate())
            return TranslationManager.INSTANCE.isRetainOrg() ?
                    IReorderingProcessor.func_242234_a(orgChatLine.func_238169_a_(),translation.func_238169_a_()):
                    translation.func_238169_a_();
        else
            return orgChatLine.func_238169_a_();
    }

    public IReorderingProcessor getOrgText(){
        return orgChatLine.func_238169_a_();
    }

    public IReorderingProcessor getTranslationText(){
        return translation != null ? translation.func_238169_a_(): null;
    }

    @Override
    public int getChatLineID() {
        return orgChatLine.getChatLineID();
    }

    @Override
    public int getUpdatedCounter() {
        return orgChatLine.getUpdatedCounter();
    }
}
