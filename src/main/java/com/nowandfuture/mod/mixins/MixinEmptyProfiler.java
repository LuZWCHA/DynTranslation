package com.nowandfuture.mod.mixins;

import com.nowandfuture.mod.core.IMixinProfiler;
import joptsimple.internal.Strings;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EmptyProfiler.class)
public abstract class MixinEmptyProfiler implements IMixinProfiler {
    private String section;

    @Inject(
            method = "startSection(Ljava/lang/String;)V",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void inject_startSection(String name, CallbackInfo callbackInfo){
        section = name;
    }

    @Inject(
            method = "endSection",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void inject_endSection(CallbackInfo callbackInfo){
        section = null;
    }

    @Override
    public String getCurrentSection() {
        return section == null ? Strings.EMPTY : section;
    }
}
