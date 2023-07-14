package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(
            method = {"renderLevel"},
            at = {@At("HEAD")}
    )
    private void renderLevel(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        if (!GTToolType.MINING_HAMMER.is(mainHandItem) || !(minecraft.hitResult instanceof BlockHitResult result)) return;

        BlockPos hitResultPos = result.getBlockPos();
        BlockState hitResultState = minecraft.level.getBlockState(hitResultPos);

        SortedSet<BlockDestructionProgress> progresses = destructionProgress.get(hitResultPos.asLong());

        if (progresses == null || progresses.isEmpty() || !mainHandItem.isCorrectToolForDrops(hitResultState)) return;

        BlockDestructionProgress progress = progresses.last();

        List<BlockPos> positions = ToolHelper.getAOEPositions(minecraft.player, mainHandItem, hitResultPos, 1);

        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double e = vec3.y();
        double f = vec3.z();

        for (BlockPos pos : positions) {
            poseStack.pushPose();
            poseStack.translate((double)pos.getX() - d, (double)pos.getY() - e, (double)pos.getZ() - f);
            PoseStack.Pose pose2 = poseStack.last();
            VertexConsumer vertexConsumer2 = new SheetedDecalTextureGenerator(
                    this.renderBuffers.crumblingBufferSource().getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(progress.getProgress())), pose2.pose(), pose2.normal()
            );
            this.minecraft.getBlockRenderer().renderBreakingTexture(minecraft.level.getBlockState(pos), pos, minecraft.level, poseStack, vertexConsumer2);
            poseStack.popPose();
        }
    }

    @Invoker("renderShape")
    public static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }

    @Inject(
            method = {"renderHitOutline"},
            at = {@At("HEAD")}
    )
    private void renderHitOutline(PoseStack poseStack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        if (!GTToolType.MINING_HAMMER.is(mainHandItem)) return;

        if (state.isAir() || !minecraft.level.isInWorldBounds(pos) || !mainHandItem.isCorrectToolForDrops(state)) return;

        List<BlockPos> blockPositions = ToolHelper.getAOEPositions(minecraft.player, mainHandItem, pos, 1);
        List<VoxelShape> outlineShapes = new ArrayList<>();

        for (BlockPos position : blockPositions) {
            if (!ToolHelper.aoeCanBreak(mainHandItem, minecraft.level, pos, position)) continue;

            BlockPos diffPos = position.subtract(pos);
            BlockState offsetState = minecraft.level.getBlockState(position);

            outlineShapes.add(offsetState.getShape(minecraft.level, position).move(diffPos.getX(), diffPos.getY(), diffPos.getZ()));
        }

        outlineShapes.forEach(shape -> {
            renderShape(
                    poseStack,
                    consumer,
                    shape,
                    (double) pos.getX() - camX,
                    (double) pos.getY() - camY,
                    (double) pos.getZ() - camZ,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.4F);
        });

    }
}
