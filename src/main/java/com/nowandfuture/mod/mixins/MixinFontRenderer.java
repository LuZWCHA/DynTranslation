package com.nowandfuture.mod.mixins;

import com.nowandfuture.mod.core.ControlChars;
import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.core.TranslationRes;
import com.nowandfuture.mod.utils.CharConsumer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow
    public abstract int getStringWidth(String text);

    @Shadow
    @Final
    public int FONT_HEIGHT;

    private final Vector3f v = new Vector3f();

//    @Inject(
//            method = "func_238411_a_(Ljava/lang/String;FFIZLnet/minecraft/util/math/vector/Matrix4f;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ZIIZ)I",
//            at = @At("HEAD"),
//            locals = LocalCapture.CAPTURE_FAILSOFT,
//            cancellable = true
//    )
//    private void inject_renderString(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean transparentIn, int colorBackgroundIn, int packedLight,boolean isbid, CallbackInfoReturnable<Integer> cir){
//        if(text == null) return;
//
//        ControlChars controlChars = ControlChars.EMPTY;
//        String orgText = removeFormat(text);
//        if(TranslationManager.INSTANCE.isEnable()) {
//            TranslationRes res = TranslationManager.INSTANCE.translate(text);
//            if (res.isTranslated) {
//                controlChars = res.controlChars;
//                text = res.text;
//
//                float scale = controlChars.getScale();
//
//                if(controlChars.isEmpty()){
//
//                    if(controlChars.isAutoScale())
//                        scale = getStringWidth(orgText) / (float)getStringWidth(text);
//
//                    if(controlChars.isAutoOffsetX())
//                        x += ((getStringWidth(orgText) - scale * getStringWidth(text)) / 2);
//
//                    if(controlChars.isAutoOffsetX())
//                        y += (FONT_HEIGHT * (1 - scale))/2;
//
//                }
//
//                if(controlChars.isEmpty() && scale != 1) {
//                    x /= scale;
//                    y /= scale;
//                    matrix.mul(Matrix4f.makeScale(scale,scale,1));
//                    v.set(controlChars.getOffsetX(), controlChars.getOffsetY(), 0);
//                    matrix.translate(v);
//                }
//
//                int num = func_238423_b_(text, x, y, color, dropShadow, matrix, buffer, transparentIn, colorBackgroundIn, packedLight,isbid);
//
//                cir.setReturnValue(num);
//            }
//        }
//
//
//    }

    @Inject(
            method = "func_238416_a_",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void inject_renderString2(IReorderingProcessor p_238416_1_, float x, float y, int color, boolean p_238416_5_, Matrix4f matrix, IRenderTypeBuffer p_238416_7_, boolean p_238416_8_, int p_238416_9_, int p_238416_10_, CallbackInfoReturnable<Integer> cir) {
        CharConsumer charConsumer = new CharConsumer();
        p_238416_1_.accept(charConsumer);
        String text = charConsumer.getBuilder().toString();

        ControlChars controlChars;
        String orgText = removeFormat(text);
        if (TranslationManager.INSTANCE.isEnable()) {
            TranslationRes res = TranslationManager.INSTANCE.translate(text);
            if (res.isTranslated) {
                controlChars = res.controlChars;
                text = res.text;
                float scale = controlChars.getScale();

                if (controlChars.isEmpty()) {

                    if (controlChars.isAutoScale())
                        scale = getStringWidth(orgText) / (float) getStringWidth(text);

                    if (controlChars.isAutoOffsetX())
                        x += ((getStringWidth(orgText) - scale * getStringWidth(text)) / 2);

                    if (controlChars.isAutoOffsetX())
                        y += (FONT_HEIGHT * (1 - scale)) / 2;
                }

                if (controlChars.isEmpty() && scale != 1) {
                    x /= scale;
                    y /= scale;
                    matrix.mul(Matrix4f.makeScale(scale, scale, 1));
                    v.set(controlChars.getOffsetX(), controlChars.getOffsetY(), 0);
                    matrix.translate(v);
                }

                IReorderingProcessor rep = IReorderingProcessor.func_242239_a(text, charConsumer.getStyle());

                int num = func_238424_b_(rep, x, y, color, p_238416_5_, matrix, p_238416_7_, p_238416_8_, p_238416_9_, p_238416_10_);

                cir.setReturnValue(num);
            }
        }
    }

//    @Shadow protected abstract int func_238423_b_(String p_238423_1_, float p_238423_2_, float p_238423_3_, int p_238423_4_, boolean p_238423_5_, Matrix4f p_238423_6_, IRenderTypeBuffer p_238423_7_, boolean p_238423_8_, int p_238423_9_, int p_238423_10_, boolean p_238423_11_);

    @Shadow
    protected abstract int func_238424_b_(IReorderingProcessor p_238416_1_, float x, float y, int color, boolean p_238416_5_, Matrix4f matrix, IRenderTypeBuffer p_238416_7_, boolean p_238416_8_, int p_238416_9_, int p_238416_10_);


//    @Shadow protected abstract int func_238423_b_(String p_238423_1_, float p_238423_2_, float p_238423_3_, int p_238423_4_, boolean p_238423_5_, Matrix4f p_238423_6_, IRenderTypeBuffer p_238423_7_, boolean p_238423_8_, int p_238423_9_, int p_238423_10_, boolean p_238423_11_);

    private String removeFormat(String text) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length()) {
                ++i;
            } else {
                builder.append(c0);
            }
        }
        return builder.toString();
    }

}
