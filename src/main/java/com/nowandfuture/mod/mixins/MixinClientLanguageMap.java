package com.nowandfuture.mod.mixins;

import com.nowandfuture.mod.vanillaopt.ClazzOfModCache;
import com.nowandfuture.mod.vanillaopt.ExtraLanguageMap;
import com.nowandfuture.mod.vanillaopt.PacketCapture;
import joptsimple.internal.Strings;
import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.client.resources.Language;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.text.LanguageMap;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;

@Mixin(ClientLanguageMap.class)
public abstract class MixinClientLanguageMap {

    @Shadow @Final private static Logger field_239493_a_;

    @Shadow @Final private Map<String, String> field_239495_c_;

    private static Set<String> conflictLanguageKeys = new TreeSet<>();
    private static Map<String, String> recordId = new HashMap<>();
    private static Map<String, String> localMap = null;

    @Inject(method = "func_239497_a_", at= @At(value = "RETURN", shift = At.Shift.BEFORE))
    private static void inject_func_239497_a_(IResourceManager resourceManager, List<Language> languageList, CallbackInfoReturnable<ClientLanguageMap> returnable) {

        if(localMap != null) {
            for (String conflictLanguageKey : conflictLanguageKeys) {
                String modId = recordId.get(conflictLanguageKey);
                //remove the conflict and put it into extraLanguageMap.
                ExtraLanguageMap.INSTANCE.put(conflictLanguageKey, localMap.get(conflictLanguageKey), modId);
            }

            recordId.clear();
            localMap = null;
        }

    }

    @Inject(method = "func_230503_a_", at=@At("HEAD"), cancellable = true)
    private void inject_func_230503_a_(String text, CallbackInfoReturnable<String> cir){
        if(this.field_239495_c_.containsKey(text) && !conflictLanguageKeys.contains(text)) {
            cir.setReturnValue(this.field_239495_c_.get(text));
        }else{

            String id = PacketCapture.getNearestInvokeMod(16);

            if(!id.isEmpty()) {
                cir.setReturnValue(ExtraLanguageMap.INSTANCE.get(text, id));
            }else{
                cir.setReturnValue(text);
            }
        }

    }



    @Inject(method = "func_239498_a_", at=@At("HEAD"), cancellable = true)
    private static void inject_func_239498_a_(List<IResource> resources, Map<String, String> map, CallbackInfo ci) {
        //touch the map instance.
        if(localMap == null) localMap = map;

        for(IResource iresource : resources) {

            try (InputStream inputstream = iresource.getInputStream()) {
                LanguageMap.func_240593_a_(inputstream, (s, s2) -> {
                    String nameSpace = iresource.getLocation().getNamespace();
                    String modId = ClazzOfModCache.INSTANCE.getModByNameSpace(nameSpace).getModId();

                    if(map.containsKey(s)){
                        conflictLanguageKeys.add(s);
                        ExtraLanguageMap.INSTANCE.put(s, s2, modId);
                    }else{
                        recordId.put(s, modId);
                        map.put(s, s2);
                    }
                });
            } catch (IOException ioexception) {
                field_239493_a_.warn("Failed to load translations from {}", iresource, ioexception);
            }
        }

        ci.cancel();
    }
}
