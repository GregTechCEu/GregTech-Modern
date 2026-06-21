package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.iris;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.RenderType;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.*;
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

    @Unique
    private static final WorldRenderingPhase GTCEU$BLOOM;

    @Invoker("<init>")
    private static WorldRenderingPhase gtceu$callInit(String name, int ordinal) {
        throw new AssertionError();
    }

    static {
        // don't bother checking if bloom can be loaded here; Oculus won't load with OptiFine installed and shaders
        // aren't loaded when this class is loaded.
        // This mixin is also only applied if bloom safe mode is disabled.
        GTCEU$BLOOM = gtceu$callInit("GTCEU$BLOOM", $VALUES.length);
        $VALUES = ArrayUtils.add($VALUES, GTCEU$BLOOM);
    }

    @Inject(method = "fromTerrainRenderType", at = @At(value = "HEAD"), cancellable = true)
    private static void gtceu$fixBloomLayerError(RenderType renderType,
                                                 CallbackInfoReturnable<WorldRenderingPhase> cir) {
        if (!BloomShaderManager.isBloomAvailable()) return;

        if (renderType == GTRenderTypes.bloom()) {
            cir.setReturnValue(GTCEU$BLOOM);
        }
    }
}
