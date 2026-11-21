package com.gregtechceu.gtceu.core.util.extensions;

import com.gregtechceu.gtceu.client.model.FaceLayer;

import net.minecraft.client.renderer.block.model.BlockElementFace;

import org.jetbrains.annotations.ApiStatus;

public interface BlockElementFaceExt {

    @ApiStatus.Internal
    default BlockElementFace gtceu$setFaceLayer(FaceLayer layer) {
        throw new AssertionError("Mixin didn't apply");
    }

    default FaceLayer gtceu$getFaceLayer() {
        throw new AssertionError("Mixin didn't apply");
    }
}
