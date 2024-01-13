package com.gregtechceu.gtceu.core.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author KilaBash
 * @date 2023/7/19
 * @implNote GuiGraphicsAccessor
 */
@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
    @Invoker("<init>")
    static GuiGraphics create(Minecraft client, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumerProvider) {
        return null;
    }
}
