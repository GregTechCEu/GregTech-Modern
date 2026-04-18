package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
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
import net.minecraft.client.resources.model.BlockModelRotation;
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
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * It can only be used for item.
 * call it in other renderer to render a facade cover.
 */
public class FacadeCoverRenderer extends BaseBakedModel implements ICoverRenderer {

    public static final double THIN_OFFSET = 2e-3;

    // All faces are slightly under a full block's size to never show the beginning of
    // the second row of pixels of the block's texture and to combat Z-fighting.
    private static final AABB FACADE_PLANE = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0 / 16 + 0.002).deflate(THIN_OFFSET);
    private static final IQuadTransformer FACADE_PLANE_TRANSFORMER = GTQuadTransformers.clamp(FACADE_PLANE);

    private static final Map<Direction, AABB> COVER_BACK_CUBES = Util.make(new EnumMap<>(Direction.class), map -> {
        for (Direction dir : GTUtil.DIRECTIONS) {
            var normal = dir.getNormal();
            var cube = new AABB(
                    normal.getX() > 0 ? 1.001 : -0.001,
                    normal.getY() > 0 ? 1.001 : -0.001,
                    normal.getZ() > 0 ? 1.001 : -0.001,
                    normal.getX() >= 0 ? 1.001 : -0.001,
                    normal.getY() >= 0 ? 1.001 : -0.001,
                    normal.getZ() >= 0 ? 1.001 : -0.001);
            map.put(dir, cube);
        }
    });

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
        return Collections.singletonList(model);
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

        Direction attachedSide = coverBehavior.attachedSide;

        BakedModel facadeModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);
        ModelData extraData = facadeModel.getModelData(level, pos, facadeState, modelData);
        List<BakedQuad> facadeQuads = facadeModel.getQuads(facadeState, attachedSide, rand, extraData, renderType);

        List<BakedQuad> coverQuads = new ArrayList<>();
        if (side == attachedSide) {
            coverQuads.addAll(facadeQuads);
        } else if (side == null && coverBehavior.coverHolder.shouldRenderBackSide()) {
            AABB cube = COVER_BACK_CUBES.get(attachedSide);

            for (BakedQuad quad : facadeQuads) {
                // flatten all the
                coverQuads.add(FACADE_PLANE_TRANSFORMER.process(quad));
                coverQuads.add(StaticFaceBakery.bakeFace(cube, attachedSide.getOpposite(),
                        quad.getSprite(), BlockModelRotation.X0_Y0,
                        quad.getTintIndex(), 0, false, quad.isShade()));
            }
        }

        // offset all the cover quads by a small value and bake their tint color into the vertices
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        for (BakedQuad quad : coverQuads) {
            if (quad.isTinted()) {
                // if the quad has a tint index set, bake the tint into the vertex
                int color = blockColors.getColor(facadeState, level, pos, quad.getTintIndex());
                quad = GTQuadTransformers.setColor(quad, color, true);
            } else {
                // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                quad = GTQuadTransformers.copy(quad);
            }

            quads.add(quad);
        }
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

    private static class FacadeItemBakedModel extends BaseBakedModel {

        private final BakedModel parentModel;
        private final BlockState facadeState;
        private @Nullable List<BakedQuad> quads = null;

        private FacadeItemBakedModel(BakedModel parentModel, BlockState facadeState) {
            this.parentModel = parentModel;
            this.facadeState = facadeState;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                        RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
            if (this.quads != null) {
                return this.quads;
            }
            this.quads = new LinkedList<>(
                    this.parentModel.getQuads(this.facadeState, side, rand, extraData, renderType));

            BakedModel facadeModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(this.facadeState);
            if (facadeModel.isCustomRenderer()) {
                return this.quads;
            }

            List<BakedQuad> facadeQuads = new LinkedList<>();
            // noinspection CollectionAddAllCanBeReplacedWithConstructor this is cleaner
            facadeQuads.addAll(facadeModel.getQuads(this.facadeState, null, rand, extraData, renderType));
            // always add unculled faces
            if (side != null) {
                // if a cullface is given, only draw that + unculled faces
                facadeQuads.addAll(facadeModel.getQuads(this.facadeState, side, rand, extraData, renderType));
            } else {
                // add all culled faces if no cullface is given
                for (Direction cullFace : GTUtil.DIRECTIONS) {
                    facadeQuads.addAll(facadeModel.getQuads(this.facadeState, cullFace, rand, extraData, renderType));
                }
            }

            // clamp all 'facaded' quads into a box and bake their tint color into the vertices
            ItemColors itemColors = Minecraft.getInstance().getItemColors();
            ItemStack stack = null;
            for (BakedQuad quad : facadeQuads) {
                if (quad.isTinted()) {
                    // if the quad has a tint index set, bake the tint into the vertex color

                    // initialize `stack` lazily so we don't allocate it for no reason
                    if (stack == null) stack = this.facadeState.getBlock().asItem().getDefaultInstance();

                    int color = itemColors.getColor(stack, quad.getTintIndex());
                    // this also copies the quad
                    quad = GTQuadTransformers.setColor(quad, color, true);
                } else {
                    // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                    quad = GTQuadTransformers.copy(quad);
                }
                // the quad's already been copied by this point, so no need to copy it again.
                FACADE_PLANE_TRANSFORMER.processInPlace(quad);

                this.quads.add(quad);
            }
            return quads;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public TextureAtlasSprite getParticleIcon() {
            return this.parentModel.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }
}
