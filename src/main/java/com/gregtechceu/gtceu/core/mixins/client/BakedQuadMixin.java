package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.core.IGTBakedQuad;

import net.minecraft.client.renderer.block.model.BakedQuad;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements IGTBakedQuad {

    @Unique
    private String gtceu$textureKey = null;

    @Override
    public BakedQuad gtceu$setTextureKey(@Nullable String key) {
        this.gtceu$textureKey = key;
        return (BakedQuad) (Object) this;
    }

    @Override
    public String gtceu$getTextureKey() {
        return gtceu$textureKey;
    }
}
