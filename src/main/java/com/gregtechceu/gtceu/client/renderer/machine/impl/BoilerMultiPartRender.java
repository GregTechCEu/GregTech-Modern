package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.model.machine.IControllerModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class BoilerMultiPartRender extends DynamicRender<MultiblockControllerMachine, BoilerMultiPartRender>
                                   implements IControllerModelRenderer {

    // spotless:off
    public static final Codec<BoilerMultiPartRender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("firebox_idle").forGetter(BoilerMultiPartRender::getFireboxIdle),
            BlockState.CODEC.fieldOf("firebox_active").forGetter(BoilerMultiPartRender::getFireboxActive),
            BlockState.CODEC.fieldOf("casing_block").forGetter(BoilerMultiPartRender::getCasing)
    ).apply(instance, BoilerMultiPartRender::new));
    public static final DynamicRenderType<MultiblockControllerMachine, BoilerMultiPartRender> TYPE = new DynamicRenderType<>(BoilerMultiPartRender.CODEC);
    // spotless:on

    @Getter
    private final BlockState fireboxIdle, fireboxActive;
    @Getter
    private final BlockState casing;

    private BakedModel fireboxIdleModel, fireboxActiveModel;
    private BakedModel casingModel;

    public BoilerMultiPartRender(BoilerFireboxType fireboxType, Supplier<? extends Block> casingBlock) {
        this(GTBlocks.ALL_FIREBOXES.get(fireboxType).getDefaultState(),
                GTBlocks.ALL_FIREBOXES.get(fireboxType).getDefaultState().setValue(GTBlockStateProperties.ACTIVE, true),
                casingBlock.get().defaultBlockState());
    }

    public BoilerMultiPartRender(BlockState fireboxIdle, BlockState fireboxActive, BlockState casing) {
        this.fireboxIdle = fireboxIdle;
        this.fireboxActive = fireboxActive;
        this.casing = casing;
    }

    @Override
    public DynamicRenderType<MultiblockControllerMachine, BoilerMultiPartRender> getType() {
        return TYPE;
    }

    @Override
    public void render(MultiblockControllerMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {}

    @Override
    public boolean shouldRender(MultiblockControllerMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return false;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController controller, IMultiPart part,
                                Direction frontFacing, @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (this.fireboxIdleModel == null) {
            this.fireboxIdleModel = ModelUtils.getModelForState(fireboxIdle);
        }
        if (this.fireboxActiveModel == null) {
            this.fireboxActiveModel = ModelUtils.getModelForState(fireboxActive);
        }
        if (this.casingModel == null) {
            this.casingModel = ModelUtils.getModelForState(casing);
        }

        BlockPos partPos = part.self().getPos();

        MultiblockControllerMachine machine = controller.self();
        BlockPos controllerPos = machine.getPos();
        Direction multiFront = machine.getFrontFacing();
        Direction multiUpward = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();
        Direction relativeDown = RelativeDirection.DOWN.getRelative(multiFront, multiUpward, flipped);

        int belowControllerY = controllerPos.relative(relativeDown).get(relativeDown.getAxis());
        int partY = partPos.get(relativeDown.getAxis());
        if (belowControllerY == partY) {
            // firebox
            if (controller instanceof IRecipeLogicMachine rlm && rlm.getRecipeLogic().isWorking()) {
                emitQuads(quads, fireboxActiveModel, machine.getLevel(), partPos, fireboxActive,
                        side, rand, modelData, renderType);
            } else {
                emitQuads(quads, fireboxIdleModel, machine.getLevel(), partPos, fireboxIdle,
                        side, rand, modelData, renderType);
            }
        } else {
            // Not exactly one below the controller, so not a firebox
            emitQuads(quads, casingModel, machine.getLevel(), partPos, casing,
                    side, rand, modelData, renderType);
        }
    }

    private static void emitQuads(List<BakedQuad> quads, @Nullable BakedModel model,
                                  BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  @Nullable Direction side, RandomSource rand,
                                  ModelData modelData, @Nullable RenderType renderType) {
        if (model == null) return;
        modelData = model.getModelData(level, pos, state, modelData);
        quads.addAll(model.getQuads(state, side, rand, modelData, renderType));
    }
}
