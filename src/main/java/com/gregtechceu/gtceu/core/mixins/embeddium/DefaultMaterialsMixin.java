package com.gregtechceu.gtceu.core.mixins.embeddium;

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

    @Inject(method = "forRenderLayer",
            at = @At(value = "NEW", target = "java/lang/IllegalArgumentException"),
            cancellable = true)
    private static void gtceu$checkForBloomLayer(RenderType renderType,
                                                 CallbackInfoReturnable<Material> cir) {
        if (renderType == GTRenderTypes.bloom()) {
            cir.setReturnValue(GTEmbeddiumCompat.BLOOM_MATERIAL);
        }
    }
}
