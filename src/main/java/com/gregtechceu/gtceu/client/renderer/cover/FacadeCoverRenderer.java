package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.gregtechceu.gtceu.client.util.FacadeBlockAndTintGetter;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.common.item.FacadeItemBehaviour;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.client.model.data.ModelData;
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

    private static final ResourceLocation CABLE_TEXTURE = GTCEu.id("block/material_sets/dull/wire_side");

    public static final FacadeCoverRenderer INSTANCE = new FacadeCoverRenderer(null);
    private static final Int2ObjectMap<ItemBakedModel> CACHE = new Int2ObjectArrayMap<>();

    private final @Nullable BakedModel defaultItemModel;

    public FacadeCoverRenderer(@Nullable BakedModel defaultItemModel) {
        this.defaultItemModel = defaultItemModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData extraData,
                                             @Nullable RenderType renderType) {
        if (defaultItemModel != null) {
            return defaultItemModel.getQuads(state, side, rand, extraData, renderType);
        }
        return Collections.emptyList();
    }

    @Override
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
        List<BakedQuad> quads = new ArrayList<>();

        var mc = Minecraft.getInstance();
        if (mc.level != null) {
            BakedModel model = mc.getBlockRenderer().getBlockModel(state);
            if (!model.isCustomRenderer()) {
                var level = new FacadeBlockAndTintGetter(mc.level, BlockPos.ZERO, state, null);
                extraData = model.getModelData(level, BlockPos.ZERO, state, extraData);

                quads.addAll(model.getQuads(state, null, rand, extraData, renderType));
                quads.addAll(model.getQuads(state, Direction.NORTH, rand, extraData, renderType));

                TextureAtlasSprite cableSprite = ModelUtils.getBlockSprite(CABLE_TEXTURE);
                for (Direction modelSide : FACADE_EDGE_FACES) {
                    quads.add(StaticFaceBakery.bakeFace(FACADE_PLANE, modelSide, cableSprite,
                            BlockModelRotation.X0_Y0, -1, 0, false, true));
                }
            }
        }
        return quads;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, Direction modelFacing, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (coverBehavior instanceof FacadeCover facadeCover) {
            var state = facadeCover.getFacadeState();
            if (state.getRenderShape() == RenderShape.MODEL) {
                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
                ModelData extraData = model.getModelData(level, BlockPos.ZERO, state, modelData);

                if (side == coverBehavior.attachedSide) {
                    quads.addAll(model.getQuads(state, side, rand, extraData, renderType));
                } else if (side == null && coverBehavior.coverHolder.shouldRenderBackSide()) {
                    var normal = coverBehavior.attachedSide.getNormal();
                    var cube = new AABB(
                            normal.getX() >  0 ? 1 : 0,
                            normal.getY() >  0 ? 1 : 0,
                            normal.getZ() >  0 ? 1 : 0,
                            normal.getX() >= 0 ? 1 : 0,
                            normal.getY() >= 0 ? 1 : 0,
                            normal.getZ() >= 0 ? 1 : 0);
                    for (BakedQuad quad : model.getQuads(state, coverBehavior.attachedSide,
                            rand, extraData, renderType)) {
                        quads.add(StaticFaceBakery.bakeFace(cube, coverBehavior.attachedSide.getOpposite(),
                                quad.getSprite(), BlockModelRotation.X0_Y0,
                                quad.getTintIndex(), 0, false, quad.isShade()));
                    }
                }
            }
        }
    }
}
