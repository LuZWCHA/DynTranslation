package com.nowandfuture.mod;

import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.core.TranslationRes;
import com.nowandfuture.mod.mixins.IMixinGuiNewChat;
import com.nowandfuture.mod.core.forgeimpl.CharConsumer;
import com.nowandfuture.mod.core.forgeimpl.MinecraftUtil;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

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
        TranslationManager manager = TranslationManager.INSTANCE;
        if (event.phase == TickEvent.Phase.START) {
            if (!langCode.equals(curLanguage)) {
                langCode = curLanguage;
                manager.languageChanged();
            }
            manager.submitTask();
        }
    }

    @SubscribeEvent
    public void handleItemTooltipEvent(ItemTooltipEvent tooltipEvent) {
        List<ITextComponent> lines = tooltipEvent.getToolTip();
        TranslationManager manager = TranslationManager.INSTANCE;
        for (int i = 0; i < lines.size(); i++) {
            ITextComponent component = lines.get(i);
            String text = component.getUnformattedComponentText();
            if (manager.isEnable()) {
                if (text != null && !text.isEmpty()) {

                    // the first line (always) is the item names, and make sure the translation key is holding.
                    if(KeyBindHandler.isTranslateKeyDown() && i == 0){
                        if(manager.isWaitingRes(text)){
                            //display the waiting string...
                            lines.add(++i, ITextComponent.func_244388_a("..."));
                        }else {
                            //display the result.
                            Optional<List<String>> res = manager.getNMTTranslation(text, manager.getDisplayNumber());
                            // TODO: 2022/1/16 map the optional ...

                            if (res.isPresent()) {
                                for (String trans :
                                        res.get()) {
                                    lines.add(++i, ITextComponent.func_244388_a(trans));
                                }

                            }
                        }

                    }else {
                        TranslationRes res = manager.translate(text);
                        text = res.text;
                        //restore the first line's information
                        if (res.isTranslated) {
                            lines.set(i, ITextComponent.func_244388_a(text));
                        }
                    }
                } else {
                    //This is only for test, if users can translate the .lang, they not need to use this mod to translate
                    if (component instanceof TranslationTextComponent) {
                        String key = ((TranslationTextComponent) component).getKey();
                        if (I18n.hasKey(key)) {
                            String localTranslate = I18n.format(key);
                            if(KeyBindHandler.isTranslateKeyDown() && i == 0){
                                if(manager.isWaitingRes(localTranslate)){
                                    lines.add(++i, ITextComponent.func_244388_a("..."));
                                }else {
                                    Optional<List<String>> res = manager.getNMTTranslation(localTranslate, manager.getDisplayNumber());
                                    if (res.isPresent()) {
                                        for (String trans :
                                                res.get()) {
                                            lines.add(++i, ITextComponent.func_244388_a(trans));
                                        }
                                    }
                                }

                            }else {
                                TranslationRes res = manager.translate(localTranslate);
                                localTranslate = res.text;
                                //restore the first line's information
                                if (res.isTranslated) {
                                    lines.set(i, ITextComponent.func_244388_a(localTranslate));
                                }
                            }
                        }
                    } else {
                        List<ITextComponent> siblings = component.getSiblings();
                        for (int j = 0; j < siblings.size(); j++) {
                            ITextComponent trTc = siblings.get(j);
                            //This is only for test, if users can translate the .lang, they not need to use this mod to translate
                            if (trTc instanceof TranslationTextComponent) {
                                TranslationTextComponent translationTextComponent = ((TranslationTextComponent) trTc);
                                String key = translationTextComponent.getKey();
                                Object[] formatArgs = translationTextComponent.getFormatArgs();

                                if (I18n.hasKey(key)) {
                                    String localTranslate = translationTextComponent.func_230532_e_().getString();
                                    boolean keyDown = KeyBindHandler.isTranslateKeyDown();
                                    if(keyDown && i + j == 0){
                                        if(manager.isWaitingRes(localTranslate)){
                                            lines.add(++i, ITextComponent.func_244388_a("..."));
                                        }else {
                                            Optional<List<String>> res = manager.getNMTTranslation(localTranslate, manager.getDisplayNumber());
                                            if (res.isPresent()) {
                                                for (String trans :
                                                        res.get()) {
                                                    IFormattableTextComponent textComponent = new StringTextComponent(trans);
                                                    textComponent.func_230530_a_(trTc.getStyle());
                                                    lines.add(++j, textComponent);
                                                }
                                            }
                                        }

                                    }else {
                                        TranslationRes res = manager.translate(localTranslate);
                                        localTranslate = res.text;
                                        //restore the first line's information
                                        if (res.isTranslated) {
                                            IFormattableTextComponent textComponent = new StringTextComponent(localTranslate);
                                            textComponent.func_230530_a_(trTc.getStyle());
                                            siblings.set(j, textComponent);
                                        }
                                    }

                                }
                            } else {
                                TranslationRes res = manager.translate(trTc.getUnformattedComponentText());
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
                    //get the processor which contain the text strings to render
                    ChatLine<IReorderingProcessor> chatLine = list.get(i);

                    if (!(chatLine instanceof TranslationChatLine)) {
                        list.set(i, new TranslationChatLine(chatLine));
                        chatLine = list.get(i);
                    }

                    if (manager.isEnableChatTranslate()) {
                        CharConsumer charConsumer = new CharConsumer();

                        if (chatLine instanceof TranslationChatLine) {
                            ((TranslationChatLine) chatLine).getOrgText().accept(charConsumer);
                            Optional<String> res = manager.translateChatText(charConsumer.getBuilder().toString());
                            res.ifPresent(((TranslationChatLine) chatLine)::setTranslation);
                        } else {
                            //TODO
                        }

                    }
                }
            }
        }
    }

//    @SubscribeEvent
//    public void handleChatEvent(ClientChatReceivedEvent event) {
//        ITextComponent component = event.getMessage();
//
//        String text = component.getUnformattedComponentText();
//        if (TranslationManager.INSTANCE.isEnableChatTranslate()) {
//            Optional<String> res = TranslationManager.INSTANCE.translateChatText(text);
//            res.ifPresent(s -> {
//                //todo
//            });
//        }
//    }

    @SubscribeEvent(priority = LOWEST)
    public void handleUnloadWorld(WorldEvent.Unload unload) {
        if (unload.getWorld().isRemote()) {
            TranslationManager.INSTANCE.stopTranslateThread();
        }
    }

    //TranslationChatLine is a wrapper to wrap the original chat line;
    //and the translation res will be filled after translator finished the translate task.
    public static class TranslationChatLine extends ChatLine<IReorderingProcessor> {
        private final ChatLine<IReorderingProcessor> orgChatLine;
        private String translationText = Strings.EMPTY;
        private ChatLine<IReorderingProcessor> translation;


        //func_238169_a_() is getStringComponent()
        public TranslationChatLine(ChatLine<IReorderingProcessor> orgChatLine) {
            super(0, orgChatLine.func_238169_a_(), 0);
            this.orgChatLine = orgChatLine;
        }

        public ChatLine<IReorderingProcessor> getOrgChatLine() {
            return orgChatLine;
        }

        public void setTranslationLine(ChatLine<IReorderingProcessor> translation) {
            this.translation = translation;
        }

        public void setTranslation(String text){
            if(!translationText.equals(text)) {
                translationText = text;
                IReorderingProcessor ire = IReorderingProcessor.func_242239_a(text, Style.field_240709_b_);
                translation = new ChatLine<>(orgChatLine.getUpdatedCounter(), ire, orgChatLine.getChatLineID());
            }
        }

        public ChatLine<IReorderingProcessor> getTranslation() {
            return translation;
        }

        public boolean hasTranslation(){
            return translation != null;
        }

        //func_238169_a_() is getStringComponent()
        @NotNull
        @Override
        public IReorderingProcessor func_238169_a_() {
            if(hasTranslation() && TranslationManager.INSTANCE.isEnable() && TranslationManager.INSTANCE.isEnableChatTranslate())
                return TranslationManager.INSTANCE.isRetainOrg() ?
                        IReorderingProcessor.func_242234_a(orgChatLine.func_238169_a_(),translation.func_238169_a_()):
                        translation.func_238169_a_();
            else
                return orgChatLine.func_238169_a_();
        }

        public IReorderingProcessor getOrgText(){
            return orgChatLine.func_238169_a_();
        }

        public IReorderingProcessor getTranslationText(){
            return translation != null ? translation.func_238169_a_(): null;
        }

        @Override
        public int getChatLineID() {
            return orgChatLine.getChatLineID();
        }

        @Override
        public int getUpdatedCounter() {
            return orgChatLine.getUpdatedCounter();
        }
    }
}