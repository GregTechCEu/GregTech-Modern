package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.client.model.FaceLayerCompositor;
import com.gregtechceu.gtceu.client.model.GTModelProperties;
import com.gregtechceu.gtceu.client.model.quad.MeshBuilder;
import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;
import com.gregtechceu.gtceu.client.model.quad.transform.QuadTransform;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.client.util.quad.transformers.QuadPositionForcer;
import com.gregtechceu.gtceu.client.util.quad.transformers.QuadReInterpolator;
import com.gregtechceu.gtceu.client.util.quad.transformers.QuadTinter;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FacadeCoverRenderer extends BaseBakedModel implements ICoverRenderer {

    private static final double FACADE_PLANE_BACK = 1.0 / 16;

    // spotless:off
    private static final Map<Direction, QuadTransform> FACADE_PLANE_TRANSFORMERS = createFacadePlaneTransformers(FACADE_PLANE_BACK);
    private static final Map<Direction, QuadTransform> FULL_BLOCK_HOLDER_FACADE_PLANE_TRANSFORMERS = createFacadePlaneTransformers(0);

    private static Map<Direction, QuadTransform> createFacadePlaneTransformers(double thickness) {
        Map<Direction, QuadTransform> transformers = new EnumMap<>(Direction.class);
        for (Direction dir : GTUtil.DIRECTIONS) {
            // All faces are slightly under a full block's size to never show the beginning of
            // the second row of pixels of the block's texture and to combat Z-fighting.
            transformers.put(dir, new QuadPositionForcer(StaticFaceBakery.createFaceCube(dir, thickness)));
        }
        return transformers;
    }
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
    public void renderCover(List<BakedQuad> quads, @Nullable Direction cullFace, RandomSource rand,
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
        ChunkRenderTypeSet facadeRenderTypes = facadeModel.getRenderTypes(facadeState, rand, facadeData);
        if (renderType != null && !facadeRenderTypes.contains(renderType)) {
            return;
        }

        Direction attachedSide = coverBehavior.attachedSide;
        if (cullFace != attachedSide && (cullFace != null || !coverBehavior.coverHolder.shouldRenderBackSide())) {
            return;
        }

        MeshBuilder meshBuilder = MeshBuilder.getInstance();
        var emitter = meshBuilder.getEmitter();

        boolean fullBlockHolder = coverBehavior.coverHolder.getCoverPlateThickness() <= 0;
        Map<Direction, QuadTransform> facadePlaneTransformers = fullBlockHolder ?
                FULL_BLOCK_HOLDER_FACADE_PLANE_TRANSFORMERS : FACADE_PLANE_TRANSFORMERS;
        QuadTransform clamper = facadePlaneTransformers.get(attachedSide);
        QuadReInterpolator interpolator = new QuadReInterpolator();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        // always add unculled faces
        Map<Direction, List<BakedQuad>> facadeQuads = new IdentityHashMap<>();
        facadeQuads.put(null, facadeModel.getQuads(facadeState, null, rand, facadeData, renderType));
        if (cullFace != null) {
            // if a cullface is given, only draw that + unculled faces
            facadeQuads.put(cullFace, facadeModel.getQuads(facadeState, cullFace, rand, facadeData, renderType));
        } else {
            // add all culled faces if no cullface is given
            for (Direction face : GTUtil.DIRECTIONS) {
                facadeQuads.put(face, facadeModel.getQuads(facadeState, face, rand, facadeData, renderType));
            }
        }
        // clamp all 'facaded' quads into a box and bake their tint color into the vertices
        for (var entry : facadeQuads.entrySet()) {
            Direction face = entry.getKey();
            List<BakedQuad> cullfaceQuads = entry.getValue();
            if (cullfaceQuads.isEmpty()) continue;

            for (BakedQuad quad : cullfaceQuads) {
                // skip quads that aren't oriented correctly
                if (quad.getDirection() != attachedSide && (fullBlockHolder || coverBehavior.shouldRenderPlate() ||
                        !coverBehavior.coverHolder.shouldRenderBackSide())) {
                    continue;
                }

                emitter.fromVanilla(quad, face);
                interpolator.setInputQuad(emitter);

                // bake the quad's colors into its vertices
                if (emitter.tintIndex() != -1) {
                    // if the quad has a tint index set, bake the tint into the vertex
                    int color = blockColors.getColor(facadeState, level, pos, quad.getTintIndex());
                    QuadTinter tinter = new QuadTinter(color);
                    tinter.transform(emitter);
                }

                // clamp the quad's vertices into the facade plane
                clamper.transform(emitter);
                // fix the quad's UVs based on the original & clamped vertices
                interpolator.transform(emitter);

                FaceLayer faceLayer = quad.gtceu$getFaceLayer() == FaceLayer.EMISSIVE ?
                        FaceLayer.COVER_EMISSIVE : FaceLayer.COVER;
                quads.add(emitter.toBlockBakedQuad().gtceu$setFaceLayer(faceLayer));
                emitter.emit();
            }
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
        if (facadeModel.isCustomRenderer()) {
            return ChunkRenderTypeSet.none();
        }
        return facadeModel.getRenderTypes(facadeState, rand, modelData);
    }

    static boolean occludesFullBlockFace(FacadeCover facade, ChunkRenderTypeSet renderTypes) {
        if (!facade.shouldRenderPlate() || renderTypes.isEmpty()) return false;

        for (RenderType renderType : renderTypes) {
            if (renderType != RenderType.solid()) return false;
        }
        return true;
    }

    static boolean rendersDynamically(FacadeCover facade, ChunkRenderTypeSet renderTypes) {
        // Only opaque facades can replace the hidden block face inside the baked chunk mesh.
        return !renderTypes.isEmpty() && !occludesFullBlockFace(facade, renderTypes);
    }

    public static boolean hasDynamicFullBlockFacade(MetaMachine machine) {
        if (machine.getCoverContainer().getCoverPlateThickness() > 0) return false;

        BlockAndTintGetter level = machine.getLevel();
        BlockPos pos = machine.getBlockPos();
        for (Direction face : GTUtil.DIRECTIONS) {
            CoverBehavior cover = machine.getCoverContainer().getCoverAtSide(face);
            if (!(cover instanceof FacadeCover facade)) continue;

            BlockState facadeState = facade.getFacadeState();
            RandomSource rand = RandomSource.create(facadeState.getSeed(pos));
            ModelData facadeData = INSTANCE.getModelData(cover, pos, level, ModelData.EMPTY);
            ChunkRenderTypeSet renderTypes = INSTANCE.getRenderTypes(cover, pos, level, rand, facadeData);
            if (rendersDynamically(facade, renderTypes)) return true;
        }
        return false;
    }

    void renderDynamicFullBlockFacade(FacadeCover facade, BlockPos pos, BlockAndTintGetter level,
                                      PoseStack poseStack, MultiBufferSource buffer, int packedOverlay) {
        BlockState facadeState = facade.getFacadeState();
        RandomSource rand = RandomSource.create(facadeState.getSeed(pos));
        ModelData facadeData = getModelData(facade, pos, level, ModelData.EMPTY);
        ChunkRenderTypeSet renderTypes = getRenderTypes(facade, pos, level, rand, facadeData);
        if (!rendersDynamically(facade, renderTypes)) return;

        var modelRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        BlockState holderState = level.getBlockState(pos);
        for (RenderType renderType : renderTypes) {
            rand.setSeed(facadeState.getSeed(pos));
            List<BakedQuad> quads = new ArrayList<>();
            renderCover(quads, facade.attachedSide, rand, facade, pos, level,
                    ModelData.EMPTY, renderType);
            if (quads.isEmpty()) continue;
            FaceLayerCompositor.composeCanonicalLayers(quads);

            RenderType facadeRenderType = GTRenderTypes.facade(renderType);
            modelRenderer.tesselateBlock(level, new DynamicFacadeModel(quads), holderState, pos, poseStack,
                    buffer.getBuffer(facadeRenderType), false, rand, holderState.getSeed(pos), packedOverlay,
                    ModelData.EMPTY, facadeRenderType);
        }
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

    private static final class DynamicFacadeModel extends BaseBakedModel {

        private final List<BakedQuad> quads;

        private DynamicFacadeModel(List<BakedQuad> quads) {
            this.quads = quads;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                        RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
            return side == null ? quads : Collections.emptyList();
        }
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
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction cullFace,
                                        RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
            if (this.quads.containsKey(cullFace)) {
                return this.quads.get(cullFace);
            }
            List<BakedQuad> quads = new LinkedList<>();
            this.quads.put(cullFace, quads);

            if (facadeState.getRenderShape() != RenderShape.MODEL) {
                return quads;
            }
            BakedModel facadeModel = RenderUtil.getModelForState(facadeState);
            if (facadeModel.isCustomRenderer()) {
                return quads;
            }

            ModelData facadeData = modelData.get(GTModelProperties.CHILD_MODEL_DATA);
            if (facadeData == null) facadeData = ModelData.EMPTY;

            MeshBuilder meshBuilder = MeshBuilder.getInstance();
            var emitter = meshBuilder.getEmitter();

            QuadTransform clamper = FACADE_PLANE_TRANSFORMERS.get(Direction.NORTH);
            QuadReInterpolator interpolator = new QuadReInterpolator();
            ItemColors itemColors = Minecraft.getInstance().getItemColors();

            for (var model : facadeModel.getRenderPasses(this.facadeStack, true)) {
                if (renderType != null && !model.getRenderTypes(facadeState, rand, facadeData).contains(renderType)) {
                    continue;
                }

                // always add unculled faces
                Map<Direction, List<BakedQuad>> facadeQuads = new IdentityHashMap<>();
                facadeQuads.put(null, model.getQuads(this.facadeState, null, rand, facadeData, renderType));
                if (cullFace != null) {
                    // if a cullface is given, only draw that + unculled faces
                    facadeQuads.put(cullFace, model.getQuads(this.facadeState, cullFace, rand, facadeData, renderType));
                } else {
                    // add all culled faces if no cullface is given
                    for (Direction face : GTUtil.DIRECTIONS) {
                        facadeQuads.put(face, model.getQuads(this.facadeState, face, rand, facadeData, renderType));
                    }
                }

                // clamp all 'facaded' quads into a box and bake their tint color into the vertices
                for (var entry : facadeQuads.entrySet()) {
                    Direction face = entry.getKey();
                    List<BakedQuad> cullfaceQuads = entry.getValue();
                    if (cullfaceQuads.isEmpty()) continue;

                    for (BakedQuad quad : cullfaceQuads) {
                        // skip quads that aren't oriented correctly
                        if (quad.getDirection() != Direction.NORTH) {
                            continue;
                        }

                        emitter.fromVanilla(quad, face);
                        interpolator.setInputQuad(emitter);

                        // bake the quad's colors into its vertices
                        if (emitter.tintIndex() != -1) {
                            // if the quad has a tint index set, bake the tint into the vertex
                            int color = itemColors.getColor(this.facadeStack, emitter.tintIndex());
                            QuadTinter tinter = new QuadTinter(color);
                            tinter.transform(emitter);
                        }

                        // clamp the quad's vertices into the facade plane
                        clamper.transform(emitter);
                        // fix the quad's UVs based on the original & clamped vertices
                        interpolator.transform(emitter);

                        FaceLayer faceLayer = quad.gtceu$getFaceLayer() == FaceLayer.EMISSIVE ?
                                FaceLayer.COVER_EMISSIVE : FaceLayer.COVER;
                        quads.add(emitter.toBlockBakedQuad().gtceu$setFaceLayer(faceLayer));
                        emitter.emit();
                    }
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
