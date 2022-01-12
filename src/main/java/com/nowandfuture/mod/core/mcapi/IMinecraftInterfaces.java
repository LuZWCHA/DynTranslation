package com.nowandfuture.mod.core.mcapi;

import com.nowandfuture.mod.core.api.IMixinProfiler;

import java.nio.file.Path;

public interface IMinecraftInterfaces {

    //language
    interface ILanguage{
        String getLanguageCode();
        String getTranslation(String key, Object... objects);
    }

    interface IProfiler{
        IMixinProfiler get();
    }

    interface IMinecraftGame{
        boolean isGamePause();
        boolean isWorldCreated();
        void notifyByToast(String title, String content);
        void runAtBackground(Runnable runnable);
        boolean isChatScreen(Object screenInstance);
    }

    interface IResource{
        Path getConfigPath();
        Path getModsPath();
    }

}


