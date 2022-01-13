package com.nowandfuture.translation.mixins;

import com.nowandfuture.translation.core.GuiUtilsAccessor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiUtils.class)
public abstract class MixinGuiUtils implements GuiUtilsAccessor {
    @Shadow(remap = false)
    private static ItemStack cachedTooltipStack;

    @Override
    public boolean isEmpty() {
        return cachedTooltipStack.isEmpty();
    }
}
