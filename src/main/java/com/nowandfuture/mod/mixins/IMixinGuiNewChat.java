package com.nowandfuture.mod.mixins;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(NewChatGui.class)
public interface IMixinGuiNewChat {

    @Accessor
    List<ChatLine<IReorderingProcessor>> getDrawnChatLines();//drawnChatLines
}
