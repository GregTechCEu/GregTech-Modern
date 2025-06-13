package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.model.machine.impl.IPartRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MachineModel extends BaseBakedModel implements ICoverableRenderer, IPartRenderer {

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    public static final ResourceLocation PIPE_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe");
    public static final ResourceLocation FLUID_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_fluid_output");
    public static final ResourceLocation ITEM_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_item_output");

    private final MachineDefinition definition;
    private final Map<MachineRenderState, BakedModel> baseModels = new IdentityHashMap<>();

    @Getter
    @Setter
    private TextureAtlasSprite particleIcon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(MissingTextureAtlasSprite.getLocation());

    public MachineModel(MachineDefinition definition, Map<MachineRenderState, BakedModel> baseModels) {
        this.definition = definition;
        this.baseModels.putAll(baseModels);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (modelData.has(LEVEL) && modelData.has(POS)) {
            return getMachineQuads(state, side, rand, modelData, renderType);
        } else {
            // if it doesn't have either of those properties, we're rendering an item.
            List<BakedQuad> quads = new ArrayList<>();
            renderMachine(quads, definition, null, Direction.NORTH,
                    side, rand, side, BlockModelRotation.X0_Y0, modelData, renderType);
            return quads;
        }
    }

    public List<BakedQuad> getMachineQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                           @NotNull ModelData modelData, @Nullable RenderType renderType) {
        BlockAndTintGetter level = modelData.get(LEVEL);
        BlockPos pos = modelData.get(POS);

        var machine = (level == null || pos == null) ? null : MetaMachine.getMachine(level, pos);
        if (machine != null) {
            Direction frontFacing = machine.getFrontFacing();
            MachineDefinition definition = machine.getDefinition();
            ModelState machineModelState = GTMatrixUtils.createRotationState(frontFacing,
                    MetaMachine.getUpwardFacing(machine));
            ModelState blockModelState = BaseBakedModel.getModelStateFromDirection(frontFacing);
            Direction modelFacing = side == null ? null : ModelFactory.modelFacing(side, frontFacing);
            List<BakedQuad> quads = new LinkedList<>();
            // render machine additional quads
            renderMachine(quads, definition, machine, frontFacing, side, rand,
                    modelFacing, machineModelState, modelData, renderType);

            // render auto IO
            if (machine instanceof IAutoOutputItem autoOutputItem) {
                var itemFace = autoOutputItem.getOutputFacingItems();
                if (itemFace != null && side == itemFace) {
                    quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY,
                            modelFacing, ModelFactory.getBlockSprite(PIPE_OVERLAY), blockModelState,
                            -1, 0, true, true));
                    if (autoOutputItem.isAutoOutputItems()) {
                        quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY,
                                modelFacing, ModelFactory.getBlockSprite(ITEM_OUTPUT_OVERLAY), blockModelState,
                                -101, 15, true, true));
                    }
                }
            }
            if (machine instanceof IAutoOutputFluid autoOutputFluid) {
                var fluidFace = autoOutputFluid.getOutputFacingFluids();
                if (fluidFace != null && side == fluidFace) {
                    quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY,
                            modelFacing, ModelFactory.getBlockSprite(PIPE_OVERLAY), blockModelState,
                            -1, 0, true, true));
                    if (autoOutputFluid.isAutoOutputFluids()) {
                        quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY,
                                modelFacing, ModelFactory.getBlockSprite(FLUID_OUTPUT_OVERLAY), blockModelState,
                                -101, 15, true, true));
                    }
                }
            }

            // render covers
            int start = quads.size();
            ICoverableRenderer.super.renderCovers(quads, side, rand, machine.getCoverContainer(), modelFacing,
                    pos, level, blockModelState);
            var iterator = quads.listIterator(start);
            while (iterator.hasNext()) {
                iterator.set(offsetQuad(iterator.next(), coverOverlayOffset()));
            }
            return quads;
        }
        return Collections.emptyList();
    }

    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState,
                              @NotNull ModelData modelData, RenderType renderType) {
        if (machine instanceof IMultiPart part && part.replacePartModelWhenFormed() &&
                renderReplacedPartMachine(quads, part, frontFacing, quadFace, rand, modelFacing,
                        modelState, modelData, renderType)) {
            return;
        }
        renderBaseModel(quads, definition, machine, modelState, quadFace, rand, modelData, renderType);
    }

    public void renderBaseModel(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                                ModelState modelState, @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, RenderType renderType) {
        if (machine == null) {
            return;
        }
        List<BakedQuad> q = baseModels.get(machine.getRenderState())
                .getQuads(definition.defaultBlockState(), side, rand, modelData, renderType);
        quads.addAll(q);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        ModelData.Builder builder = super.getModelData(level, pos, state, modelData)
                .derive()
                .with(LEVEL, level)
                .with(POS, pos);
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            machine.onModelDataUpdate(builder);
        }

        return builder.build();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    public boolean isGui3d() {
        return true;
    }

    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public float coverOverlayOffset() {
        return 0.008f;
    }
}
