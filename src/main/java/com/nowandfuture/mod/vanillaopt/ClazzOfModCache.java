package com.nowandfuture.mod.vanillaopt;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum ClazzOfModCache {

    INSTANCE;

    private final Map<String, ModContainer> class2Mod;
    private final Map<String, ModContainer> nameSpace2Mod;

    ClazzOfModCache() {
        class2Mod = new HashMap<>();
        nameSpace2Mod = new HashMap<>();

    }

    public void init() {
        long start = System.currentTimeMillis();

        final Map<IModFileInfo, String> modFileInfoStringMap = new HashMap<>();

        ModList.get().forEachModContainer(new BiConsumer<String, ModContainer>() {
            @Override
            public void accept(String s, ModContainer modContainer) {
                IModFileInfo file = modContainer.getModInfo().getOwningFile();

                nameSpace2Mod.put(modContainer.getNamespace(), modContainer);
                modFileInfoStringMap.put(file, s);
            }
        });

        ModList.get().forEachModFile(modFile -> {
            IModFileInfo file = modFile.getModFileInfo();
            String modId = modFileInfoStringMap.get(file);
            modFile.getScanResult().getClasses().forEach(new Consumer<ModFileScanData.ClassData>() {
                Field field = null;

                @Override
                public void accept(ModFileScanData.ClassData classData) {
                    try {
                        if (field == null) {
                            field = ModFileScanData.ClassData.class.getDeclaredField("clazz");
                            field.setAccessible(true);
                        }
                        Type typeClazz = (Type) field.get(classData);
                        String clazzDes = typeClazz.getInternalName();
                        System.out.println(modId + ": " + clazzDes);
                        ModList.get().getModContainerById(modId).ifPresent(new Consumer<ModContainer>() {
                            @Override
                            public void accept(ModContainer modContainer) {
                                class2Mod.put(clazzDes.replace('/', '.'), modContainer);
                            }
                        });

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        long time = (System.currentTimeMillis() - start);

//        class2Mod.forEach((s, modContainer) -> System.out.println(s + ": " + modContainer.getModId()));

        System.out.println("Take : " + time + "ms");

    }

    @Nullable
    public ModContainer getModByClassName(String name) {
        return class2Mod.get(name);
    }

    @Nullable
    public ModContainer getModByNameSpace(String nameSpace){
        return nameSpace2Mod.get(nameSpace);
    }

}
