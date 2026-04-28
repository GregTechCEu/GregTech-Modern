package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.gregtechceu.gtceu.client.model.TextureOverrideModel;
import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.model.compat.ItemTransforms;
import com.gregtechceu.gtceu.client.util.FacadeBlockAndTintGetter;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FacadeCoverRenderer extends BaseBakedModel implements ICoverRenderer {

    private static final AABB FACADE_PLANE = new AABB(0.01, 0.01, 0.01, 0.99, 0.99, 1 / 16f);
    private static final EnumSet<Direction> FACADE_EDGE_FACES = EnumSet.of(Direction.DOWN, Direction.UP,
            Direction.SOUTH, Direction.WEST, Direction.EAST);
    private static final Map<Direction, AABB> COVER_BACK_CUBES = Util.make(new EnumMap<>(Direction.class), map -> {
        for (Direction dir : GTUtil.DIRECTIONS) {
            var normal = dir.getUnitVec3i();
            var cube = new AABB(
                    normal.getX() > 0 ? 1.01 : -0.01,
                    normal.getY() > 0 ? 1.01 : -0.01,
                    normal.getZ() > 0 ? 1.01 : -0.01,
                    normal.getX() >= 0 ? 1.01 : -0.01,
                    normal.getY() >= 0 ? 1.01 : -0.01,
                    normal.getZ() >= 0 ? 1.01 : -0.01);
            map.put(dir, cube);
        }
    });

    public static final FacadeCoverRenderer INSTANCE = new FacadeCoverRenderer();
    private static final Int2ObjectMap<ItemBakedModel> CACHE = new Int2ObjectArrayMap<>();

    @OnlyIn(Dist.CLIENT)
    private @Nullable BakedModel defaultItemModel;

    private FacadeCoverRenderer() {}

    @OnlyIn(Dist.CLIENT)
    public FacadeCoverRenderer(@Nullable BakedModel defaultItemModel) {
        this.defaultItemModel = defaultItemModel;
    }

    public static void clearItemModelCache() {
        CACHE.clear();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData extraData,
                                             @Nullable RenderType renderType) {
        if (defaultItemModel != null) {
            return defaultItemModel.getQuads(state, side, rand, extraData, renderType);
        }
        return Collections.emptyList();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        if (!(stack.getItem() instanceof ComponentItem)) {
            return Collections.singletonList(this);
        }
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            return Collections.singletonList(this);
        }
        BlockState facadeState = FacadeItemBehaviour.getFacadeStateNullable(stack);
        if (facadeState == null) {
            return Collections.singletonList(this);
        }

        int hash = facadeState.hashCode();
        ItemBakedModel model = CACHE.computeIfAbsent(hash, $ -> new ItemBakedModel() {

            private final FacadeBlockAndTintGetter level = new FacadeBlockAndTintGetter(mc.level,
                    BlockPos.ZERO, facadeState, null);

            @Override
            public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                                     @NotNull RandomSource rand) {
                return getQuads(state, side, rand, ModelData.EMPTY, null);
            }

            @Override
            public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                                     @NotNull RandomSource rand, @NotNull ModelData extraData,
                                                     @Nullable RenderType renderType) {
                return getFacadeQuads(facadeState, level, rand, extraData, renderType);
            }
        });
        return Collections.singletonList(model);
    }

    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getFacadeQuads(BlockState state, BlockAndTintGetter level, @NotNull RandomSource rand,
                                          @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new LinkedList<>();
        BakedModel model = ModelUtils.getModelForState(state);

        if (!model.isCustomRenderer()) {
            extraData = model.getModelData(level, BlockPos.ZERO, state, extraData);

            List<BakedQuad> facadeQuads = new LinkedList<>();
            facadeQuads.addAll(model.getQuads(state, null, rand, extraData, renderType));
            facadeQuads.addAll(model.getQuads(state, Direction.NORTH, rand, extraData, renderType));

            // offset all the cover quads by a small value and bake their tint color into the vertices
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
            for (BakedQuad quad : facadeQuads) {
                if (!shouldRenderQuad(quad, renderType)) {
                    continue;
                }
                if (quad.materialInfo().isTinted()) {
                    // if the quad has a tint index set, bake the tint into the vertex
                    int color = getBlockTint(blockColors, state, level, BlockPos.ZERO,
                            quad.materialInfo().tintIndex());
                    quad = GTQuadTransformers.setColor(quad, color, true);
                } else {
                    // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                    quad = GTQuadTransformers.copy(quad);
                }
                quad = GTQuadTransformers.process(TextureOverrideModel.OVERLAY_OFFSET, quad);

                quads.add(quad);
            }

            for (Direction modelSide : FACADE_EDGE_FACES) {
                quads.add(StaticFaceBakery.bakeFace(FACADE_PLANE, modelSide, ICoverableRenderer.COVER_BACK_PLATE[0]));
            }
        }
        return quads;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (!(coverBehavior instanceof FacadeCover facadeCover)) {
            return;
        }
        BlockState state = facadeCover.getFacadeState();
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        Direction attachedSide = coverBehavior.attachedSide;

        BakedModel model = ModelUtils.getModelForState(state);
        ModelData extraData = model.getModelData(level, pos, state, modelData);

        List<BakedQuad> facadeQuads = model.getQuads(state, attachedSide, rand, extraData, renderType);
        facadeQuads = new LinkedList<>(facadeQuads);

        List<BakedQuad> coverQuads = new ArrayList<>();
        if (side == attachedSide) {
            coverQuads.addAll(facadeQuads);
        } else if (side == null && coverBehavior.coverHolder.shouldRenderBackSide()) {
            AABB cube = COVER_BACK_CUBES.get(attachedSide);

            for (BakedQuad quad : facadeQuads) {
                coverQuads.add(StaticFaceBakery.bakeFace(cube, attachedSide.getOpposite(),
                        quad.materialInfo().sprite(), quad.materialInfo().tintIndex(), 0, quad.materialInfo().shade()));
            }
        }

        // offset all the cover quads by a small value and bake their tint color into the vertices
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        for (BakedQuad quad : coverQuads) {
            if (!shouldRenderQuad(quad, renderType)) {
                continue;
            }
            if (quad.materialInfo().isTinted()) {
                // if the quad has a tint index set, bake the tint into the vertex
                int color = getBlockTint(blockColors, state, level, pos, quad.materialInfo().tintIndex());
                quad = GTQuadTransformers.setColor(quad, color, true);
            } else {
                // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                quad = GTQuadTransformers.copy(quad);
            }
            quad = GTQuadTransformers.process(TextureOverrideModel.OVERLAY_OFFSET, quad);

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
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData) {
        if (defaultItemModel != null) {
            return defaultItemModel.getParticleIcon(modelData);
        }
        return super.getParticleIcon();
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType,
                                              @NotNull PoseStack poseStack, boolean leftHand) {
        if (defaultItemModel != null) {
            defaultItemModel.applyTransform(transformType, poseStack, leftHand);
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull ItemTransforms getTransforms() {
        if (defaultItemModel != null) {
            return defaultItemModel.getTransforms();
        }
        return super.getTransforms();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        if (defaultItemModel != null) {
            return defaultItemModel.getOverrides();
        }
        return super.getOverrides();
    }

    private static boolean shouldRenderQuad(BakedQuad quad, @Nullable RenderType renderType) {
        return renderType == null || quad.materialInfo().itemRenderType() == renderType ||
                quad.materialInfo().layer().pipeline() == renderType.pipeline();
    }

    private static int getBlockTint(BlockColors blockColors, BlockState state, BlockAndTintGetter level,
                                    BlockPos pos, int tintIndex) {
        BlockTintSource tintSource = blockColors.getTintSource(state, tintIndex);
        return tintSource == null ? -1 : tintSource.colorInWorld(state, level, pos);
    }
}
