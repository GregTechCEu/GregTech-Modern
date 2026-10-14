package com.gregtechceu.gtceu.client.bloom;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.BufferBuilder;

public interface IRenderSetup {

    /**
     * Run any pre render gl code here.
     *
     * @param buffer Buffer builder
     */
    @OnlyIn(Dist.CLIENT)
    void preDraw(BufferBuilder buffer);

    /**
     * Run any post render gl code here.
     *
     * @param buffer Buffer builder
     */
    @OnlyIn(Dist.CLIENT)
    void postDraw(BufferBuilder buffer);
}
