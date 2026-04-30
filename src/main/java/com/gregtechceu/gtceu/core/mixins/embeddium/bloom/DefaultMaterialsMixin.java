package com.gregtechceu.gtceu.core.mixins.embeddium.bloom;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
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
        // TODO add a way to conditionally load mixins based on configs
        if (ConfigHolder.INSTANCE.client.bloom.safeMode) return;
        if (!GTShaders.canUseBloomShader()) return;

        if (renderType == GTRenderTypes.bloom()) {
            cir.setReturnValue(GTEmbeddiumCompat.BLOOM_MATERIAL);
        }
    }
}
