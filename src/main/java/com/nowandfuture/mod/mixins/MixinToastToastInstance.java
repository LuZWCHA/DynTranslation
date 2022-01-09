package com.nowandfuture.mod.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.core.TranslationManager;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.client.gui.toasts.ToastGui$ToastInstance")
public abstract class MixinToastToastInstance<T extends IToast> {

    @Shadow @Final private T toast;

    @Inject(method = "render", at=@At("HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void inject_head_render(int p_193684_1_, int p_193684_2_, MatrixStack p_193684_3_, CallbackInfoReturnable<Boolean> returnable) {
        final Class clazz = this.toast.getClass();
        TranslationManager.INSTANCE.getToastClazz().set(clazz);
    }

    @Inject(method = "render", at=@At("RETURN"))
    public void inject_return_render(int p_193684_1_, int p_193684_2_, MatrixStack p_193684_3_, CallbackInfoReturnable<Boolean> returnable) {
        TranslationManager.INSTANCE.getToastClazz().set(null);
    }
}
