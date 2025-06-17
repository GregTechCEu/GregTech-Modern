package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicMachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.IMachineBlockEntity.*;

public class MachineModel extends BaseBakedModel implements ICoverableRenderer, IPartModelRenderer, IMachineRendererModel<MetaMachine> {

    public static final float COVER_OVERLAY_OFFSET = 0.008f;

    public static final ResourceLocation PIPE_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe");
    public static final ResourceLocation FLUID_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_fluid_output");
    public static final ResourceLocation ITEM_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_item_output");

    @Getter
    private final MachineDefinition definition;
    private final Map<MachineRenderState, BakedModel> modelsByState = new IdentityHashMap<>();
    private final List<DynamicMachineRenderer<?>> dynamicRenderers = new ArrayList<>();

    @Getter
    @Setter
    private TextureAtlasSprite particleIcon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(MissingTextureAtlasSprite.getLocation());

    public MachineModel(MachineDefinition definition, Map<MachineRenderState, BakedModel> modelsByState) {
        this.definition = definition;
        this.modelsByState.putAll(modelsByState);
    }

    public MachineModel addDynamicRenderer(DynamicMachineRenderer<?> renderer) {
        if (renderer.getDefinition() != this.definition) {
            throw new IllegalArgumentException(
                    "Cannot add a dynamic renderer with a different machine type, input: %s; has %s"
                    .formatted(renderer.getDefinition().getId(), this.definition.getId()));
        }
        this.dynamicRenderers.add(renderer);
        return this;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (modelData.has(MODEL_DATA_LEVEL) && modelData.has(MODEL_DATA_POS)) {
            return getMachineQuads(state, side, rand, modelData, renderType);
        } else {
            // if it doesn't have either of those properties, we're rendering an item.
            List<BakedQuad> quads = new ArrayList<>();
            renderMachine(quads, definition, null, Direction.NORTH,
                    side, rand, side, BlockModelRotation.X0_Y0, modelData, renderType);
            return quads;
        }
    }

    public List<BakedQuad> getMachineQuads(@Nullable BlockState state, @Nullable Direction side,
                                           @NotNull RandomSource rand,
                                           @NotNull ModelData modelData, @Nullable RenderType renderType) {
        BlockAndTintGetter level = modelData.get(MODEL_DATA_LEVEL);
        BlockPos pos = modelData.get(MODEL_DATA_POS);

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
                iterator.set(offsetQuad(iterator.next(), COVER_OVERLAY_OFFSET));
            }
            return quads;
        }
        return Collections.emptyList();
    }

    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState,
                              @NotNull ModelData modelData, RenderType renderType) {
        if (machine instanceof IMultiPart part && part.replacePartModelWhenFormed()) {
            if (renderReplacedPartMachine(quads, part, frontFacing, quadFace, rand, modelFacing,
                    modelState, modelData, renderType)) {
                return;
            }
        }
        renderBaseModel(quads, definition, machine, modelState, quadFace, rand, modelData, renderType);
    }

    public void renderBaseModel(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                                ModelState modelState, @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, RenderType renderType) {
        if (machine == null) return;
        quads.addAll(modelsByState.get(machine.getRenderState())
                .getQuads(machine.getBlockState(), side, rand, modelData, renderType));
    }

    @Override
    public boolean isCustomRenderer() {
        if (dynamicRenderers.isEmpty()) return false;
        for (DynamicMachineRenderer<?> renderer : dynamicRenderers) {
            if (renderer.isCustomRenderer()) return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void render(MetaMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay, BlockEntityRendererProvider.Context context) {
        if (dynamicRenderers.isEmpty()) return;
        for (DynamicMachineRenderer model : dynamicRenderers) {
            if (!model.shouldRender(machine, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
                continue;
            }
            model.render(machine, partialTick, poseStack, buffer, packedLight, packedOverlay, context);
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (dynamicRenderers.isEmpty()) return;
        for (DynamicMachineRenderer<?> model : dynamicRenderers) {
            model.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public AABB getRenderBoundingBox(MetaMachine machine) {
        AABB bounds = IMachineRendererModel.super.getRenderBoundingBox(machine);
        if (dynamicRenderers.isEmpty()) return bounds;
        for (DynamicMachineRenderer model : dynamicRenderers) {
            bounds = bounds.minmax(model.getRenderBoundingBox(machine));
        }
        return bounds;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean shouldRenderOffScreen(MetaMachine machine) {
        if (dynamicRenderers.isEmpty()) return false;
        for (DynamicMachineRenderer renderer : dynamicRenderers) {
            if (renderer.shouldRenderOffScreen(machine)) return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean shouldRender(MetaMachine machine, Vec3 cameraPos) {
        if (dynamicRenderers.isEmpty()) return false;
        for (DynamicMachineRenderer model : dynamicRenderers) {
            if (model.shouldRender(machine, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
                return true;
            }
        }
        return false;
    }
}
