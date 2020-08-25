package com.nowandfuture.mod.mixins;

import com.nowandfuture.mod.core.GuiUtilsAccessor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;

@Mixin(GuiUtils.class)
public abstract class MixinGuiUtils implements GuiUtilsAccessor {

    @Shadow(remap = false)
    @Nonnull
    private static ItemStack cachedTooltipStack;

    @Override
    public boolean isEmpty() {
        return cachedTooltipStack.isEmpty();
    }
}
