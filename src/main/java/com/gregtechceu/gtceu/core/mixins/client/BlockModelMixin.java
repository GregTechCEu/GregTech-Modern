package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockModel.class, priority = 1500)
public class BlockModelMixin {

    @ModifyReturnValue(method = "bakeFace", at = @At(value = "RETURN"))
    private static BakedQuad gtceu$addQuadTextureKeyBlock(BakedQuad quad, BlockElement part, BlockElementFace face) {
        return quad.gtceu$setTextureKey(face.texture);
    }
}
