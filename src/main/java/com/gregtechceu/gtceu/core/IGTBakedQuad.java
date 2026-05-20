package com.gregtechceu.gtceu.core;

import net.minecraft.client.renderer.block.model.BakedQuad;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IGTBakedQuad {

    @ApiStatus.Internal
    default BakedQuad gtceu$setTextureKey(@Nullable String key) {
        return (BakedQuad) this;
    }

    default @Nullable String gtceu$getTextureKey() {
        return null;
    }
}
