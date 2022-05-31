package com.nowandfuture.mod.core.forgeimpl.vanillaopt;

import joptsimple.internal.Strings;
import net.minecraftforge.fml.ModContainer;

public class PacketCapture {

    public static String getNearestInvokeMod(int parentLevel) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        for (int i = 2; i < elements.length && parentLevel > 0; i++) {
            String clazzName = elements[i].getClassName();
            if (!clazzName.equals(PacketCapture.class.getName()) && !clazzName.equals(Thread.class.getName())) {
                ModContainer mod = ClazzOfModCache.INSTANCE.getModByClassName(clazzName);
                if (mod != null) {
                    if (!mod.getModId().equals("forge") && !mod.getModId().equals("minecraft")) {
                        return mod.getModInfo().getModId();
                    }
                }
            }
            parentLevel --;
        }

        return Strings.EMPTY;
    }

}
