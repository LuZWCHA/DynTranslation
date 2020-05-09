package com.nowandfuture.translation;

import com.nowandfuture.translation.core.TranslationChatLine;
import com.nowandfuture.translation.core.TranslationManager;
import com.nowandfuture.translation.core.TranslationRes;
import com.nowandfuture.translation.mixins.IMixinGuiNewChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class RenderHandler {
    private String langCode = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();

    @SubscribeEvent
    public void handleGuiOpen(GuiOpenEvent event) {
        TranslationManager.INSTANCE.setCurrentGui(event.getGui());
    }

    @SubscribeEvent
    public void handleClientTick(TickEvent.ClientTickEvent tickEvent){
        String curlang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        if(!langCode.equals(curlang)){
            langCode = curlang;
            TranslationManager.INSTANCE.languageChanged();
        }
        TranslationManager.INSTANCE.submitTask();
    }

    @SubscribeEvent
    public void handleItemTooltipEvent(ItemTooltipEvent tooltipEvent){
        List<String> lines = tooltipEvent.getToolTip();
//        tooltipEvent.getItemStack().getItem().getCreatorModId()

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            if(TranslationManager.INSTANCE.isEnable()) {
                TranslationRes res = TranslationManager.INSTANCE.translate(text);
                text = res.text;
                lines.set(i,text);
            }
        }
    }

    @SubscribeEvent
    public void handleRender(TickEvent.RenderTickEvent event){
        if(event.phase == TickEvent.Phase.START){
            List<ChatLine> list =
                    ((IMixinGuiNewChat)(Minecraft.getMinecraft().ingameGUI.getChatGUI()))
                            .getDrawnChatLines();
            TranslationManager manager = TranslationManager.INSTANCE;

            if(manager.isEnable()) {
                for (int i = 0; i < list.size(); i++) {
                    ChatLine chatLine = list.get(i);
                    if (manager.isEnableChatTranslate()) {
                        if (!(chatLine instanceof TranslationChatLine)) {
                            String translatedText = manager.getNetworkTranslate(chatLine.getChatComponent().getUnformattedText());
                            if (translatedText != null)
                                list.set(i, new TranslationChatLine(chatLine, chatLine.getUpdatedCounter(), new TextComponentString(translatedText), 0));
                            else
                                manager.translateChatText(chatLine.getChatComponent().getUnformattedText());
                        }
                    } else {
                        if (chatLine instanceof TranslationChatLine) {
                            list.set(i, ((TranslationChatLine) chatLine).getOrgChatLine());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleChatEvent(ClientChatReceivedEvent event){
        ITextComponent component = event.getMessage();
        String text = component.getUnformattedText();
        if(TranslationManager.INSTANCE.isEnableChatTranslate()){
            text = TranslationManager.INSTANCE.translateChatText(text);
//            if(TranslationManager.INSTANCE.isRetainOrg())
//                component.appendText("\n->" + text);
//            else {
//                component = new TextComponentString(text);
//            }
        }

        event.setMessage(component);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleUnloadWorld(WorldEvent.Unload unload){
        if(unload.getWorld().isRemote){
            TranslationManager.INSTANCE.stopTranslateThread();
        }
    }
}