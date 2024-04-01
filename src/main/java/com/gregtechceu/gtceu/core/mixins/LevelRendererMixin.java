package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(LevelRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow private @Nullable ClientLevel level;

    @Inject(
            method = {"renderLevel"},
            at = {@At("HEAD")}
    )
    private void renderLevel(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        if (!ToolHelper.hasBehaviorsTag(mainHandItem) || ToolHelper.getAoEDefinition(mainHandItem) == AoESymmetrical.none() || !(minecraft.hitResult instanceof BlockHitResult result) || minecraft.player.isShiftKeyDown()) return;

        BlockPos hitResultPos = result.getBlockPos();
        BlockState hitResultState = minecraft.level.getBlockState(hitResultPos);

        SortedSet<BlockDestructionProgress> progresses = destructionProgress.get(hitResultPos.asLong());
        if (progresses == null || progresses.isEmpty() || !mainHandItem.isCorrectToolForDrops(hitResultState)) return;
        BlockDestructionProgress progress = progresses.last();

        Set<BlockPos> positions = ToolHelper.getHarvestableBlocks(mainHandItem, ToolHelper.getAoEDefinition(mainHandItem), level, minecraft.player, result);

        Vec3 vec3 = camera.getPosition();
        double camX = vec3.x();
        double camY = vec3.y();
        double camZ = vec3.z();

        for (BlockPos pos : positions) {
            poseStack.pushPose();
            poseStack.translate((double)pos.getX() - camX, (double)pos.getY() - camY, (double)pos.getZ() - camZ);
            PoseStack.Pose last = poseStack.last();
            VertexConsumer breakProgressDecal = new SheetedDecalTextureGenerator(
                    this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress.getProgress())),
                    last.pose(),
                    last.normal(),
                    1.0f
            );
            this.minecraft.getBlockRenderer().renderBreakingTexture(minecraft.level.getBlockState(pos), pos, minecraft.level, poseStack, breakProgressDecal);
            poseStack.popPose();
        }
    }

    @Invoker("renderShape")
    public static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }

    @Inject(method = "renderHitOutline", at = @At("HEAD"))
    private void renderHitOutline(PoseStack poseStack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        if (state.isAir() || !minecraft.level.isInWorldBounds(pos) || !mainHandItem.isCorrectToolForDrops(state) || minecraft.player.isShiftKeyDown() || !ToolHelper.hasBehaviorsTag(mainHandItem)) return;

        Set<BlockPos> blockPositions = ToolHelper.getHarvestableBlocks(mainHandItem, ToolHelper.getAoEDefinition(mainHandItem), level, minecraft.player, minecraft.hitResult);
        Set<VoxelShape> outlineShapes = new HashSet<>();

        for (BlockPos position : blockPositions) {
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
