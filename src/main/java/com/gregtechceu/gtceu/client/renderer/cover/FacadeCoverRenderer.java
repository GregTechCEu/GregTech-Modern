package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.gregtechceu.gtceu.client.model.TextureOverrideModel;
import com.gregtechceu.gtceu.client.util.FacadeBlockAndTintGetter;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.common.item.FacadeItemBehaviour;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * It can only be used for item.
 * call it in other renderer to render a facade cover.
 */
public class FacadeCoverRenderer extends BaseBakedModel implements ICoverRenderer {

    private static final AABB FACADE_PLANE = new AABB(0.01, 0.01, 0.01, 0.99, 0.99, 1 / 16f);
    private static final EnumSet<Direction> FACADE_EDGE_FACES = EnumSet.of(Direction.DOWN, Direction.UP,
            Direction.SOUTH, Direction.WEST, Direction.EAST);
    private static final Map<Direction, AABB> COVER_BACK_CUBES = Util.make(new EnumMap<>(Direction.class), map -> {
        for (Direction dir : GTUtil.DIRECTIONS) {
            var normal = dir.getNormal();
            var cube = new AABB(
                    normal.getX() > 0 ? 1.005 : 0.005,
                    normal.getY() > 0 ? 1.005 : 0.005,
                    normal.getZ() > 0 ? 1.005 : 0.005,
                    normal.getX() >= 0 ? 1.005 : 0.005,
                    normal.getY() >= 0 ? 1.005 : 0.005,
                    normal.getZ() >= 0 ? 1.005 : 0.005);
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
        BlockState facadeState = FacadeItemBehaviour.getFacadeStateNullable(stack);
        if (facadeState == null) {
            return Collections.singletonList(this);
        }

        int hash = facadeState.hashCode();
        ItemBakedModel model = CACHE.computeIfAbsent(hash, $ -> new ItemBakedModel() {

            @Override
            public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                                     @NotNull RandomSource rand) {
                return getQuads(state, side, rand, ModelData.EMPTY, null);
            }

            @Override
            public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                                     @NotNull RandomSource rand, @NotNull ModelData extraData,
                                                     @Nullable RenderType renderType) {
                return getFacadeQuads(facadeState, rand, extraData, renderType);
            }
        });
        return Collections.singletonList(model);
    }

    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getFacadeQuads(BlockState state, @NotNull RandomSource rand,
                                          @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();
        BakedModel model = mc.getBlockRenderer().getBlockModel(state);

        if (!model.isCustomRenderer()) {
            var level = new FacadeBlockAndTintGetter(mc.level, BlockPos.ZERO, state, null);
            extraData = model.getModelData(level, BlockPos.ZERO, state, extraData);

            quads.addAll(model.getQuads(state, null, rand, extraData, renderType));
            quads.addAll(model.getQuads(state, Direction.NORTH, rand, extraData, renderType));

            quads = new ArrayList<>(TextureOverrideModel.OVERLAY_OFFSET.process(quads));

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

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        ModelData extraData = model.getModelData(level, BlockPos.ZERO, state, modelData);

        List<BakedQuad> facadeQuads = model.getQuads(state, attachedSide, rand, extraData, renderType);
        facadeQuads = new ArrayList<>(facadeQuads);

        List<BakedQuad> coverQuads = new ArrayList<>();
        if (side == attachedSide) {
            coverQuads.addAll(facadeQuads);
        } else if (side == null && coverBehavior.coverHolder.shouldRenderBackSide()) {
            AABB cube = COVER_BACK_CUBES.get(attachedSide);

            for (BakedQuad quad : facadeQuads) {
                coverQuads.add(StaticFaceBakery.bakeFace(cube, attachedSide.getOpposite(),
                        quad.getSprite(), BlockModelRotation.X0_Y0,
                        quad.getTintIndex(), 0, false, quad.isShade()));
            }
        }
        // offset all the cover quads by a small value and bake their tint color into the vertices
        for (BakedQuad quad : coverQuads) {
            TextureOverrideModel.OVERLAY_OFFSET.processInPlace(quad);

            quads.add(quad);
        }
    }
}
