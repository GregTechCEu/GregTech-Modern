package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;

import net.minecraft.client.renderer.RenderType;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderType.class)
public class CustomChunkRenderTypeMixin {

    @ModifyExpressionValue(method = "<clinit>",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;",
                                    remap = false))
    private static ImmutableList<RenderType> gtceu$addCustomChunkBufferLayers(ImmutableList<RenderType> original) {
        ImmutableList.Builder<RenderType> layers = ImmutableList.builder();
        layers.addAll(original);
        for (var pass : CustomChunkRenderPassRegistry.activePasses()) {
            if (original.stream().noneMatch(renderType -> renderType == pass.renderType())) {
                layers.add(pass.renderType());
            }
        }
        return layers.build();
    }
}
