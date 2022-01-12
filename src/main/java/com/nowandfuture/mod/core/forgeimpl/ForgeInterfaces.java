package com.nowandfuture.mod.core.forgeimpl;

import com.nowandfuture.mod.core.api.IMixinProfiler;
import com.nowandfuture.mod.core.mcapi.IMinecraftInterfaces;
import com.nowandfuture.mod.gui.CommandGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgeInterfaces {

    public static class ForgeLanguage implements IMinecraftInterfaces.ILanguage{
        @Override
        public String getLanguageCode() {
            return MinecraftUtil.getCurrentLC();
        }

        @Override
        public String getTranslation(String key, Object ... objects) {
            return I18n.format(key, objects);
        }
    }

    public static class ForgeProfiler implements IMinecraftInterfaces.IProfiler{

        @Override
        public IMixinProfiler get() {
            return (IMixinProfiler) Minecraft.getInstance().getProfiler();
        }
    }

    public static class ForgeMinecraftGame implements IMinecraftInterfaces.IMinecraftGame{

        @Override
        public boolean isGamePause() {
            return Minecraft.getInstance().isGamePaused();
        }

        @Override
        public boolean isWorldCreated() {
            return Minecraft.getInstance().world != null;
        }

        @Override
        public void notifyByToast(String title, String content) {
            ToastGui toastgui = Minecraft.getInstance().getToastGui();
            toastgui.clear();
            toastgui.add(new CommandGui.MyToast(new StringTextComponent(title), new TranslationTextComponent(content)));
        }

        @Override
        public void runAtBackground(Runnable runnable) {
            Util.getServerExecutor().execute(runnable);
        }

        @Override
        public boolean isChatScreen(Object screenInstance) {
            return screenInstance instanceof ChatScreen;
        }
    }

    public static class FMLResource implements IMinecraftInterfaces.IResource{

        @Override
        public Path getConfigPath() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public Path getModsPath() {
            return FMLPaths.MODSDIR.get();
        }
    }

}
