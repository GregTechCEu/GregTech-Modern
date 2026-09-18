package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsAccessor {

    @Invoker
    void callFlushIfUnmanaged();

    @Mutable
    @Accessor
    void setPose(PoseStack pose);
}
