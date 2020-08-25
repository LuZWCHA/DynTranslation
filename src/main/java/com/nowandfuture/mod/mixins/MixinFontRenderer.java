package com.nowandfuture.mod.mixins;

import com.nowandfuture.mod.core.ControlChars;
import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.core.TranslationRes;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow public abstract int getStringWidth(String text);

    @Shadow protected abstract int renderStringAt(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean transparentIn, int colorBackgroundIn, int packedLight);

    @Shadow @Final public int FONT_HEIGHT;

    private final Vector3f v = new Vector3f();

    @Inject(
            method = "renderString(Ljava/lang/String;FFIZLnet/minecraft/client/renderer/Matrix4f;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ZII)I",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void inject_renderString(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean transparentIn, int colorBackgroundIn, int packedLight, CallbackInfoReturnable<Integer> cir){
        ControlChars controlChars = ControlChars.EMPTY;
        String orgText = removeFormat(text);
        if(TranslationManager.INSTANCE.isEnable()) {
            TranslationRes res = TranslationManager.INSTANCE.translate(text);
            controlChars = res.controlChars;
            text = res.text;
        }

        float scale = controlChars.getScale();

        if(controlChars.isEmpty()){

            if(controlChars.isAutoScale())
                scale = getStringWidth(orgText) / (float)getStringWidth(text);

            if(controlChars.isAutoOffsetX())
                x += ((getStringWidth(orgText) - scale * getStringWidth(text)) / 2);

            if(controlChars.isAutoOffsetX())
                y += (FONT_HEIGHT * (1 - scale))/2;

        }

        if(controlChars.isEmpty() && scale != 1) {
            x /= scale;
            y /= scale;
            matrix.mul(Matrix4f.makeScale(scale,scale,1));
            v.set(controlChars.getOffsetX(), controlChars.getOffsetY(), 0);
            matrix.translate(v);
        }

        int num = renderStringAt(text, x, y, color, dropShadow, matrix, buffer, transparentIn, colorBackgroundIn, packedLight);

        cir.setReturnValue(num);
    }

    private String removeFormat(String text){
        final StringBuilder builder = new StringBuilder();
        for(int i = 0;i < text.length();++i) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length()) {
                ++i;
            }else{
                builder.append(c0);
            }
        }
        return builder.toString();
    }

}
