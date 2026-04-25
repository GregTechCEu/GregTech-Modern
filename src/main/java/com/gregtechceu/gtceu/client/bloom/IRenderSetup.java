package com.gregtechceu.gtceu.client.bloom;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.jetbrains.annotations.NotNull;

public interface IRenderSetup {

    /**
     * Run any pre render gl code here.
     *
     * @param buffer Buffer builder
     */
    void preDraw(BufferBuilder buffer);

    /**
     * Run any post render gl code here.
     *
     * @param buffer Buffer builder
     */
    void postDraw(BufferBuilder buffer);
}
