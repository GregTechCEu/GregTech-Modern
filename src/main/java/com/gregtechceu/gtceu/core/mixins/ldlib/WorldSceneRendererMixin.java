package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.gregtechceu.gtceu.client.model.compat.BakedModel;

import com.lowdragmc.lowdraglib.client.scene.ISceneBlockRenderHook;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.lang.reflect.Method;
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
    private void renderBlocks(PoseStack poseStack, Object blockRenderer, RenderType renderType,
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
                gtceu$invokeByName(hook, "applyVertexConsumerWrapper", world, pos, state, wrapperBuffer, renderType,
                        partialTicks);
            }
            if (block == Blocks.AIR) continue;
            Object baked = gtceu$invokeByName(blockRenderer, "getBlockModel", state);
            if (!(baked instanceof BakedModel model)) continue;
            ModelData modelData = gtceu$betterGetModelData(model, state, pos, world);
            RandomSource random = RandomSource.create(0);

            if (state.getRenderShape() == RenderShape.MODEL &&
                    model.getRenderTypes(state, random, modelData).contains(renderType)) {
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                gtceu$invokeByName(blockRenderer, "renderBatched", state, pos,
                        (BlockAndTintGetter) world,
                        poseStack, wrapperBuffer, true, random, modelData, renderType);
                poseStack.popPose();
            }
            if (!fluidState.isEmpty()) {
                wrapperBuffer.addOffset((pos.getX() - (pos.getX() & 15)), (pos.getY() - (pos.getY() & 15)),
                        (pos.getZ() - (pos.getZ() & 15)));
                gtceu$invokeByName(blockRenderer, "renderLiquid", pos,
                        (BlockAndTintGetter) world, wrapperBuffer,
                        state, fluidState);
            }
            wrapperBuffer.clearOffset();
            wrapperBuffer.clearColor();
            if (maxProgress > 0) {
                progress++;
            }
        }
    }

    @Unique
    private static @Nullable Object gtceu$invokeByName(Object target, String name, Object... args) {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
                continue;
            }
            try {
                return method.invoke(target, args);
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {}
        }
        return null;
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
        return model.getModelData((BlockAndTintGetter) (Object) level, pos, state, modelData);
    }
}
