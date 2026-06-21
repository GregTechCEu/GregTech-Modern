package com.gregtechceu.gtceu.core.util.extensions;

import net.minecraft.client.renderer.RenderType;

public interface QuadLighterExt {

    default void gtceu$setRenderType(RenderType currentRenderType) {
        throw new AssertionError("Mixin didn't apply");
    }
}
