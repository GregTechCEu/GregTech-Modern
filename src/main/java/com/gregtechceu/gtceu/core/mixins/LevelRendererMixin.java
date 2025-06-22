package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = LevelRenderer.class, priority = 500)
@OnlyIn(Dist.CLIENT)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    private @Nullable ClientLevel level;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void renderLevel(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline,
                             Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
                             Matrix4f projectionMatrix, CallbackInfo ci) {
        if (minecraft.player == null || level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        if (minecraft.player.isShiftKeyDown() ||
                !ToolHelper.hasBehaviorsTag(mainHandItem) ||
                !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(mainHandItem);
        if (aoeDefinition.isZero()) return;

        BlockPos hitPos = hitResult.getBlockPos();
        BlockState hitState = level.getBlockState(hitPos);

        SortedSet<BlockDestructionProgress> progresses = destructionProgress.get(hitPos.asLong());
        if (progresses == null || progresses.isEmpty() || !mainHandItem.isCorrectToolForDrops(hitState)) return;
        BlockDestructionProgress progress = progresses.last();

        UseOnContext context = new UseOnContext(minecraft.player, InteractionHand.MAIN_HAND, hitResult);
        var positions = ToolHelper.getHarvestableBlocks(aoeDefinition, context);

        Vec3 vec3 = camera.getPosition();
        double camX = vec3.x();
        double camY = vec3.y();
        double camZ = vec3.z();

        for (BlockPos pos : positions) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
            PoseStack.Pose last = poseStack.last();
            VertexConsumer breakProgressDecal = new SheetedDecalTextureGenerator(
                    this.renderBuffers.crumblingBufferSource()
                            .getBuffer(ModelBakery.DESTROY_TYPES.get(progress.getProgress())),
                    last.pose(), last.normal(), 1.0f);
            ModelData modelData = level.getModelDataManager().getAt(pos);
            this.minecraft.getBlockRenderer().renderBreakingTexture(level.getBlockState(pos), pos,
                    level, poseStack, breakProgressDecal, modelData != null ? modelData : ModelData.EMPTY);
            poseStack.popPose();
        }
    }

    @Shadow
    private static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                    double x, double y, double z,
                                    float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }

    @WrapOperation(method = "renderLevel",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void gtceu$handleAOEOutline(LevelRenderer instance, PoseStack poseStack, VertexConsumer consumer,
                                        Entity entity, double camX, double camY, double camZ,
                                        BlockPos pos, BlockState state, Operation<Void> original) {
        if (minecraft.player == null || level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        if (state.isAir() || minecraft.player.isShiftKeyDown() || !level.isInWorldBounds(pos) ||
                !mainHandItem.isCorrectToolForDrops(state) || !ToolHelper.hasBehaviorsTag(mainHandItem) ||
                !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            gtceu$renderContextAwareOutline(instance, poseStack, consumer, entity, camX, camY, camZ,
                    pos, state, original);
            return;
        }

        UseOnContext context = new UseOnContext(minecraft.player, InteractionHand.MAIN_HAND, hitResult);
        var blocks = ToolHelper.getHarvestableBlocks(ToolHelper.getAoEDefinition(mainHandItem), context);
        blocks.sort((o1, o2) -> {
            if (level.getBlockState(o1).getBlock() instanceof MaterialBlock) {
                if (level.getBlockState(o2).getBlock() instanceof MaterialBlock) {
                    return 0;
                }
                return 1;
            }
            if (level.getBlockState(o2).getBlock() instanceof MaterialBlock) {
                return -1;
            }
            return 0;
        });
        blocks.forEach(blockPos -> gtceu$renderContextAwareOutline(instance, poseStack, consumer, entity,
                camX, camY, camZ, blockPos, level.getBlockState(blockPos), original));
    }

    @Unique
    private void gtceu$renderContextAwareOutline(LevelRenderer instance, PoseStack poseStack, VertexConsumer consumer,
                                                 Entity entity, double camX, double camY, double camZ,
                                                 BlockPos pos, BlockState state, Operation<Void> original) {
        assert level != null;
        var rendererCfg = ConfigHolder.INSTANCE.client.renderer;
        int rgb = 0;
        var doRenderColoredOutline = false;
        // spotless:off
        if (state.getBlock() instanceof MaterialBlock matBlock) {
            doRenderColoredOutline = true;
            rgb = matBlock.material.getMaterialRGB();
        } else if (state.getBlock() instanceof MetaMachineBlock &&
                level.getBlockEntity(pos) instanceof MetaMachineBlockEntity mmbe) {
            if (rendererCfg.coloredTieredMachineOutline && mmbe.getMetaMachine() instanceof ITieredMachine tiered) {
                doRenderColoredOutline = true;
                rgb = GTValues.VCM[tiered.getTier()];
            }
        } else if (rendererCfg.coloredWireOutline && level.getBlockEntity(pos) instanceof CableBlockEntity cbe) {
            doRenderColoredOutline = true;
            rgb = GTValues.VCM[GTUtil.getTierByVoltage(cbe.getNodeData().getVoltage())];
        }

        var blockShape = state.getShape(level, pos, CollisionContext.of(entity));
        // spotless:on
        if (doRenderColoredOutline) {
            var red = (float) FastColor.ARGB32.red(rgb) / 255f;
            var green = (float) FastColor.ARGB32.green(rgb) / 255f;
            var blue = (float) FastColor.ARGB32.blue(rgb) / 255f;
            renderShape(poseStack, consumer, blockShape, pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                    red, green, blue, 1f);
            return;
        }
        for (int dY = -1; dY <= 1; dY++) {
            for (int dX = -1; dX <= 1; dX++) {
                for (int dZ = -1; dZ <= 1; dZ++) {
                    if (level.getBlockState(pos.offset(dX, dY, dZ)).getBlock() instanceof MaterialBlock) {
                        renderShape(poseStack, consumer, blockShape,
                                pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                                0, 0, 0, 1f);
                        return;
                    }
                }
            }
        }
        original.call(instance, poseStack, consumer, entity, camX, camY, camZ, pos, state);
    }
}
