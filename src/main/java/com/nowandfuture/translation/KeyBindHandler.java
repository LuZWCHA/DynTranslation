package com.nowandfuture.translation;

import com.nowandfuture.translation.core.TranslationManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class KeyBindHandler {
    public static KeyBinding keyChatTranslate;

    public KeyBindHandler(){
        keyChatTranslate = new KeyBinding("translation.key.chat.translate", Keyboard.KEY_HOME,"translation.key.translate");
        ClientRegistry.registerKeyBinding(keyChatTranslate);
    }

    @SubscribeEvent
    public void handleKeyDown(TickEvent.ClientTickEvent tickEvent){
        if(keyChatTranslate.isKeyDown()){
            TranslationManager.INSTANCE.setEnableChatTranslate(!TranslationManager.INSTANCE.isEnableChatTranslate());
        }
    }
}
