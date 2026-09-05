package com.gregtechceu.gtceu.core.mixins.client.customchunk.iris;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.RenderType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldRenderingPhase.class, remap = false)
public class WorldRenderingPhaseMixin {

    @Inject(method = "fromTerrainRenderType", at = @At("HEAD"), cancellable = true)
    private static void gtceu$classifyCustomChunkLayer(RenderType renderType,
                                                       CallbackInfoReturnable<WorldRenderingPhase> cir) {
        var pass = CustomChunkRenderPassRegistry.getPass(renderType);
        if (pass == null) return;

        cir.setReturnValue(switch (pass.terrainPhase()) {
            case SOLID -> WorldRenderingPhase.TERRAIN_SOLID;
            case CUTOUT_MIPPED -> WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED;
            case CUTOUT -> WorldRenderingPhase.TERRAIN_CUTOUT;
            case TRANSLUCENT -> WorldRenderingPhase.TERRAIN_TRANSLUCENT;
            case TRIPWIRE -> WorldRenderingPhase.TRIPWIRE;
            case CUSTOM -> WorldRenderingPhase.TERRAIN_CUTOUT;
        });
    }
}
