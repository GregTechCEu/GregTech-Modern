package com.gregtechceu.gtceu.core.util.extensions;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface PostPassExt {

    default void gtceu$setDepthCopySource(RenderTarget source) {
        throw new AssertionError("Mixin didn't apply");
    }

    default void gtceu$setClearBeforeDraw(boolean clearBeforeDraw) {
        throw new AssertionError("Mixin didn't apply");
    }

    default void gtceu$setWriteDepth(boolean writeDepth) {
        throw new AssertionError("Mixin didn't apply");
    }
}
