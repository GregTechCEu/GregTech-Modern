package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.GTModelProperties;
import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FacadeCoverRenderer extends BaseBakedModel implements ICoverRenderer {

    private static final double FACADE_PLANE_BACK = 1.0 / 16;

    private static final AABB FACADE_PLANE = StaticFaceBakery.BLOCK.deflate(ICoverableRenderer.THIN_OFFSET);

    // spotless:off
    private static final Map<Direction, IQuadTransformer> FACADE_PLANE_TRANSFORMERS = Util.make(new EnumMap<>(Direction.class), map -> {
        for (Direction dir : GTUtil.DIRECTIONS) {
            // All faces are slightly under a full block's size to never show the beginning of
            // the second row of pixels of the block's texture and to combat Z-fighting.
            AABB facadePlane = switch (dir) {
                case DOWN -> FACADE_PLANE.setMaxY(FACADE_PLANE_BACK);
                case UP -> FACADE_PLANE.setMinY(FACADE_PLANE_BACK);
                case NORTH -> FACADE_PLANE.setMaxZ(FACADE_PLANE_BACK);
                case SOUTH -> FACADE_PLANE.setMinZ(FACADE_PLANE_BACK);
                case WEST -> FACADE_PLANE.setMaxX(FACADE_PLANE_BACK);
                case EAST -> FACADE_PLANE.setMinX(FACADE_PLANE_BACK);
            };

            map.put(dir, GTQuadTransformers.clamp(facadePlane));
        }
    });
    private static final IQuadTransformer FACADE_ITEM_PLANE_TRANSFORMER = FACADE_PLANE_TRANSFORMERS.get(Direction.NORTH);
    // spotless:on

    public static final FacadeCoverRenderer INSTANCE = new FacadeCoverRenderer();
    private static final Int2ObjectMap<BakedModel> CACHE = new Int2ObjectArrayMap<>();

    @OnlyIn(Dist.CLIENT)
    private @Nullable BakedModel defaultItemModel;

    private FacadeCoverRenderer() {}

    @OnlyIn(Dist.CLIENT)
    public FacadeCoverRenderer(@Nullable BakedModel parentModel) {
        this.defaultItemModel = parentModel;
    }

    public static void clearItemModelCache() {
        CACHE.clear();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        if (this.defaultItemModel != null) {
            return this.defaultItemModel.getQuads(state, side, rand, extraData, renderType);
        }
        return Collections.emptyList();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        if (this.defaultItemModel == null) {
            return Collections.emptyList();
        }
        BlockState facadeState = FacadeItemBehaviour.getFacadeStateNullable(stack);
        if (facadeState == null) {
            return Collections.singletonList(this);
        }

        int hash = facadeState.hashCode();
        BakedModel model = CACHE.computeIfAbsent(hash,
                $ -> new FacadeItemBakedModel(this.defaultItemModel, facadeState));
        return model.getRenderPasses(stack, fabulous);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            ModelData modelData, @Nullable RenderType renderType) {
        if (!(coverBehavior instanceof FacadeCover facadeCover)) {
            return;
        }
        BlockState facadeState = facadeCover.getFacadeState();
        if (facadeState.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        BakedModel facadeModel = RenderUtil.getModelForState(facadeState);
        if (facadeModel.isCustomRenderer()) {
            return;
        }

        ModelData facadeData = facadeModel.getModelData(level, pos, facadeState, modelData);
        if (renderType != null && !facadeModel.getRenderTypes(facadeState, rand, facadeData).contains(renderType)) {
            return;
        }

        Direction attachedSide = coverBehavior.attachedSide;
        if (side != attachedSide && (side != null || !coverBehavior.coverHolder.shouldRenderBackSide())) {
            return;
        }

        IQuadTransformer clamper = FACADE_PLANE_TRANSFORMERS.get(attachedSide);
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        // always add unculled faces
        List<BakedQuad> facadeQuads = new LinkedList<>(
                facadeModel.getQuads(facadeState, null, rand, facadeData, renderType));
        if (side != null) {
            // if a cullface is given, only draw that + unculled faces
            facadeQuads.addAll(facadeModel.getQuads(facadeState, side, rand, facadeData, renderType));
        } else {
            // add all culled faces if no cullface is given
            for (Direction cullFace : GTUtil.DIRECTIONS) {
                facadeQuads.addAll(facadeModel.getQuads(facadeState, cullFace, rand, facadeData, renderType));
            }
        }

        for (BakedQuad quad : facadeQuads) {
            // bake the quad's colors into its vertices
            if (quad.isTinted()) {
                // if the quad has a tint index set, bake the tint into the vertex
                int color = blockColors.getColor(facadeState, level, pos, quad.getTintIndex());
                quad = GTQuadTransformers.setColor(quad, color, true);
            } else {
                // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                quad = GTQuadTransformers.copy(quad);
            }

            // clamp the quad's vertex positions to fit into the facade plane
            clamper.processInPlace(quad);

            quads.add(quad);
        }
    }

    @Override
    public ModelData getModelData(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                  ModelData holderModelData) {
        if (!(coverBehavior instanceof FacadeCover facadeCover)) {
            return ModelData.EMPTY;
        }
        BlockState facadeState = facadeCover.getFacadeState();
        if (facadeState.getRenderShape() != RenderShape.MODEL) {
            return ModelData.EMPTY;
        }

        BakedModel facadeModel = RenderUtil.getModelForState(facadeState);
        return facadeModel.getModelData(level, pos, facadeState, holderModelData);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                             RandomSource rand, ModelData modelData) {
        if (!(coverBehavior instanceof FacadeCover facadeCover)) {
            return ChunkRenderTypeSet.none();
        }
        BlockState facadeState = facadeCover.getFacadeState();
        if (facadeState.getRenderShape() != RenderShape.MODEL) {
            return ChunkRenderTypeSet.none();
        }

        BakedModel facadeModel = RenderUtil.getModelForState(facadeState);
        return facadeModel.getRenderTypes(facadeState, rand, modelData);
    }

    @Override
    public boolean shouldRenderBackPlateForSide(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                                @Nullable Direction side) {
        // skip rendering the cover baseplate for the attachment side
        return side != coverBehavior.attachedSide;
    }

    @Override
    public boolean useAmbientOcclusion() {
        if (defaultItemModel != null) {
            return defaultItemModel.useAmbientOcclusion();
        }
        return super.useAmbientOcclusion();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData) {
        if (defaultItemModel != null) {
            return defaultItemModel.getParticleIcon(modelData);
        }
        return super.getParticleIcon();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean leftHand) {
        if (defaultItemModel != null) {
            defaultItemModel.applyTransform(transformType, poseStack, leftHand);
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemTransforms getTransforms() {
        if (defaultItemModel != null) {
            return defaultItemModel.getTransforms();
        }
        return super.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        if (defaultItemModel != null) {
            return defaultItemModel.getOverrides();
        }
        return super.getOverrides();
    }

    private static class FacadeItemBakedModel extends BakedModelWrapper<BakedModel> implements IDynamicBakedModel {

        private final BlockState facadeState;
        private final Map<Direction, List<BakedQuad>> quads = new IdentityHashMap<>();

        private final ItemStack facadeStack;

        private FacadeItemBakedModel(BakedModel parentModel, BlockState facadeState) {
            super(parentModel);
            this.facadeState = facadeState;

            this.facadeStack = this.facadeState.getBlock().asItem().getDefaultInstance();;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return IDynamicBakedModel.super.getQuads(state, side, rand);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                        RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
            if (this.quads.containsKey(side)) {
                return this.quads.get(side);
            }
            List<BakedQuad> quads = new LinkedList<>();
            this.quads.put(side, quads);

            if (facadeState.getRenderShape() != RenderShape.MODEL) {
                return quads;
            }
            BakedModel facadeModel = RenderUtil.getModelForState(facadeState);
            if (facadeModel.isCustomRenderer()) {
                return quads;
            }

            ModelData facadeData = modelData.get(GTModelProperties.CHILD_MODEL_DATA);
            if (facadeData == null) facadeData = ModelData.EMPTY;

            ItemColors itemColors = Minecraft.getInstance().getItemColors();

            for (var model : facadeModel.getRenderPasses(this.facadeStack, true)) {
                if (renderType != null && !model.getRenderTypes(facadeState, rand, facadeData).contains(renderType)) {
                    continue;
                }

                // always add unculled faces
                List<BakedQuad> facadeQuads = new LinkedList<>(
                        model.getQuads(this.facadeState, null, rand, facadeData, renderType));
                if (side != null) {
                    // if a cullface is given, only draw that + unculled faces
                    facadeQuads.addAll(model.getQuads(this.facadeState, side, rand, facadeData, renderType));
                } else {
                    // add all culled faces if no cullface is given
                    for (Direction cullFace : GTUtil.DIRECTIONS) {
                        facadeQuads.addAll(model.getQuads(this.facadeState, cullFace, rand, facadeData, renderType));
                    }
                }

                // clamp all 'facaded' quads into a box and bake their tint color into the vertices
                for (BakedQuad quad : facadeQuads) {
                    // bake the quad's colors into its vertices
                    if (quad.isTinted()) {
                        // if the quad has a tint index set, bake the tint into the vertex color
                        int color = itemColors.getColor(this.facadeStack, quad.getTintIndex());
                        // this also copies the quad
                        quad = GTQuadTransformers.setColor(quad, color, true);
                    } else {
                        // otherwise just copy the quad so we don't mutate the original model with the clamping
                        quad = GTQuadTransformers.copy(quad);
                    }

                    // clamp the quad's vertex positions to fit into the facade plane
                    FACADE_ITEM_PLANE_TRANSFORMER.processInPlace(quad);

                    quads.add(quad);
                }
            }

            return quads;
        }

        @Override
        public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
            return super.getModelData(level, pos, state, modelData).derive()
                    .with(GTModelProperties.CHILD_MODEL_DATA,
                            RenderUtil.getModelForState(facadeState).getModelData(level, pos, state, modelData))
                    .build();
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
            List<RenderType> renderTypes = new ArrayList<>();

            BakedModel facadeModel = RenderUtil.getModelForState(this.facadeState);
            for (var model : facadeModel.getRenderPasses(stack, fabulous)) {
                renderTypes.addAll(model.getRenderTypes(this.facadeStack, fabulous));
            }

            return renderTypes;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(originalModel, this);
        }
    }
}
