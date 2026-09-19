package com.gregtechceu.gtceu.core.mixins.client.customchunk.sodium;

import com.gregtechceu.gtceu.client.renderer.FaceLayerRouting;

import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class SodiumFaceLayerTagMixin {

    @ModifyExpressionValue(method = "bufferDefaultModel",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;"))
    private MutableQuadViewImpl gtceu$carryFaceLayerMarker(MutableQuadViewImpl quadView, @Local BakedQuad quad) {
        int tag = FaceLayerRouting.getSodiumRoutingTag(quad);
        if (tag != 0) {
            quadView.tag(tag);
        }
        return quadView;
    }
}
