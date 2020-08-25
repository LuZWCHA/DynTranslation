package com.nowandfuture.mod.core;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

public class TranslationChatLine extends ChatLine {
    private final ChatLine orgChatLine;
    private ChatLine translation;

    public TranslationChatLine(ChatLine orgChatLine) {
        super(0, orgChatLine.getChatComponent(), 0);
        this.orgChatLine = orgChatLine;
    }

    public ChatLine getOrgChatLine() {
        return orgChatLine;
    }

    public void setTranslationLine(ChatLine translation) {
        this.translation = translation;
    }

    public void setTranslation(String text){
        translation = new ChatLine(orgChatLine.getUpdatedCounter(),new StringTextComponent(text),orgChatLine.getChatLineID());
    }

    public ChatLine getTranslation() {
        return translation;
    }

    public boolean hasTranslation(){
        return translation != null;
    }

    @NotNull
    @Override
    public ITextComponent getChatComponent() {
        if(hasTranslation() && TranslationManager.INSTANCE.isEnable() && TranslationManager.INSTANCE.isEnableChatTranslate())
            return TranslationManager.INSTANCE.isRetainOrg() ?
                    new StringTextComponent(orgChatLine.getChatComponent().getFormattedText() + ": ").appendSibling(translation.getChatComponent()):
                    translation.getChatComponent();
        else
            return orgChatLine.getChatComponent();
    }

    public ITextComponent getOrgText(){
        return orgChatLine.getChatComponent();
    }

    public ITextComponent getTranslationText(){
        return translation != null ? translation.getChatComponent(): null;
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
