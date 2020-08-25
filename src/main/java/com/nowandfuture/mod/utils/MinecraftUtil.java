package com.nowandfuture.mod.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.LanguageManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;

public class MinecraftUtil {

    @OnlyIn(Dist.CLIENT)
    public static LanguageManager getLanguageManager(){
        return Minecraft.getInstance().getLanguageManager();
    }

    @OnlyIn(Dist.CLIENT)
    public static String getCurrentLC(){
        return getLanguageManager().getCurrentLanguage().getCode();
    }

    public static String copyFormat(String text){
        final StringBuilder builder = new StringBuilder();
        for(int i = 0;i < text.length();++i) {
            char c0 = text.charAt(i);
            if (c0 == '§' && i + 1 < text.length()) {
                builder.append(c0);
                builder.append(text.charAt(++i));
            }
        }
        return builder.toString();
    }

    public static boolean isContainFormat167(String text){
        return text.contains("§");
    }


    @Nullable
    public static String removeFormat(@Nullable String text){
        if(Objects.isNull(text)) return null;

        final StringBuilder builder = new StringBuilder();
        for(int i = 0;i < text.length();++i) {
            char c0 = text.charAt(i);
            if (c0 == '§' && i + 1 < text.length()) {
                ++i;
            }else{
                builder.append(c0);
            }
        }
        return builder.toString();
    }
}
