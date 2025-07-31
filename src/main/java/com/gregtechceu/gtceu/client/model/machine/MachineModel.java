package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;
import com.gregtechceu.gtceu.client.model.TextureOverrideModel;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartBakedModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.data.models.GTModels;

import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.machine.IMachineBlockEntity.*;

public final class MachineModel extends BaseBakedModel implements ICoverableRenderer,
                                IBlockEntityRendererBakedModel<BlockEntity> {

    public static final ResourceLocation PIPE_OVERLAY = GTCEu.id("block/overlay/machine/overlay_pipe");
    public static final ResourceLocation FLUID_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_fluid_output");
    public static final ResourceLocation ITEM_OUTPUT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_item_output");

    private static @Nullable TextureAtlasSprite pipeOverlaySprite;
    private static @Nullable TextureAtlasSprite fluidOutputOverlaySprite;
    private static @Nullable TextureAtlasSprite itemOutputOverlaySprite;
    private static @Nullable TextureAtlasSprite blankSprite;

    public static final Map<String, List<String>> TEXTURE_REMAPS = Util.make(new HashMap<>(), map -> {
        var all = List.of("all");

        map.put("side", all);
        map.put("top", all);
        map.put("bottom", all);
        map.put("all", List.of("side", "top", "bottom"));
    });

    @Getter
    private final MachineDefinition definition;
    private final Map<MachineRenderState, BakedModel> modelsByState;
    private final @Nullable MultiPartBakedModel multiPart;
    @Getter
    private final List<DynamicRender<?, ?>> dynamicRenders;

    @Getter
    private final ItemTransforms transforms;
    private final Transformation rootTransform;
    private final ModelState modelState;
    @Getter
    private final boolean isGui3d;
    @Accessors(fluent = true)
    @Getter
    private final boolean usesBlockLight, useAmbientOcclusion;

    @Setter
    private TextureAtlasSprite particleIcon = null;
    @Setter
    private Set<String> replaceableTextures;
    @Setter
    private Map<String, TextureAtlasSprite> textureOverrides;

    public MachineModel(MachineDefinition definition,
                        Map<MachineRenderState, BakedModel> modelsByState,
                        @Nullable MultiPartBakedModel multiPart,
                        List<DynamicRender<?, ?>> dynamicRenders,
                        ItemTransforms transforms, Transformation rootTransform, ModelState modelState,
                        boolean isGui3d, boolean usesBlockLight, boolean useAmbientOcclusion) {
        this.definition = definition;
        this.modelsByState = modelsByState;
        this.multiPart = multiPart;
        this.dynamicRenders = dynamicRenders;

        this.transforms = transforms;
        this.rootTransform = rootTransform;
        this.modelState = modelState;
        this.isGui3d = isGui3d;
        this.usesBlockLight = usesBlockLight;
        this.useAmbientOcclusion = useAmbientOcclusion;

        for (DynamicRender<?, ?> render : this.dynamicRenders) {
            render.setParent(this);
        }
    }

    public static void initSprites(TextureAtlas atlas) {
        pipeOverlaySprite = atlas.getSprite(PIPE_OVERLAY);
        fluidOutputOverlaySprite = atlas.getSprite(FLUID_OUTPUT_OVERLAY);
        itemOutputOverlaySprite = atlas.getSprite(ITEM_OUTPUT_OVERLAY);
        blankSprite = atlas.getSprite(GTModels.BLANK_TEXTURE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        if (particleIcon != null) {
            return particleIcon;
        } else if (multiPart != null) {
            return multiPart.getParticleIcon();
        } else if (!modelsByState.isEmpty()) {
            return modelsByState.get(getDefinition().defaultRenderState()).getParticleIcon();
        } else {
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(MissingTextureAtlasSprite.getLocation());
        }
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData) {
        BlockAndTintGetter level = modelData.get(MODEL_DATA_LEVEL);
        BlockPos pos = modelData.get(MODEL_DATA_POS);

        MetaMachine machine = (level == null || pos == null) ? null : MetaMachine.getMachine(level, pos);
        MachineRenderState renderState = machine != null ? machine.getRenderState() :
                getDefinition().defaultRenderState();

        if (multiPart != null) {
            return multiPart.getParticleIcon(renderState, modelData);
        } else if (modelsByState.containsKey(renderState)) {
            return modelsByState.get(renderState).getParticleIcon(modelData);
        } else {
            return super.getParticleIcon(modelData);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        ModelData.Builder builder = modelData.derive()
                .with(MODEL_DATA_LEVEL, level)
                .with(MODEL_DATA_POS, pos);
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        MachineRenderState renderState = machine == null ? definition.defaultRenderState() : machine.getRenderState();

        // add the inner model's model data too
        if (multiPart != null) {
            multiPart.addMachineModelData(renderState, level, pos, state, modelData, builder);
        }
        if (modelsByState.containsKey(renderState)) {
            ModelData data = modelsByState.get(renderState).getModelData(level, pos, state, modelData);
            for (ModelProperty key : data.getProperties()) {
                builder.with(key, data.get(key));
            }
        }
        return builder.build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData modelData, @Nullable RenderType renderType) {
        // If there is a root transform, undo the ModelState transform, apply it,
        // then re-apply the ModelState transform.
        // This is necessary because of things like UV locking, which should only respond to the ModelState,
        // and as such that is the only transform that should be applied during face bake.
        var postTransform = QuadTransformers.empty();
        if (!rootTransform.isIdentity()) {
            postTransform = UnbakedGeometryHelper.applyRootTransform(modelState, rootTransform);
        }

        List<BakedQuad> quads;
        if (modelData.has(MODEL_DATA_LEVEL) && modelData.has(MODEL_DATA_POS)) {
            quads = getMachineQuads(state, side, rand, modelData, renderType);
        } else {
            // if it doesn't have either of those properties, we're rendering an item.
            quads = renderMachine(null, null, null, state, side, rand, modelData, renderType);
        }
        postTransform.processInPlace(quads);
        return quads;
    }

    public List<BakedQuad> getMachineQuads(@Nullable BlockState blockState, @Nullable Direction side,
                                           @NotNull RandomSource rand, @NotNull ModelData modelData,
                                           @Nullable RenderType renderType) {
        BlockAndTintGetter level = modelData.get(MODEL_DATA_LEVEL);
        BlockPos pos = modelData.get(MODEL_DATA_POS);

        MetaMachine machine = (level == null || pos == null) ? null : MetaMachine.getMachine(level, pos);
        // render machine quads
        List<BakedQuad> quads = renderMachine(machine, level, pos, blockState, side, rand, modelData, renderType);
        if (machine == null) {
            return quads;
        }

        // render output overlays
        if (machine instanceof IAutoOutputItem autoOutputItem) {
            var itemFace = autoOutputItem.getOutputFacingItems();
            if (itemFace != null && side == itemFace) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY, side, pipeOverlaySprite));
                if (autoOutputItem.isAutoOutputItems()) {
                    quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY, side,
                            itemOutputOverlaySprite));
                }
            }
        }
        if (machine instanceof IAutoOutputFluid autoOutputFluid) {
            var fluidFace = autoOutputFluid.getOutputFacingFluids();
            if (fluidFace != null && side == fluidFace) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY, side, pipeOverlaySprite));
                if (autoOutputFluid.isAutoOutputFluids()) {
                    quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY, side,
                            fluidOutputOverlaySprite));
                }
            }
        }

        // render covers
        ICoverableRenderer.super.renderCovers(quads, machine.getCoverContainer(), pos, level,
                side, rand, modelData, renderType);
        return quads;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<BakedQuad> renderMachine(@Nullable MetaMachine machine, @Nullable BlockAndTintGetter level,
                                         @Nullable BlockPos pos, @Nullable BlockState blockState,
                                         @Nullable Direction side, RandomSource rand,
                                         @NotNull ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new LinkedList<>();

        MachineRenderState renderState = machine != null ? machine.getRenderState() : definition.defaultRenderState();
        renderBaseModel(quads, renderState, blockState, side, rand, modelData, renderType);

        for (DynamicRender render : dynamicRenders) {
            quads.addAll(render.getRenderQuads(machine, level, pos, blockState, side, rand, modelData, renderType));
        }
        // the instanceof check also ensures it's not null
        if (machine instanceof IMultiPart part && part.replacePartModelWhenFormed()) {
            quads = replacePartBaseModel(quads, part, machine.getFrontFacing(), side, rand, modelData, renderType);
        }

        // we have to recalculate CTM ourselves.
        // this is the slowest part by a long shot because the LDLib quad logic isn't very optimized.
        if (level != null && pos != null && blockState != null) {
            return CustomBakedModel.reBakeCustomQuads(quads, level, pos, blockState, side, 0.0f);
        }
        return quads;
    }

    public void renderBaseModel(List<BakedQuad> quads, @NotNull MachineRenderState renderState,
                                @Nullable BlockState blockState, @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (multiPart != null) {
            quads.addAll(multiPart.getMachineQuads(definition, renderState, blockState,
                    side, rand, modelData, renderType));
        }
        if (modelsByState.containsKey(renderState)) {
            quads.addAll(modelsByState.get(renderState).getQuads(blockState, side, rand, modelData, renderType));
        }
    }

    public List<BakedQuad> replacePartBaseModel(List<BakedQuad> originalQuads, IMultiPart part, Direction frontFacing,
                                                @Nullable Direction side, RandomSource rand,
                                                ModelData modelData, @Nullable RenderType renderType) {
        var controllers = part.getControllers();
        for (IMultiController controller : controllers) {
            var state = controller.self().getBlockState();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            List<BakedQuad> newQuads = null;

            // spotless:off
            if (model instanceof IControllerModelRenderer controllerRenderer) {
                controllerRenderer.renderPartModel(originalQuads, controller, part, frontFacing, side,
                        rand, modelData, renderType);
            } else if (model instanceof MachineModel controllerModel) {
                newQuads = renderPartOverrides(controllerModel, controller, originalQuads, part, frontFacing,
                        side, rand, modelData, renderType);
            }
            if (newQuads != null) {
                return newQuads;
            }
            // spotless:on
        }
        return originalQuads;
    }

    public List<String> remapReplaceableTextures(String key) {
        if (this.replaceableTextures.contains(key)) {
            return Collections.singletonList(key);
        } else {
            List<String> remapped = TEXTURE_REMAPS.get(key);
            if (remapped != null) return remapped;
            else return Collections.emptyList();
        }
    }

    private List<BakedQuad> renderPartOverrides(MachineModel controllerModel, IMultiController controller,
                                                List<BakedQuad> quads, IMultiPart part, Direction frontFacing,
                                                @Nullable Direction side, RandomSource rand,
                                                ModelData modelData, @Nullable RenderType renderType) {
        var overrides = controllerModel.textureOverrides;

        List<BakedQuad> renderQuads = new LinkedList<>();
        for (var render : controllerModel.getDynamicRenders()) {
            if (render instanceof IControllerModelRenderer controllerRenderer) {
                controllerRenderer.renderPartModel(renderQuads, controller, part, frontFacing, side,
                        rand, modelData, renderType);
                if (!renderQuads.isEmpty()) {
                    // assume the renderer drew the base model, and replace the override textures with empty ones
                    overrides = new HashMap<>();
                    for (String key : this.replaceableTextures) {
                        overrides.put(key, blankSprite);
                    }
                    break;
                }

            }
        }
        if (overrides.isEmpty()) {
            quads.addAll(renderQuads);
            return quads;
        }

        // parse out valid overrides
        Map<String, String> remaps = new IdentityHashMap<>();
        final TextureAtlasSprite missingno = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(MissingTextureAtlasSprite.getLocation());
        final Map<String, TextureAtlasSprite> finalOverrides = overrides;
        overrides = finalOverrides.keySet().stream()
                .flatMap(key -> {
                    var remapped = remapReplaceableTextures(key);
                    for (String r : remapped) {
                        remaps.put(r, key);
                    }
                    return remapped.stream();
                })
                .collect(Collectors.toMap(Function.identity(),
                        key -> finalOverrides.getOrDefault(remaps.get(key), missingno),
                        (o1, o2) -> o1));

        // actually process the sprite replacement
        quads = TextureOverrideModel.retextureQuads(quads, overrides);
        quads.addAll(renderQuads);
        return quads;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void render(@NotNull BlockEntity blockEntity, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE)) return;
        if (machineBE.getDefinition() != getDefinition()) return;
        ICoverableRenderer.super.renderDynamicCovers(machineBE.getMetaMachine(), partialTick, poseStack, buffer,
                packedLight,
                packedOverlay);
        if (dynamicRenders.isEmpty()) return;

        MetaMachine machine = machineBE.getMetaMachine();
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
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        AABB bounds = IBlockEntityRendererBakedModel.super.getRenderBoundingBox(blockEntity);

        if (!(blockEntity instanceof IMachineBlockEntity machineBE)) return bounds;
        if (machineBE.getDefinition() != getDefinition()) return bounds;
        if (dynamicRenders.isEmpty()) return bounds;

        MetaMachine machine = machineBE.getMetaMachine();
        for (DynamicRender model : dynamicRenders) {
            bounds = bounds.minmax(model.getRenderBoundingBox(machine));
        }
        return bounds;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE)) return false;
        if (machineBE.getDefinition() != getDefinition()) return false;
        if (dynamicRenders.isEmpty()) return false;

        MetaMachine machine = machineBE.getMetaMachine();
        for (DynamicRender render : dynamicRenders) {
            if (render.shouldRenderOffScreen(machine)) return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean shouldRender(BlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE)) return false;
        if (machineBE.getDefinition() != getDefinition()) return false;
        if (machineBE.getMetaMachine().getCoverContainer().hasDynamicCovers()) return true;
        if (dynamicRenders.isEmpty()) return false;

        MetaMachine machine = machineBE.getMetaMachine();
        for (DynamicRender model : dynamicRenders) {
            if (model.shouldRender(machine, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getViewDistance() {
        int distance = 0;
        for (DynamicRender<?, ?> model : dynamicRenders) {
            distance = Math.max(distance, model.getViewDistance());
        }
        return distance;
    }

    @Override
    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return getDefinition().getBlockEntityType();
    }
}
