package com.gregtechceu.gtceu.core.util.extensions;

import com.gregtechceu.gtceu.client.model.FaceLayer;

import net.minecraft.client.renderer.block.model.BakedQuad;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface BakedQuadExt {

    @ApiStatus.Internal
    default BakedQuad gtceu$setTextureKey(@Nullable String key) {
        throw new AssertionError("Mixin didn't apply");
    }

    default @Nullable String gtceu$getTextureKey() {
        throw new AssertionError("Mixin didn't apply");
    }

    @ApiStatus.Internal
    default BakedQuad gtceu$setFaceLayer(FaceLayer layer) {
        throw new AssertionError("Mixin didn't apply");
    }

    default FaceLayer gtceu$getFaceLayer() {
        throw new AssertionError("Mixin didn't apply");
    }
}
