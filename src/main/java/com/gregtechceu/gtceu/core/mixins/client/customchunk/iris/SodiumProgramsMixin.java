package com.gregtechceu.gtceu.core.mixins.client.customchunk.iris;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPass;
import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms;
import net.irisshaders.iris.shadows.ShadowRenderingState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SodiumPrograms.class, remap = false)
public class SodiumProgramsMixin {

    @Inject(method = "mapTerrainRenderPass", at = @At("HEAD"), cancellable = true)
    private void gtceu$mapCustomTerrainPass(TerrainRenderPass terrainRenderPass,
                                            CallbackInfoReturnable<SodiumPrograms.Pass> cir) {
        for (CustomChunkRenderPass pass : CustomChunkRenderPassRegistry.activePasses()) {
            if (GTSodiumCompat.getCustomRenderPass(pass.renderType()) != terrainRenderPass) continue;

            boolean shadow = ShadowRenderingState.areShadowsCurrentlyBeingRendered();
            cir.setReturnValue(switch (pass.terrainPhase()) {
                case SOLID -> shadow ? SodiumPrograms.Pass.SHADOW : SodiumPrograms.Pass.TERRAIN;
                case CUTOUT_MIPPED, CUTOUT, CUSTOM -> shadow ?
                        SodiumPrograms.Pass.SHADOW_CUTOUT : SodiumPrograms.Pass.TERRAIN_CUTOUT;
                case TRANSLUCENT, TRIPWIRE -> shadow ?
                        SodiumPrograms.Pass.SHADOW_TRANS : SodiumPrograms.Pass.TRANSLUCENT;
            });
            return;
        }
    }
}
