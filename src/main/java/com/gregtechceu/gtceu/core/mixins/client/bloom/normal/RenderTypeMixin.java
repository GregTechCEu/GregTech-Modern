package com.gregtechceu.gtceu.core.mixins.client.bloom.normal;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;

import net.minecraft.client.renderer.RenderType;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @ModifyExpressionValue(method = "<clinit>",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;",
                                    remap = false))
    private static ImmutableList<RenderType> gtceu$forceAddBloomToChunkBufferLayers(ImmutableList<RenderType> original) {
        // don't bother checking if bloom can be loaded here; Bloom can't be used with OptiFine installed and shaders
        // aren't loaded when this class is loaded.
        // This mixin is also only applied if bloom safe mode is disabled.
        if (GTEarlyConfig.OPTIFINE_PRESENT) return original;

        return ImmutableList.<RenderType>builder()
                .addAll(original).add(GTRenderTypes.bloom())
                .build();
    }
}
