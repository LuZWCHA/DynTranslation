package com.nowandfuture.mod;

import com.nowandfuture.mod.core.TranslationChatLine;
import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.core.TranslationRes;
import com.nowandfuture.mod.mixins.IMixinGuiNewChat;
import com.nowandfuture.mod.utils.MinecraftUtil;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

public class RenderHandler {
    private String langCode = Strings.EMPTY;
    private static boolean isTooltipRendering = false;

    @SubscribeEvent
    public void handleGuiOpen(GuiOpenEvent event) {
        TranslationManager.INSTANCE.setCurrentGui(event.getGui());
    }

    @SubscribeEvent
    public void handleClientTick(TickEvent.ClientTickEvent event){
        String curLanguage = MinecraftUtil.getCurrentLC();
        if(!langCode.equals(curLanguage)){
            langCode = curLanguage;
            TranslationManager.INSTANCE.languageChanged();
        }
        TranslationManager.INSTANCE.submitTask();
    }

    @SubscribeEvent
    public void handleItemTooltipEvent(ItemTooltipEvent tooltipEvent){
        List<ITextComponent> lines = tooltipEvent.getToolTip();

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getString();
            if(TranslationManager.INSTANCE.isEnable()) {
                TranslationRes res = TranslationManager.INSTANCE.translate(text);
                text = res.text;
                lines.set(i,new StringTextComponent(text));
            }
        }
    }

    @SubscribeEvent
    public void handleRender(TickEvent.RenderTickEvent event){
        if(event.phase == TickEvent.Phase.START){
            List<ChatLine> list =
                    ((IMixinGuiNewChat)(Minecraft.getInstance().ingameGUI.getChatGUI()))
                            .getDrawnChatLines();
            TranslationManager manager = TranslationManager.INSTANCE;

            if(manager.isEnable()) {
                for (int i = 0; i < list.size(); i++) {
                    ChatLine chatLine = list.get(i);

                    if(!(chatLine instanceof TranslationChatLine)){
                        list.set(i, new TranslationChatLine(chatLine));
                        chatLine = list.get(i);
                    }

                    if (manager.isEnableChatTranslate()) {

                        if(chatLine instanceof TranslationChatLine){
                            Optional<String> res = TranslationManager.INSTANCE.translateChatText(((TranslationChatLine) chatLine).getOrgText().getString());
                            res.ifPresent(((TranslationChatLine) chatLine)::setTranslation);
                        }else{
                            //todo
                        }

                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleChatEvent(ClientChatReceivedEvent event){
        ITextComponent component = event.getMessage();

        String text = component.getUnformattedComponentText();
        if(TranslationManager.INSTANCE.isEnableChatTranslate()){
            Optional<String> res = TranslationManager.INSTANCE.translateChatText(text);
            res.ifPresent(s -> {
                //todo
            });
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void handleUnloadWorld(WorldEvent.Unload unload){
        if(unload.getWorld().isRemote()){
            TranslationManager.INSTANCE.stopTranslateThread();
        }
    }
}