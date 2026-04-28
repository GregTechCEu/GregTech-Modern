package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.Nullable;
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

    @Unique
    private final RandomSource gtceu$modelRandom = RandomSource.create();

    @Inject(method = "submitBlockDestroyAnimation", at = @At("TAIL"))
    private void gtceu$submitAoeBlockDestroyAnimation(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                                      LevelRenderState levelRenderState, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        if (minecraft.player.isShiftKeyDown() ||
                !ToolHelper.hasBehaviorsComponent(mainHandItem) ||
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

        Vec3 camPos = levelRenderState.cameraRenderState.pos;
        int breakProgress = progress.getProgress();

        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x(), pos.getY() - camPos.y(), pos.getZ() - camPos.z());
            BlockStateModel model = this.minecraft.getModelManager().getBlockStateModelSet().get(state);
            submitNodeCollector.submitBreakingBlockModel(poseStack, model, state.getSeed(pos), breakProgress);
            poseStack.popPose();
        }
    }

    @WrapOperation(method = "renderBlockOutline",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IF)V",
                            ordinal = 1))
    private void gtceu$handleAOEOutline(LevelRenderer instance, PoseStack poseStack, VertexConsumer consumer,
                                        double camX, double camY, double camZ, BlockOutlineRenderState outlineState,
                                        int color, float width, Operation<Void> original) {
        if (minecraft.player == null || level == null) return;

        BlockPos pos = outlineState.pos();
        BlockState state = level.getBlockState(pos);
        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        if (state.isAir() || minecraft.player.isShiftKeyDown() || !level.isInWorldBounds(pos) ||
                !mainHandItem.isCorrectToolForDrops(state) || !ToolHelper.hasBehaviorsComponent(mainHandItem) ||
                !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            gtceu$renderContextAwareOutline(instance, poseStack, consumer, camX, camY, camZ,
                    outlineState, pos, state, color, width, original);
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
        blocks.forEach(blockPos -> gtceu$renderContextAwareOutline(instance, poseStack, consumer,
                camX, camY, camZ, outlineState, blockPos, level.getBlockState(blockPos), color, width, original));
    }

    @Unique
    private void gtceu$renderContextAwareOutline(LevelRenderer instance, PoseStack poseStack, VertexConsumer consumer,
                                                 double camX, double camY, double camZ,
                                                 BlockOutlineRenderState outlineState, BlockPos pos, BlockState state,
                                                 int color, float width, Operation<Void> original) {
        assert level != null;
        var rendererCfg = ConfigHolder.INSTANCE.client.renderer;
        int rgb = 0;
        boolean renderColoredOutline = false;

        // spotless:off
        MaterialEntry materialEntry = ChemicalHelper.getMaterialEntry(state.getBlock());
        if (rendererCfg.coloredMaterialBlockOutline && !materialEntry.isEmpty()) {
            renderColoredOutline = true;
            rgb = materialEntry.material().getMaterialRGB();
        } else if (rendererCfg.coloredTieredMachineOutline) {
                if (level.getBlockEntity(pos) instanceof SteamMachine steam) {
                    renderColoredOutline = true;
                    rgb = steam.isHighPressure() ? GTValues.VC_HP_STEAM : GTValues.VC_LP_STEAM;
                } else if (level.getBlockEntity(pos) instanceof ITieredMachine tiered) {
                    renderColoredOutline = true;
                    rgb = GTValues.VCM[tiered.getTier()];
                }
        } else if (rendererCfg.coloredWireOutline && level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipe) {
            renderColoredOutline = true;
            if (!pipe.getFrameMaterial().isNull()) {
                rgb = pipe.getFrameMaterial().getMaterialRGB();
            } else if (pipe instanceof CableBlockEntity cable) {
                rgb = GTValues.VCM[GTUtil.getTierByVoltage(cable.getNodeData().getVoltage())];
            } else if (state.getBlock() instanceof MaterialPipeBlock<?,?,?> materialPipe) {
                rgb = materialPipe.material.getMaterialRGB();
            }
        }
        // spotless:on
        Entity entity = minecraft.getCameraEntity();
        if (entity == null) {
            entity = minecraft.player;
        }
        VoxelShape blockShape = state.getShape(level, pos, CollisionContext.of(entity));

        if (renderColoredOutline) {
            float red = ARGB.red(rgb) / 255f;
            float green = ARGB.green(rgb) / 255f;
            float blue = ARGB.blue(rgb) / 255f;
            ShapeRenderer.renderShape(poseStack, consumer, blockShape,
                    pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ,
                    ARGB.colorFromFloat(0.4f, red, green, blue), width);
            return;
        }

        BlockOutlineRenderState renderState = pos.equals(outlineState.pos()) ? outlineState :
                new BlockOutlineRenderState(pos, outlineState.isTranslucent(), outlineState.highContrast(),
                        blockShape);
        original.call(instance, poseStack, consumer, camX, camY, camZ, renderState, color, width);
    }
}
