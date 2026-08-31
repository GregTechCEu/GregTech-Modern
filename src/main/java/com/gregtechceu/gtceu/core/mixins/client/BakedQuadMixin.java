package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.util.extensions.BakedQuadExt;

import net.minecraft.client.renderer.block.model.BakedQuad;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.Arrays;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadExt {

    @Unique
    private String gtceu$textureKey = null;
    @Unique
    private FaceLayer gtceu$faceLayer = FaceLayer.UNCLASSIFIED;

    @Override
    public BakedQuad gtceu$setTextureKey(@Nullable String key) {
        this.gtceu$textureKey = key;
        this.gtceu$faceLayer = FaceLayer.fromTextureKey(key);
        return (BakedQuad) (Object) this;
    }

    @Override
    public String gtceu$getTextureKey() {
        return gtceu$textureKey;
    }

    @Override
    public BakedQuad gtceu$setFaceLayer(FaceLayer layer) {
        this.gtceu$faceLayer = layer;
        return (BakedQuad) (Object) this;
    }

    @Override
    public FaceLayer gtceu$getFaceLayer() {
        return gtceu$faceLayer;
    }

    // @Intrinsic means this'll be skipped if someone else does it too
    @Intrinsic
    public boolean equals(Object o) {
        if (!(o instanceof BakedQuad that)) return false;

        return self().isShade() == that.isShade() &&
                self().hasAmbientOcclusion() == that.hasAmbientOcclusion() &&
                self().getTintIndex() == that.getTintIndex() &&
                self().getDirection() == that.getDirection() &&
                self().getSprite() == that.getSprite() &&
                Arrays.equals(self().getVertices(), that.getVertices());
    }

    @Intrinsic
    public int hashCode() {
        int result = Boolean.hashCode(self().isShade());
        result = 31 * result + Boolean.hashCode(self().hasAmbientOcclusion());
        result = 31 * result + self().getTintIndex();
        result = 31 * result + self().getDirection().hashCode();
        result = 31 * result + self().getSprite().hashCode();
        result = 31 * result + Arrays.hashCode(self().getVertices());

        return result;
    }

    @Intrinsic
    private BakedQuad self() {
        return (BakedQuad) (Object) this;
    }
}
