package com.gregtechceu.gtceu.core.mixins.client.customchunk.iris;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.RenderType;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    private static final WorldRenderingPhase GTCEU$CUSTOM_CHUNK;

    @Invoker("<init>")
    private static WorldRenderingPhase gtceu$callInit(String name, int ordinal) {
        throw new AssertionError();
    }

    static {
        GTCEU$CUSTOM_CHUNK = gtceu$callInit("GTCEU$CUSTOM_CHUNK", $VALUES.length);
        $VALUES = ArrayUtils.add($VALUES, GTCEU$CUSTOM_CHUNK);
    }

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
            case CUSTOM -> GTCEU$CUSTOM_CHUNK;
        });
    }
}
