package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FaceBakery.class, priority = 1500)
public class FaceBakeryMixin {

    @ModifyReturnValue(method = "bakeQuad", at = @At(value = "RETURN"))
    private BakedQuad gtceu$addQuadTextureKey(BakedQuad quad, Vector3f posFrom, Vector3f posTo, BlockElementFace face) {
        return quad.gtceu$setTextureKey(face.texture);
    }
}
