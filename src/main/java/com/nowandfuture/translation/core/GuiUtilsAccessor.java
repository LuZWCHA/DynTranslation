package com.nowandfuture.translation.core;

import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface GuiUtilsAccessor{
    boolean isEmpty();

    static boolean isTooltipRendering(){
        return !((GuiUtilsAccessor)new GuiUtils()).isEmpty();
    }
}