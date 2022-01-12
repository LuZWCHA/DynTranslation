package com.nowandfuture.mod.core.forgeimpl;

import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.text.Style;

public class CharConsumer implements ICharacterConsumer {
    private StringBuilder builder = new StringBuilder();
    private Style style = Style.field_240709_b_;

    @Override
    public boolean accept(int p_accept_1_, Style p_accept_2_, int p_accept_3_) {
        builder.appendCodePoint(p_accept_3_);
        style = p_accept_2_;
        return true;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    public Style getStyle() {
        return style;
    }
}
