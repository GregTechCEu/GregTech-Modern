package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.client.scene.ISceneBlockRenderHook;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.Set;

@Mixin(value = WorldSceneRenderer.class, remap = false)
public abstract class WorldSceneRendererMixin {

    @Shadow
    public @Final Level world;

    @Shadow
    protected int maxProgress;
    @Shadow
    protected int progress;
    @Shadow
    private Set<BlockPos> blocked;

    /**
     * @author screret
     * @reason Patch the rendering to use thread-safe logic and correct render type lookup
     */
    @Overwrite
    private void renderBlocks(PoseStack poseStack, BlockRenderDispatcher blockRenderer, RenderType renderType,
                              WorldSceneRenderer.VertexConsumerWrapper wrapperBuffer,
                              Collection<BlockPos> renderedBlocks, @Nullable ISceneBlockRenderHook hook,
                              float partialTicks) {
        for (BlockPos pos : renderedBlocks) {
            if (blocked != null && blocked.contains(pos)) {
                continue;
            }
            BlockState state = world.getBlockState(pos);
            FluidState fluidState = state.getFluidState();
            Block block = state.getBlock();

            if (hook != null) {
                hook.applyVertexConsumerWrapper(world, pos, state, wrapperBuffer, renderType, partialTicks);
            }
            if (block == Blocks.AIR) continue;
            BakedModel model = blockRenderer.getBlockModel(state);
            ModelData modelData = gtceu$betterGetModelData(model, state, pos, world);
            RandomSource random = RandomSource.create(0);

            if (state.getRenderShape() == RenderShape.MODEL &&
                    model.getRenderTypes(state, random, modelData).contains(renderType)) {
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderer.getModelRenderer().tesselateBlock(world, model, state, pos,
                        poseStack, wrapperBuffer, true, random, state.getSeed(pos),
                        OverlayTexture.NO_OVERLAY, modelData, renderType);
                poseStack.popPose();
            }
            if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderType) {
                wrapperBuffer.addOffset((pos.getX() - (pos.getX() & 15)), (pos.getY() - (pos.getY() & 15)),
                        (pos.getZ() - (pos.getZ() & 15)));
                blockRenderer.renderLiquid(pos, world, wrapperBuffer, state, fluidState);
            }
            wrapperBuffer.clearOffset();
            wrapperBuffer.clearColor();
            if (maxProgress > 0) {
                progress++;
            }
        }
    }

    @Unique
    private static ModelData gtceu$betterGetModelData(BakedModel model, BlockState state, BlockPos pos, Level level) {
        ModelData modelData = level.getModelData(pos);
        if (modelData == ModelData.EMPTY) {
            var be = level.getBlockEntity(pos);
            if (be != null) {
                modelData = be.getModelData();
            }
        }
        return model.getModelData(level, pos, state, modelData);
    }
}
