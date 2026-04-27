package com.gregtechceu.gtceu.core.mixins.iris;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.integration.iris.IrisHooks;

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
        IrisHooks.BLOOM_RENDERING_PHASE = gtceu$callInit("BLOOM", $VALUES.length);
        $VALUES = ArrayUtils.add($VALUES, IrisHooks.BLOOM_RENDERING_PHASE);
    }

    @Inject(method = "fromTerrainRenderType",
            at = @At(value = "NEW", target = "java/lang/IllegalStateException"),
            cancellable = true)
    private static void gtceu$checkForBloomLayer(RenderType renderType,
                                                 CallbackInfoReturnable<WorldRenderingPhase> cir) {
        if (renderType == GTRenderTypes.bloom()) {
            cir.setReturnValue(IrisHooks.BLOOM_RENDERING_PHASE);
        }
    }
}
