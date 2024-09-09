package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate;
import com.lowdragmc.lowdraglib.utils.FacadeBlockAndTintGetter;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote MachineRenderer
 */
public class MachineRenderer extends TextureOverrideRenderer
                             implements ICoverableRenderer, IPartRenderer, ICTMPredicate {

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    public static final ResourceLocation PIPE_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe");
    public static final ResourceLocation FLUID_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_fluid_output");
    public static final ResourceLocation ITEM_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_item_output");

    public MachineRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    @NotNull ModelData modelData, @Nullable RenderType renderType) {
        BlockAndTintGetter level = modelData.get(LEVEL);
        BlockPos pos = modelData.get(POS);

        if (state != null && state.getBlock() instanceof MetaMachineBlock machineBlock) {
            var frontFacing = machineBlock.getFrontFacing(state);
            var machine = (level == null || pos == null) ? null : machineBlock.getMachine(level, pos);
            if (machine != null) {
                var definition = machine.getDefinition();
                var modelState = ModelFactory.getRotation(frontFacing);
                var modelFacing = side == null ? null : ModelFactory.modelFacing(side, frontFacing);
                var quads = new LinkedList<BakedQuad>();
                // render machine additional quads
                renderMachine(quads, definition, machine, frontFacing, side, rand,
                        modelFacing, modelState, modelData, renderType);

                // render auto IO
                if (machine instanceof IAutoOutputItem autoOutputItem) {
                    var itemFace = autoOutputItem.getOutputFacingItems();
                    if (itemFace != null && side == itemFace) {
                        quads.add(
                                StaticFaceBakery.bakeFace(StaticFaceBakery.SLIGHTLY_OVER_BLOCK,
                                        modelFacing, ModelFactory.getBlockSprite(PIPE_OVERLAY),
                                        modelState, -1, 0, true, true));
                    }
                }
                if (machine instanceof IAutoOutputFluid autoOutputFluid) {
                    var fluidFace = autoOutputFluid.getOutputFacingFluids();
                    if (fluidFace != null && side == fluidFace) {
                        quads.add(
                                StaticFaceBakery.bakeFace(StaticFaceBakery.SLIGHTLY_OVER_BLOCK,
                                        modelFacing, ModelFactory.getBlockSprite(PIPE_OVERLAY), modelState,
                                        -1, 0, true, true));
                    }
                }

                if (machine instanceof IAutoOutputItem autoOutputItem) {
                    var itemFace = autoOutputItem.getOutputFacingItems();
                    if (itemFace != null && side == itemFace) {
                        if (autoOutputItem.isAutoOutputItems()) {
                            quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.SLIGHTLY_OVER_BLOCK,
                                    modelFacing, ModelFactory.getBlockSprite(ITEM_OUTPUT_OVERLAY), modelState,
                                    -101, 15, true, true));
                        }
                    }
                }

                if (machine instanceof IAutoOutputFluid autoOutputFluid) {
                    var fluidFace = autoOutputFluid.getOutputFacingFluids();
                    if (fluidFace != null && side == fluidFace) {
                        if (autoOutputFluid.isAutoOutputFluids()) {
                            quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.SLIGHTLY_OVER_BLOCK,
                                    modelFacing, ModelFactory.getBlockSprite(FLUID_OUTPUT_OVERLAY), modelState,
                                    -101, 15, true, true));
                        }
                    }
                }

                // render covers
                ICoverableRenderer.super.renderCovers(quads, side, rand, machine.getCoverContainer(), modelFacing, pos,
                        level, modelState);
                return quads;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBaseModel(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                                Direction frontFacing, @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, RenderType renderType) {
        quads.addAll(getBaseModel().getQuads(definition.defaultBlockState(), side, rand, modelData, renderType));
    }

    /**
     * Render machine block / item
     *
     * @param quads       quads pipeline
     * @param definition  machine definition
     * @param machine     if null, rendering item
     * @param frontFacing front facing
     * @param side        quad side
     * @param rand        random
     * @param modelFacing model facing before rotation
     * @param modelState  uvLocked rotation according to the front facing
     */
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState,
                              @NotNull ModelData modelData, RenderType renderType) {
        if (!(machine instanceof IMultiPart part) || !part.replacePartModelWhenFormed() ||
                !renderReplacedPartMachine(quads, part, frontFacing, side, rand, modelFacing, modelState, modelData,
                        renderType)) {
            renderBaseModel(quads, definition, machine, frontFacing, side, rand, modelData, renderType);
        }
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        return super.getModelData(level, pos, state, getBaseModel().getModelData(level, pos, state, modelData)).derive()
                .with(LEVEL, level)
                .with(POS, pos)
                .build();
    }

    //////////////////////////////////////
    // ********** CTM ***********//
    //////////////////////////////////////
    @Override
    public boolean isConnected(BlockAndTintGetter level, BlockState state, BlockPos pos, BlockState sourceState,
                               BlockPos sourcePos, Direction side) {
        var stateAppearance = FacadeBlockAndTintGetter.getAppearance(state, level, pos, side, sourceState, sourcePos);
        var sourceStateAppearance = FacadeBlockAndTintGetter.getAppearance(sourceState, level, sourcePos, side, state,
                pos);
        return stateAppearance == sourceStateAppearance;
    }
}
