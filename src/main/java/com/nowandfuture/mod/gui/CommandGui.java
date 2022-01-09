package com.nowandfuture.mod.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nowandfuture.mod.core.TranslationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.List;


public class CommandGui extends Screen {

//    private AbstractList<FileEntry> listGui;
    private ExtendedButton loadMapBtn, loadConfigBtn;
    private ExtendedCheckButton enableBtn, spiltBtn, retainOrgBtn;

    public CommandGui(ITextComponent titleIn) {
        super(titleIn);
    }

    //init()
    @Override
    protected void func_231160_c_() {
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

        loadConfigBtn = new ExtendedButton(this.field_230708_k_ / 2 - 100, this.field_230709_l_ / 4 + 24, 200, 20, new StringTextComponent(I18n.format("gui.dyntranslation.config.load")), new Button.IPressable() {
            @Override
            public void onPress(Button p_onPress_1_) {
                TranslationManager.INSTANCE.loadConfig();
            }
        });

        loadMapBtn = new ExtendedButton(this.field_230708_k_ / 2 - 100, this.field_230709_l_ / 4 + 48, 200, 20, new StringTextComponent(I18n.format("gui.dyntranslation.map.load")), new Button.IPressable() {
            @Override
            public void onPress(Button p_onPress_1_) {
                TranslationManager.INSTANCE.loadFromJsonMaps();
            }
        });

        enableBtn = new ExtendedCheckButton(this.field_230708_k_ / 2 - 100, this.field_230709_l_ / 4 + 72, 200, 20, ITextComponent.func_244388_a(I18n.format("gui.dyntranslation.enable")), TranslationManager.INSTANCE.isEnable());
        spiltBtn = new ExtendedCheckButton(this.field_230708_k_ / 2 - 100, this.field_230709_l_ / 4 + 96, 200, 20, ITextComponent.func_244388_a(I18n.format("gui.dyntranslation.word.spilt")), TranslationManager.INSTANCE.isEnableSpiltWords());
        retainOrgBtn = new ExtendedCheckButton(this.field_230708_k_ / 2 - 100, this.field_230709_l_ / 4 + 120, 200, 20, ITextComponent.func_244388_a(I18n.format("gui.dyntranslation.org.retain")), TranslationManager.INSTANCE.isRetainOrg());

        enableBtn.addListener(TranslationManager.INSTANCE::setEnable);
        spiltBtn.addListener(TranslationManager.INSTANCE::setEnableSpiltWords);
        retainOrgBtn.addListener(TranslationManager.INSTANCE::setRetainOrg);

        //addButton
        this.func_230480_a_(loadConfigBtn);
        this.func_230480_a_(loadMapBtn);
        this.func_230480_a_(enableBtn);
        this.func_230480_a_(spiltBtn);
        this.func_230480_a_(retainOrgBtn);
//        this.children.add(listGui);
    }


    //render()
    @Override
    public void func_230430_a_(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.func_238651_a_(matrixStack, -1);//renderBackground()
        //drawCenteredString,frontRender,width
        func_238471_a_(matrixStack,field_230712_o_, I18n.format("gui.dyntranslation.setting.title"), field_230708_k_/2,20,16777215);
        super.func_230430_a_(matrixStack,p_render_1_, p_render_2_, p_render_3_);
//        this.listGui.render(p_render_1_, p_render_2_, p_render_3_);
    }

    public static class LoadedMapList extends AbstractList<FileEntry>{

        public LoadedMapList(Minecraft mcIn,int x, int y, int w,int h, int itemHeightIn) {
            super(mcIn, w, h, y, y + h, itemHeightIn);
            this.field_230675_l_= x;
            this.field_230674_k_ = x + w;
        }

//        getRowWidth
        @Override
        public int func_230949_c_() {
            return 200;
        }
    }


    public static class FileEntry extends AbstractList.AbstractListEntry<FileEntry>{

        private String loadedFileName;

        public String getLoadedFileName() {
            return loadedFileName;
        }


//        @Override
//        public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
//            Minecraft.getInstance().fontRenderer.drawString(loadedFileName, left, top, 0xCCCCCC);
//        }


        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return false;
        }


        public void setLoadedFileName(String loadedFileName) {
            this.loadedFileName = loadedFileName;
        }

        @Override
        public void func_230432_a_(MatrixStack matrixStack, int left, int top, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_) {
            Minecraft.getInstance().fontRenderer.func_243248_b(matrixStack, ITextComponent.func_244388_a(loadedFileName), left, top, 0xCCCCCC);
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

        @Override
        public Visibility func_230444_a_(MatrixStack matrixStack, ToastGui toastGui, long delta) {
            toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

            toastGui.func_238474_b_(matrixStack,0, 0, 0, 0, 160, 32);
            if (subTitle != null) {
                List<IReorderingProcessor> list = toastGui.getMinecraft().fontRenderer.func_238425_b_(subTitle.func_230531_f_(), 125);
                int i = 16776960;
                TranslationManager.INSTANCE.getDoTranslate().set(false);

                if (list.size() == 1) {
                    toastGui.getMinecraft().fontRenderer.func_243248_b(matrixStack, title.func_230531_f_(), 30.0F, 7.0F, i | -16777216);
                    toastGui.getMinecraft().fontRenderer.func_243248_b(matrixStack, subTitle.func_230531_f_(), 30.0F, 18.0F, -1);
                } else {
                    int j = 1500;
                    float f = 300.0F;
                    if (delta < 1500L) {
                        int k = MathHelper.floor(MathHelper.clamp((float)(1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                        toastGui.getMinecraft().fontRenderer.func_243248_b(matrixStack, title.func_230531_f_(), 30.0F, 11.0F, i | k);
                    } else {
                        int i1 = MathHelper.floor(MathHelper.clamp((float)(delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                        int l = 16 - list.size() * 9 / 2;

                        for(IReorderingProcessor s : list) {
                            toastGui.getMinecraft().fontRenderer.func_238422_b_(matrixStack, s, 30.0F, (float)l, 16777215 | i1);
                            l += 9;
                        }
                    }
                }

                if (!this.hasPlayedSound && delta > 0L) {
                    this.hasPlayedSound = true;
                }

                TranslationManager.INSTANCE.getDoTranslate().set(true);

                return delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
            } else {
                return Visibility.HIDE;
            }
        }
    }

}
