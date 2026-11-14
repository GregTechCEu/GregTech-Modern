package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.util.extensions.BlockElementFaceExt;

import net.minecraft.client.renderer.block.model.BlockElementFace;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockElementFace.class)
public class BlockElementFaceMixin implements BlockElementFaceExt {

    @Unique
    private FaceLayer gtceu$faceLayer = FaceLayer.UNCLASSIFIED;

    @Override
    public BlockElementFace gtceu$setFaceLayer(FaceLayer layer) {
        this.gtceu$faceLayer = layer;
        return (BlockElementFace) (Object) this;
    }

    @Override
    public FaceLayer gtceu$getFaceLayer() {
        return this.gtceu$faceLayer;
    }
}
