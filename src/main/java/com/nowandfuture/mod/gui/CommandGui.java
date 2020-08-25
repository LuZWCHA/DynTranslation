package com.nowandfuture.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nowandfuture.mod.core.TranslationManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.List;


public class CommandGui extends Screen {

//    private AbstractList<FileEntry> listGui;
    private ExtendedButton loadMapBtn, loadConfigBtn;
    private ExtendedCheckButton enableBtn, spiltBtn, retainOrgBtn;

    public CommandGui(ITextComponent titleIn) {
        super(titleIn);
    }

    @Override
    protected void init() {
//        listGui = new LoadedMapList(minecraft,this.width / 2 - 100, this.height / 4,200,0,16);
//        listGui.children().clear();
//        TranslationManager.INSTANCE.getFileList()
//                .stream().map(file -> {
//            FileEntry entry = new FileEntry();
//            entry.setLoadedFileName(file.getName());
//            return entry;
//        }).forEach(fileEntry -> {
//            listGui.children().add(fileEntry);
//        });

        loadConfigBtn = new ExtendedButton(this.width / 2 - 100, this.height / 4 + 24, 200, 20, I18n.format("gui.dyntranslation.config.load"), new Button.IPressable() {
            @Override
            public void onPress(Button p_onPress_1_) {
                TranslationManager.INSTANCE.loadConfig();
            }
        });

        loadMapBtn = new ExtendedButton(this.width / 2 - 100, this.height / 4 + 48, 200, 20, I18n.format("gui.dyntranslation.map.load"), new Button.IPressable() {
            @Override
            public void onPress(Button p_onPress_1_) {
                TranslationManager.INSTANCE.loadFromJsonMaps();
            }
        });

        enableBtn = new ExtendedCheckButton(this.width / 2 - 100, this.height / 4 + 72, 200, 20, I18n.format("gui.dyntranslation.enable"), TranslationManager.INSTANCE.isEnable());
        spiltBtn = new ExtendedCheckButton(this.width / 2 - 100, this.height / 4 + 96, 200, 20, I18n.format("gui.dyntranslation.word.spilt"), TranslationManager.INSTANCE.isEnableSpiltWords());
        retainOrgBtn = new ExtendedCheckButton(this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.format("gui.dyntranslation.org.retain"), TranslationManager.INSTANCE.isRetainOrg());

        enableBtn.addListener(TranslationManager.INSTANCE::setEnable);
        spiltBtn.addListener(TranslationManager.INSTANCE::setEnableSpiltWords);
        retainOrgBtn.addListener(TranslationManager.INSTANCE::setRetainOrg);

        this.addButton(loadConfigBtn);
        this.addButton(loadMapBtn);
        this.addButton(enableBtn);
        this.addButton(spiltBtn);
        this.addButton(retainOrgBtn);
//        this.children.add(listGui);
    }


    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(-1);
        this.drawCenteredString(font, I18n.format("gui.dyntranslation.setting.title"), width/2,20,16777215);
        super.render(p_render_1_, p_render_2_, p_render_3_);
//        this.listGui.render(p_render_1_, p_render_2_, p_render_3_);
    }

    public static class LoadedMapList extends AbstractList<FileEntry>{

        public LoadedMapList(Minecraft mcIn,int x, int y, int w,int h, int itemHeightIn) {
            super(mcIn, w, h, y, y + h, itemHeightIn);
            x0 = x;
            x1 = x + w;
        }

        @Override
        protected void renderDecorations(int p_renderDecorations_1_, int p_renderDecorations_2_) {
            super.renderDecorations(p_renderDecorations_1_, p_renderDecorations_2_);
        }

        @Override
        public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
            super.render(p_render_1_, p_render_2_, p_render_3_);
        }

        @Override
        protected void renderBackground() {
            super.renderBackground();
        }

        @Override
        public int getRowWidth() {
            return 200;
        }
    }


    public static class FileEntry extends AbstractList.AbstractListEntry<CommandGui.FileEntry>{

        private String loadedFileName;

        public String getLoadedFileName() {
            return loadedFileName;
        }

        @Override
        public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
            Minecraft.getInstance().fontRenderer.drawString(loadedFileName, left, top, 0xCCCCCC);
        }

        @Override
        public void mouseMoved(double xPos, double mouseY) {

        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return false;
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            return false;
        }

        @Override
        public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
            return true;
        }

        @Override
        public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
            return false;
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
            return false;
        }

        @Override
        public boolean changeFocus(boolean p_changeFocus_1_) {
            return false;
        }

        public void setLoadedFileName(String loadedFileName) {
            this.loadedFileName = loadedFileName;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class MyToast implements IToast {
        private final ITextComponent title, subTitle;
        private boolean hasPlayedSound;

        public MyToast(ITextComponent title, ITextComponent subTitle) {
            this.title = title;
            this.subTitle = subTitle;
        }

        public IToast.Visibility draw(ToastGui toastGui, long delta) {
            toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            toastGui.blit(0, 0, 0, 0, 160, 32);
            if (subTitle != null) {
                List<String> list = toastGui.getMinecraft().fontRenderer.listFormattedStringToWidth(subTitle.getFormattedText(), 125);
                int i = 16776960;
                if (list.size() == 1) {
                    toastGui.getMinecraft().fontRenderer.drawString(title.getFormattedText(), 30.0F, 7.0F, i | -16777216);
                    toastGui.getMinecraft().fontRenderer.drawString(subTitle.getFormattedText(), 30.0F, 18.0F, -1);
                } else {
                    int j = 1500;
                    float f = 300.0F;
                    if (delta < 1500L) {
                        int k = MathHelper.floor(MathHelper.clamp((float)(1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                        toastGui.getMinecraft().fontRenderer.drawString(title.getFormattedText(), 30.0F, 11.0F, i | k);
                    } else {
                        int i1 = MathHelper.floor(MathHelper.clamp((float)(delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                        int l = 16 - list.size() * 9 / 2;

                        for(String s : list) {
                            toastGui.getMinecraft().fontRenderer.drawString(s, 30.0F, (float)l, 16777215 | i1);
                            l += 9;
                        }
                    }
                }

                if (!this.hasPlayedSound && delta > 0L) {
                    this.hasPlayedSound = true;
                }

                return delta >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
            } else {
                return IToast.Visibility.HIDE;
            }
        }
    }

}
