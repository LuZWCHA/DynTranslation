package com.nowandfuture.mod;

import com.nowandfuture.mod.core.TranslationChatLine;
import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.core.TranslationRes;
import com.nowandfuture.mod.mixins.IMixinGuiNewChat;
import com.nowandfuture.mod.utils.CharConsumer;
import com.nowandfuture.mod.utils.MinecraftUtil;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

public class RenderHandler {
    private String langCode = Strings.EMPTY;
    private static boolean isTooltipRendering = false;

    @SubscribeEvent
    public void handleGuiOpen(GuiOpenEvent event) {
        TranslationManager.INSTANCE.setCurrentGui(event.getGui());
    }

    @SubscribeEvent
    public void handleClientTick(TickEvent.ClientTickEvent event) {
        String curLanguage = MinecraftUtil.getCurrentLC();
        if (event.phase == TickEvent.Phase.START) {
            if (!langCode.equals(curLanguage)) {
                langCode = curLanguage;
                TranslationManager.INSTANCE.languageChanged();
            }
            TranslationManager.INSTANCE.submitTask();
        }
    }

    @SubscribeEvent
    public void handleItemTooltipEvent(ItemTooltipEvent tooltipEvent) {
        List<ITextComponent> lines = tooltipEvent.getToolTip();

        for (int i = 0; i < lines.size(); i++) {
            ITextComponent component = lines.get(i);
            String text = component.getUnformattedComponentText();
            if (TranslationManager.INSTANCE.isEnable()) {
                if (!text.isEmpty()) {
                    TranslationRes res = TranslationManager.INSTANCE.translate(text);
                    text = res.text;
                    //restore the first line's information
                    if (res.isTranslated)
                        lines.set(i, ITextComponent.func_244388_a(text));
                } else {
                    //This is only for test, if users can translate the .lang, they not need to use this mod to translate
                    if (component instanceof TranslationTextComponent) {
                        String key = ((TranslationTextComponent) component).getKey();
                        if (I18n.hasKey(key)) {
                            String localTranslate = I18n.format(key);
                            TranslationRes res = TranslationManager.INSTANCE.translate(localTranslate);
                            text = res.text;
                            if (res.isTranslated) {
                                IFormattableTextComponent textComponent = new StringTextComponent(text);
                                textComponent.func_230530_a_(component.getStyle());
                                lines.set(i, textComponent);
                            }
                        }
                    } else {
                        List<ITextComponent> siblings = component.getSiblings();
                        for (int j = 0; j < siblings.size(); j++) {
                            ITextComponent trTc = siblings.get(j);
                            //This is only for test, if users can translate the .lang, they not need to use this mod to translate
                            if (trTc instanceof TranslationTextComponent) {
                                String key = ((TranslationTextComponent) trTc).getKey();
                                if (I18n.hasKey(key)) {
                                    String localTranslate = I18n.format(key);
                                    TranslationRes res = TranslationManager.INSTANCE.translate(localTranslate);
                                    text = res.text;
                                    if (res.isTranslated) {
                                        IFormattableTextComponent textComponent = new StringTextComponent(text);
                                        textComponent.func_230530_a_(trTc.getStyle());
                                        siblings.set(j, textComponent);
                                    }
                                }
                            } else {
                                TranslationRes res = TranslationManager.INSTANCE.translate(trTc.getUnformattedComponentText());
                                text = res.text;
                                if (res.isTranslated) {
                                    IFormattableTextComponent textComponent = new StringTextComponent(text);
                                    textComponent.func_230530_a_(trTc.getStyle());
                                    siblings.set(j, textComponent);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleRender(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            List<ChatLine<IReorderingProcessor>> list =
                    ((IMixinGuiNewChat) (Minecraft.getInstance().ingameGUI.getChatGUI()))
                            .getDrawnChatLines();
            TranslationManager manager = TranslationManager.INSTANCE;

            if (manager.isEnable()) {
                for (int i = 0; i < list.size(); i++) {
                    ChatLine<IReorderingProcessor> chatLine = list.get(i);

                    if (!(chatLine instanceof TranslationChatLine)) {
                        list.set(i, new TranslationChatLine(chatLine));
                        chatLine = list.get(i);
                    }

                    if (manager.isEnableChatTranslate()) {
                        CharConsumer charConsumer = new CharConsumer();

                        if (chatLine instanceof TranslationChatLine) {
                            ((TranslationChatLine) chatLine).getOrgText().accept(charConsumer);
                            Optional<String> res = TranslationManager.INSTANCE.translateChatText(charConsumer.getBuilder().toString());
                            res.ifPresent(((TranslationChatLine) chatLine)::setTranslation);
                        } else {
                            //todo
                        }

                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleChatEvent(ClientChatReceivedEvent event) {
        ITextComponent component = event.getMessage();

        String text = component.getUnformattedComponentText();
        if (TranslationManager.INSTANCE.isEnableChatTranslate()) {
            Optional<String> res = TranslationManager.INSTANCE.translateChatText(text);
            res.ifPresent(s -> {
                //todo
            });
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void handleUnloadWorld(WorldEvent.Unload unload) {
        if (unload.getWorld().isRemote()) {
            TranslationManager.INSTANCE.stopTranslateThread();
        }
    }
}