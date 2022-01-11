package com.nowandfuture.mod.mixins;

import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.client.resources.Language;
import net.minecraft.resources.IResourceManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientLanguageMap.class)
public abstract class MixinClientLanguageMap {

//    @Shadow @Final private static Logger field_239493_a_;
//
//    @Inject(method = "func_239497_a_", at= @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
//    private static void func_239497_a_(IResourceManager resourceManager, List<Language> languageList, CallbackInfoReturnable<ClientLanguageMap> returnable) {
//
//    }
}
