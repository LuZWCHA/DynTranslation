package com.nowandfuture.mod.core.forgeimpl;

import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.text.Style;

public class CharConsumer implements ICharacterConsumer {
    private StringBuilder builder = new StringBuilder();
    private Style style = Style.field_240709_b_;

    @Override
    public boolean accept(int code, Style style, int p_accept_3_) {
        builder.appendCodePoint(p_accept_3_);
        this.style = style;
        return true;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    public Style getStyle() {
        return style;
    }
}
