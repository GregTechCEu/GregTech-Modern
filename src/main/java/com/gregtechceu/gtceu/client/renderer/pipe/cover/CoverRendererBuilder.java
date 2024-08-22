package com.gregtechceu.gtceu.client.renderer.pipe.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.ColorQuadCache;
import com.gregtechceu.gtceu.client.renderer.pipe.cache.SubListAddress;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.QuadHelper;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.RecolorableBakedQuad;
import com.gregtechceu.gtceu.client.renderer.pipe.quad.UVMapper;
import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CoverRendererBuilder {

    private static final float OVERLAY_DIST_1 = 0.003f;
    private static final float OVERLAY_DIST_2 = 0.006f;

    private static final ColorQuadCache PLATE_QUADS;
    private static final EnumMap<Direction, SubListAddress> PLATE_COORDS = new EnumMap<>(Direction.class);

    public static final EnumMap<Direction, VoxelShape> PLATE_AABBS = new EnumMap<>(Direction.class);
    private static final EnumMap<Direction, Pair<Vector3f, Vector3f>> PLATE_BOXES = new EnumMap<>(Direction.class);
    private static final EnumMap<Direction, Pair<Vector3f, Vector3f>> OVERLAY_BOXES_1 = new EnumMap<>(
            Direction.class);
    private static final EnumMap<Direction, Pair<Vector3f, Vector3f>> OVERLAY_BOXES_2 = new EnumMap<>(
            Direction.class);

    private static final UVMapper defaultMapper = UVMapper.standard(0);

    static {
        for (Direction facing : GTUtil.DIRECTIONS) {
            PLATE_AABBS.put(facing, ICoverable.getCoverPlateBox(facing, 1d / 16));
        }
        for (var value : PLATE_AABBS.entrySet()) {
            // make sure that plates render slightly below any normal block quad
            // TODO replace .bounds() calls with actual VoxelShape support
            PLATE_BOXES.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue().bounds(), -OVERLAY_DIST_1));
            OVERLAY_BOXES_1.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue().bounds(), OVERLAY_DIST_1));
            OVERLAY_BOXES_2.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue().bounds(), OVERLAY_DIST_2));
        }
        PLATE_QUADS = buildPlates(new SpriteInformation(defaultPlateSprite(), 0));
    }

    private static @NotNull TextureAtlasSprite defaultPlateSprite() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(GTCEu.id("block/casings/voltage/lv/side"));
    }

    public static ColorQuadCache buildPlates(SpriteInformation sprite) {
        List<RecolorableBakedQuad> quads = new ObjectArrayList<>();
        UVMapper mapper = UVMapper.standard(0);
        for (Direction facing : GTUtil.DIRECTIONS) {
            PLATE_COORDS.put(facing, buildPlates(quads, facing, mapper, sprite));
        }
        return new ColorQuadCache(quads);
    }

    protected static SubListAddress buildPlates(List<RecolorableBakedQuad> quads, Direction facing,
                                                UVMapper mapper, SpriteInformation sprite) {
        int start = quads.size();
        Pair<Vector3f, Vector3f> box = PLATE_BOXES.get(facing);
        for (Direction dir : Direction.values()) {
            quads.add(QuadHelper.buildQuad(dir, box, mapper, sprite, DefaultVertexFormat.BLOCK));
        }
        return new SubListAddress(start, quads.size());
    }

    protected static void addPlates(List<BakedQuad> quads, List<BakedQuad> plateQuads, EnumSet<Direction> plates) {
        for (Direction facing : plates) {
            quads.add(plateQuads.get(facing.ordinal()));
        }
    }

    protected final ResourceLocation texture;
    protected final ResourceLocation textureEmissive;

    protected TextureAtlasSprite sprite;
    protected TextureAtlasSprite spriteEmissive;

    protected UVMapper mapper = defaultMapper;
    protected UVMapper mapperEmissive = defaultMapper;

    protected ColorQuadCache plateQuads = PLATE_QUADS;

    public CoverRendererBuilder(@NotNull ResourceLocation texture, @Nullable ResourceLocation textureEmissive) {
        this.texture = texture;
        this.textureEmissive = textureEmissive;
    }

    public CoverRendererBuilder setMapper(@NotNull UVMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public CoverRendererBuilder setMapperEmissive(@NotNull UVMapper mapperEmissive) {
        this.mapperEmissive = mapperEmissive;
        return this;
    }

    public CoverRendererBuilder setPlateQuads(ColorQuadCache cache) {
        this.plateQuads = cache;
        return this;
    }

    protected static List<BakedQuad> getPlates(Direction facing, ColorData data, ColorQuadCache plateQuads) {
        return PLATE_COORDS.get(facing).getSublist(plateQuads.getQuads(data));
    }

    public CoverRenderer build() {
        if (sprite == null) {
            sprite = ModelFactory.getBlockSprite(texture);
        }
        if (spriteEmissive == null && textureEmissive != null) {
            spriteEmissive = ModelFactory.getBlockSprite(textureEmissive);
        }

        EnumMap<Direction, Pair<BakedQuad, BakedQuad>> spriteQuads = new EnumMap<>(Direction.class);
        EnumMap<Direction, Pair<BakedQuad, BakedQuad>> spriteEmissiveQuads = textureEmissive != null ?
                new EnumMap<>(Direction.class) : null;
        for (Direction facing : GTUtil.DIRECTIONS) {
            spriteQuads.put(facing, ImmutablePair.of(
                    QuadHelper.buildQuad(facing, OVERLAY_BOXES_1.get(facing), mapper, sprite),
                    QuadHelper.buildQuad(facing.getOpposite(), OVERLAY_BOXES_1.get(facing), mapper, sprite)));
            if (textureEmissive != null) spriteEmissiveQuads.put(facing, ImmutablePair.of(
                    QuadHelper.buildQuad(facing, OVERLAY_BOXES_2.get(facing), mapperEmissive, spriteEmissive),
                    QuadHelper.buildQuad(facing.getOpposite(), OVERLAY_BOXES_2.get(facing), mapperEmissive,
                            spriteEmissive)));
        }

        return (quads, side, rand, renderPlate, renderBackside, data) -> {
            addPlates(quads, getPlates(side, data, plateQuads), renderPlate);
            quads.add(spriteQuads.get(side).getLeft());
            if (renderBackside) quads.add(spriteQuads.get(side).getRight());

            if (spriteEmissiveQuads != null) {
                quads.add(spriteEmissiveQuads.get(side).getLeft());
                if (renderBackside) quads.add(spriteEmissiveQuads.get(side).getRight());
            }
        };
    }
}
