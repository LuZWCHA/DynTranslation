package com.nowandfuture.translation.mixins;

import com.nowandfuture.translation.core.ControlChars;
import com.nowandfuture.translation.core.ControlCharsUtil;
import com.nowandfuture.translation.core.TranslationManager;
import com.nowandfuture.translation.core.TranslationRes;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow public abstract int getStringWidth(String text);

    @Shadow public abstract int drawString(String text, float x, float y, int color, boolean dropShadow);

    @Inject(
            method = "drawString(Ljava/lang/String;FFIZ)I",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void inject_drawString(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> callbackInfo){
        ControlChars controlChars = ControlChars.EMPTY;
        String orgText = removeFormat(text);
        if(TranslationManager.INSTANCE.isEnable()) {
            TranslationRes res = TranslationManager.INSTANCE.translate(text);
            controlChars = res.controlChars;
            text = res.text;
        }

        float scale = controlChars.getScale();

        if(!controlChars.isEMPTY()){

            if(controlChars.isAutoScale())
                scale = getStringWidth(orgText) / (float)getStringWidth(text);

            if(controlChars.isAutoOffsetX())
                x += ((getStringWidth(orgText) - scale * getStringWidth(text)) / 2);
            else
                x += controlChars.getOffsetX();

            if(controlChars.isAutoOffsetX())
                y += (9f * (1 - scale))/2;
            else
                y += controlChars.getOffsetY();
        }


        if(!controlChars.isEMPTY() && scale != 1) {
//            GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST );
//            GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST );
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y,0);
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.translate(-x, -y,0);
        }else{
//            GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR );
//            GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR );
        }

        GlStateManager.enableAlpha();
        resetStyles();
        int i;

        if (dropShadow)
        {
            i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
            i = Math.max(i, this.renderString(text, x, y, color, false));
        }
        else
        {
            i = this.renderString(text, x, y, color, false);
        }
        callbackInfo.setReturnValue(i);

        if(!controlChars.isEMPTY() &&scale != 1) {
            GlStateManager.popMatrix();
        }
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

    @Shadow
    protected abstract int renderString(String text, float v, float v1, int color, boolean b);

    @Shadow
    protected abstract void resetStyles();
}
