package com.gregtechceu.gtceu.client.bloom;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.*;
import org.jetbrains.annotations.Nullable;

public interface IRenderSetup {

    /**
     * Run any pre render GL code here.
     */
    @OnlyIn(Dist.CLIENT)
    @Nullable
    BufferBuilder preDraw();

    /**
     * Run any post render gl code here.
     *
     * @param buffer Buffer builder
     */
    @OnlyIn(Dist.CLIENT)
    default void postDraw(BufferBuilder buffer) {
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
