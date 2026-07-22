package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.RenderType;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultMaterials.class, remap = false)
public class DefaultMaterialsMixin {

    @Inject(method = "forRenderLayer", at = @At(value = "HEAD"), cancellable = true)
    private static void gtceu$fixBloomLayerError(RenderType renderType,
                                                 CallbackInfoReturnable<Material> cir) {
        if (!BloomShaderManager.isBloomAvailable()) return;

        if (renderType == GTRenderTypes.bloom()) {
            cir.setReturnValue(GTEmbeddiumCompat.BLOOM_MATERIAL);
        }
    }
}
