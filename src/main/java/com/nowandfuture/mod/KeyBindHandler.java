package com.nowandfuture.mod;

import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.gui.CommandGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindHandler {
    public static KeyBinding keyChatTranslate;
    public static KeyBinding keyOpenMenu;
    public static KeyBinding keyStartRecord;
    public static KeyBinding keyTranslate;

    public KeyBindHandler(){
        keyStartRecord = new KeyBinding("key.dyntranslation.record", GLFW.GLFW_KEY_V, "string.dyntranslation.name");
        keyChatTranslate = new KeyBinding("translation.key.chat.translate", GLFW.GLFW_KEY_Y, "string.dyntranslation.name");
        keyOpenMenu = new KeyBinding("key.dyntranslation.gui.setting.open", GLFW.GLFW_KEY_Z,"string.dyntranslation.name");

        keyTranslate = new KeyBinding("key.dyntranslation.item.translate", GLFW.GLFW_KEY_LEFT_ALT, "string.dyntranslation.name");

        ClientRegistry.registerKeyBinding(keyChatTranslate);
        ClientRegistry.registerKeyBinding(keyOpenMenu);
        ClientRegistry.registerKeyBinding(keyStartRecord);
        ClientRegistry.registerKeyBinding(keyTranslate);
    }

    public static boolean isTranslateKeyDown(){
        return keyTranslate.isKeyDown();
    }

    @SubscribeEvent
    public void handleKeyPressed(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase == TickEvent.Phase.START) {
            TranslationManager manager = TranslationManager.INSTANCE;
            if (keyChatTranslate.isPressed()) {
                manager.setEnableChatTranslate(!manager.isEnableChatTranslate());
            }

            if (keyOpenMenu.isKeyDown() && Minecraft.getInstance().currentScreen == null) {
                Minecraft.getInstance().displayGuiScreen(new CommandGui(new StringTextComponent("command gui")));
            }

            if (keyStartRecord.isPressed()) {
                if (!manager.isRecording())
                    manager.startRecord();
                else
                    manager.endRecord();
            }

        }
    }
}
