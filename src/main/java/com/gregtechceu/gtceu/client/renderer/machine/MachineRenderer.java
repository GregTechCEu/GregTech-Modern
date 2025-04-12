package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.utils.FacadeBlockAndTintGetter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote MachineRenderer
 */
public class MachineRenderer extends TextureOverrideRenderer
                             implements ICoverableRenderer, IPartRenderer, ICTMPredicate {

    public static final ResourceLocation PIPE_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe");
    public static final ResourceLocation FLUID_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_fluid_output");
    public static final ResourceLocation ITEM_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_item_output");

    public MachineRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useAO() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        if (stack.getItem() instanceof MetaMachineItem machineItem) {
            IItemRendererProvider.disabled.set(true);
            Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer,
                    combinedLight, combinedOverlay,
                    new ItemBakedModel() {

                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction,
                                                        RandomSource random) {
                            List<BakedQuad> quads = new LinkedList<>();
                            renderMachine(quads, machineItem.getDefinition(), null, Direction.NORTH, direction, random,
                                    direction, BlockModelRotation.X0_Y0);
                            return quads;
                        }
                    });
            IItemRendererProvider.disabled.set(false);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                             @Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        if (state != null && state.getBlock() instanceof MetaMachineBlock machineBlock) {
            var frontFacing = machineBlock.getFrontFacing(state);
            var machine = (level == null || pos == null) ? null : machineBlock.getMachine(level, pos);
            if (machine != null) {
                var definition = machine.getDefinition();
                var modelState = ModelFactory.getRotation(frontFacing);
                var modelFacing = side == null ? null : ModelFactory.modelFacing(side, frontFacing);
                var quads = new LinkedList<BakedQuad>();
                // render machine additional quads
                renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);

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

    @OnlyIn(Dist.CLIENT)
    public void renderBaseModel(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                                Direction frontFacing, @Nullable Direction side, RandomSource rand) {
        var rotation = machine == null || !machine.allowExtendedFacing() ?
                ModelFactory.getRotation(frontFacing) :
                GTMatrixUtils.createRotationState(frontFacing, machine.getUpwardsFacing());
        quads.addAll(getRotatedModel(rotation).getQuads(definition.defaultBlockState(), side, rand));
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
                              @Nullable Direction modelFacing, ModelState modelState) {
        if (!(machine instanceof IMultiPart part) || !part.replacePartModelWhenFormed() ||
                !renderReplacedPartMachine(quads, part, frontFacing, side, rand, modelFacing, modelState)) {
            renderBaseModel(quads, definition, machine, frontFacing, side, rand);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(PIPE_OVERLAY);
            register.accept(FLUID_OUTPUT_OVERLAY);
            register.accept(ITEM_OUTPUT_OVERLAY);
        }
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
        // var machine = MetaMachine.getMachine(level, pos);
        // if (machine != null) {
        // if (machine instanceof IMultiController controller && !controller.isFormed()) {
        // return false;
        // }
        // }
        return stateAppearance == sourceStateAppearance;
    }
}
