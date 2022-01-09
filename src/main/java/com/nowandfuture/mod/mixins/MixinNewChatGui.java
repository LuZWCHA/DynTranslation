package com.nowandfuture.mod.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.core.TranslationManager;
import net.minecraft.client.gui.NewChatGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NewChatGui.class)
public abstract class MixinNewChatGui {

    @Inject(method = "func_238492_a_", at=@At("HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void inject_head_func_238492_a_(MatrixStack p_238492_1_, int p_238492_2_, CallbackInfo callbackInfo) {
        TranslationManager.INSTANCE.getDoTranslate().set(false);
    }

    @Inject(method = "func_238492_a_", at=@At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void inject_return_func_238492_a_(MatrixStack p_238492_1_, int p_238492_2_, CallbackInfo callbackInfo) {
        TranslationManager.INSTANCE.getDoTranslate().set(true);
    }

}
