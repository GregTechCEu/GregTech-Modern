package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
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

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.IMachineBlockEntity.*;

public final class MachineModel extends BaseBakedModel implements ICoverableRenderer, IPartModelRenderer,
                                IMachineRendererModel<MetaMachine> {

    public static final float COVER_OVERLAY_OFFSET = 0.008f;

    public static final ResourceLocation PIPE_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe");
    public static final ResourceLocation FLUID_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_fluid_output");
    public static final ResourceLocation ITEM_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_item_output");

    @Getter
    private final MachineDefinition definition;
    private final Map<MachineRenderState, BakedModel> modelsByState;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;

    @Getter
    @Setter
    private TextureAtlasSprite particleIcon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(MissingTextureAtlasSprite.getLocation());

    public MachineModel(MachineDefinition definition,
                        Map<MachineRenderState, BakedModel> modelsByState,
                        List<DynamicRender<?, ?>> dynamicRenders) {
        this.definition = definition;
        this.modelsByState = modelsByState;
        this.dynamicRenders = dynamicRenders;

        for (DynamicRender<?, ?> render : this.dynamicRenders) {
            render.setParent(this);
        }
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
                    side, rand, side, modelData, renderType);
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

            ModelState blockModelState = ModelUtils.getModelStateFromDirection(frontFacing);
            Direction elementSide = side == null ? null : RelativeDirection.findRelativeOf(frontFacing, side).global;
            List<BakedQuad> quads = new LinkedList<>();
            // render machine additional quads
            renderMachine(quads, definition, machine, frontFacing, side, rand,
                    elementSide, modelData, renderType);

            // render auto IO
            if (machine instanceof IAutoOutputItem autoOutputItem) {
                var itemFace = autoOutputItem.getOutputFacingItems();
                if (itemFace != null && side == itemFace) {
                    quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY,
                            elementSide, ModelUtils.getBlockSprite(PIPE_OVERLAY), blockModelState,
                            -1, 0, true, true));
                    if (autoOutputItem.isAutoOutputItems()) {
                        quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY,
                                elementSide, ModelUtils.getBlockSprite(ITEM_OUTPUT_OVERLAY), blockModelState,
                                -101, 15, true, true));
                    }
                }
            }
            if (machine instanceof IAutoOutputFluid autoOutputFluid) {
                var fluidFace = autoOutputFluid.getOutputFacingFluids();
                if (fluidFace != null && side == fluidFace) {
                    quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY,
                            elementSide, ModelUtils.getBlockSprite(PIPE_OVERLAY), blockModelState,
                            -1, 0, true, true));
                    if (autoOutputFluid.isAutoOutputFluids()) {
                        quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY,
                                elementSide, ModelUtils.getBlockSprite(FLUID_OUTPUT_OVERLAY), blockModelState,
                                -101, 15, true, true));
                    }
                }
            }

            // render covers
            int start = quads.size();
            ICoverableRenderer.super.renderCovers(quads, side, rand, machine.getCoverContainer(), elementSide,
                    pos, level, blockModelState, modelData, renderType);
            var iterator = quads.listIterator(start);
            while (iterator.hasNext()) {
                iterator.set(ModelUtils.offsetQuad(iterator.next(), COVER_OVERLAY_OFFSET));
            }
            return quads;
        }
        return Collections.emptyList();
    }

    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction quadFace, RandomSource rand,
                              @Nullable Direction elementSide,
                              @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (machine == null) {
            quads.addAll(modelsByState.get(definition.defaultRenderState())
                    .getQuads(null, quadFace, rand, modelData, renderType));
            return;
        }

        if (!dynamicRenders.isEmpty()) {
            for (var render : dynamicRenders) {
                quads.addAll(render.getQuads(machine.getBlockState(), elementSide, rand, modelData, renderType));
            }
        }
        if (machine instanceof IMultiPart part && part.replacePartModelWhenFormed()) {
            if (renderReplacedPartMachine(quads, part, frontFacing, quadFace, rand, elementSide,
                    modelData, renderType)) {
                return;
            }
        }
        renderBaseModel(quads, machine, quadFace, rand, modelData, renderType);
    }

    public void renderBaseModel(List<BakedQuad> quads, @NotNull MetaMachine machine,
                                @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, @Nullable RenderType renderType) {
        quads.addAll(modelsByState.get(machine.getRenderState())
                .getQuads(machine.getBlockState(), side, rand, modelData, renderType));
    }

    @Override
    public boolean isCustomRenderer() {
        if (dynamicRenders.isEmpty()) return false;
        for (DynamicRender<?, ?> render : dynamicRenders) {
            if (render.isCustomRenderer()) return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void render(MetaMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (dynamicRenders.isEmpty()) return;
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        for (DynamicRender model : dynamicRenders) {
            if (!model.shouldRender(machine, cameraPos)) {
                continue;
            }
            model.render(machine, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (dynamicRenders.isEmpty()) return;
        for (DynamicRender<?, ?> model : dynamicRenders) {
            model.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public AABB getRenderBoundingBox(MetaMachine machine) {
        AABB bounds = IMachineRendererModel.super.getRenderBoundingBox(machine);
        if (dynamicRenders.isEmpty()) return bounds;
        for (DynamicRender model : dynamicRenders) {
            bounds = bounds.minmax(model.getRenderBoundingBox(machine));
        }
        return bounds;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean shouldRenderOffScreen(MetaMachine machine) {
        if (dynamicRenders.isEmpty()) return false;
        for (DynamicRender render : dynamicRenders) {
            if (render.shouldRenderOffScreen(machine)) return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean shouldRender(MetaMachine machine, Vec3 cameraPos) {
        if (dynamicRenders.isEmpty()) return false;
        for (DynamicRender model : dynamicRenders) {
            if (model.shouldRender(machine, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
                return true;
            }
        }
        return false;
    }
}
