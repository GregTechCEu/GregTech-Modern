package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
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
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class BoilerMultiPartRender extends DynamicRender<LargeBoilerMachine, BoilerMultiPartRender> implements IControllerModelRenderer {

    // spotless:off
    public static final Codec<BoilerMultiPartRender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("firebox_idle").forGetter(BoilerMultiPartRender::getFireboxIdle),
            BlockState.CODEC.fieldOf("firebox_active").forGetter(BoilerMultiPartRender::getFireboxActive),
            BlockState.CODEC.fieldOf("casing_block").forGetter(BoilerMultiPartRender::getCasing)
    ).apply(instance, BoilerMultiPartRender::new));
    public static final DynamicRenderType<LargeBoilerMachine, BoilerMultiPartRender> TYPE = new DynamicRenderType<>(BoilerMultiPartRender.CODEC);
    // spotless:on

    @Getter
    private final BlockState fireboxIdle, fireboxActive;
    @Getter
    private final BlockState casing;

    private BakedModel fireboxIdleModel, fireboxActiveModel;
    private BakedModel casingModel;

    public BoilerMultiPartRender(BoilerFireboxType fireboxType, Supplier<? extends Block> casingBlock) {
        this(GTBlocks.ALL_FIREBOXES.get(fireboxType).getDefaultState(),
                GTBlocks.ALL_FIREBOXES.get(fireboxType).getDefaultState().setValue(ActiveBlock.ACTIVE, true),
                casingBlock.get().defaultBlockState());
    }

    public BoilerMultiPartRender(BlockState fireboxIdle, BlockState fireboxActive, BlockState casing) {
        this.fireboxIdle = fireboxIdle;
        this.fireboxActive = fireboxActive;

        this.casing = casing;
        ModelUtils.registerBakeEventListener(event -> {
            this.fireboxIdleModel = event.getModels().get(BlockModelShaper.stateToModelLocation(this.fireboxIdle));
            this.fireboxActiveModel = event.getModels().get(BlockModelShaper.stateToModelLocation(this.fireboxActive));
            this.casingModel = event.getModels().get(BlockModelShaper.stateToModelLocation(this.casing));
        });
    }

    @Override
    public DynamicRenderType<LargeBoilerMachine, BoilerMultiPartRender> getType() {
        return TYPE;
    }

    @Override
    public void render(LargeBoilerMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay, BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRender(LargeBoilerMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing,
                                @Nullable Direction renderSide, RandomSource rand, Direction elementSide,
                                ModelState modelState, @NotNull ModelData modelData, RenderType renderType) {
        if (renderSide == null) {
            return;
        }
        BlockPos partPos = part.self().getPos();

        MultiblockControllerMachine controller = machine.self();
        BlockPos controllerPos = controller.getPos();
        Direction multiFront = controller.getFrontFacing();
        Direction multiUpward = controller.getUpwardsFacing();
        boolean flipped = controller.isFlipped();
        Direction relativeDown = RelativeDirection.DOWN.getRelativeFacing(multiFront, multiUpward, flipped);

        int controllerYMinus1 = controllerPos.relative(relativeDown).get(relativeDown.getAxis());
        int partY = partPos.get(relativeDown.getAxis());

        ModelState multiState = GTMatrixUtils.createRotationState(multiFront, multiUpward);
        renderSide = multiState.getRotation().rotateTransform(renderSide);
        // Not exactly one below the controller, so not a firebox
        if (controllerYMinus1 != partY) {
            emitQuads(quads, casingModel, controller.getLevel(), partPos, casing,
                    renderSide, multiState, rand, modelData, renderType);
            return;
        }
        // firebox
        if (machine instanceof IRecipeLogicMachine rlm && rlm.getRecipeLogic().isWorking()) {
            emitQuads(quads, fireboxActiveModel, controller.getLevel(), partPos, fireboxActive,
                    renderSide, multiState, rand, modelData, renderType);
        } else {
            emitQuads(quads, fireboxIdleModel, controller.getLevel(), partPos, fireboxIdle,
                    renderSide, multiState, rand, modelData, renderType);
        }
    }

    private static void emitQuads(List<BakedQuad> quads, @Nullable BakedModel model,
                                  BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  Direction renderFace, ModelState modelState,
                                  RandomSource rand, ModelData modelData, RenderType renderType) {
        if (model == null) return;
        modelData = model.getModelData(level, pos, state, modelData);
        // render both the culled & unculled quads
        quads.addAll(model.getQuads(state, null,
                rand, modelData, renderType));
        quads.addAll(model.getQuads(state, renderFace,
                rand, modelData, renderType));

    }

}
