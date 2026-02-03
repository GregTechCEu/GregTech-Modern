package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {

    /**
     * Fix the block destruction progress animation ignoring any model data
     * the model itself adds in {@link BakedModel#getModelData}.
     * 
     * @author screret
     */
    @ModifyArg(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraftforge/client/model/data/ModelData;)V",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"),
               index = 10)
    private ModelData gtceu$fixBreakingAnimationModelData(ModelData modelData,
                                                          @Local(argsOnly = true) BlockState state,
                                                          @Local(argsOnly = true) BlockPos pos,
                                                          @Local(argsOnly = true) BlockAndTintGetter level,
                                                          @Local BakedModel model) {
        return model.getModelData(level, pos, state, modelData);
    }
}
