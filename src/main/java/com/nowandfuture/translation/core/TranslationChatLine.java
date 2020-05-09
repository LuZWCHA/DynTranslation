package com.nowandfuture.translation.core;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;

public class TranslationChatLine extends ChatLine {
    private ChatLine orgChatLine;

    public TranslationChatLine(ChatLine orgChatLine ,int updateCounterCreatedIn, ITextComponent lineStringIn, int chatLineIDIn) {
        super(updateCounterCreatedIn, lineStringIn, chatLineIDIn);
        this.orgChatLine = orgChatLine;
    }

    public ChatLine getOrgChatLine() {
        return orgChatLine;
    }
}
