package com.nowandfuture.mod.core;


import net.minecraftforge.fml.client.gui.GuiUtils;

public interface GuiUtilsAccessor{
    boolean isEmpty();

    GuiUtilsAccessor g = (GuiUtilsAccessor) new GuiUtils();
    static boolean isTooltipRendering(){
        return !g.isEmpty();
    }
}