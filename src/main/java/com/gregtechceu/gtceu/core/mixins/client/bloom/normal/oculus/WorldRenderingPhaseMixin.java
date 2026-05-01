package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.oculus;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.integration.iris.GTIrisHooks;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.RenderType;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldRenderingPhase.class, remap = false)
public class WorldRenderingPhaseMixin {

    @Shadow
    @Final
    @Mutable
    private static WorldRenderingPhase[] $VALUES;

    @SuppressWarnings("SameParameterValue")
    @Invoker("<init>")
    private static WorldRenderingPhase gtceu$callInit(String name, int ordinal) {
        throw new AssertionError();
    }

    static {
        if (BloomShaderManager.isBloomShaderAvailable()) {
            GTIrisHooks.BLOOM_RENDERING_PHASE = gtceu$callInit("GTCEU:BLOOM", $VALUES.length);
            $VALUES = ArrayUtils.add($VALUES, GTIrisHooks.BLOOM_RENDERING_PHASE);
        }
    }

    @Inject(method = "fromTerrainRenderType", at = @At(value = "HEAD"), cancellable = true)
    private static void gtceu$fixBloomLayerError(RenderType renderType,
                                                 CallbackInfoReturnable<WorldRenderingPhase> cir) {
        if (!BloomShaderManager.isBloomShaderAvailable()) return;

        if (renderType == GTRenderTypes.bloom()) {
            cir.setReturnValue(GTIrisHooks.getBloomRenderingPhase());
        }
    }
}
