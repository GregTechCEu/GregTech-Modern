package com.gregtechceu.gtceu.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

public interface IRenderSetup {

    /**
     * Run any pre render gl code here.
     *
     * @param buffer Buffer builder
     */
    void preDraw(@NotNull MultiBufferSource buffer);

    /**
     * Run any post render gl code here.
     *
     * @param buffer Buffer builder
     */
    void postDraw(@NotNull MultiBufferSource buffer);
}
