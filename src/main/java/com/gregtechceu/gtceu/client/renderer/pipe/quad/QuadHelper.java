package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@OnlyIn(Dist.CLIENT)
public final class QuadHelper {

    private static final FaceBakery BAKERY = new FaceBakery();

    private QuadHelper() {}

    public static RecolorableBakedQuad buildQuad(Direction normal, Pair<Vector3f, Vector3f> box,
                                                 UVMapper uv, SpriteInformation targetSprite,
                                                 VertexFormat format) {
        BlockElementFace face = new BlockElementFace(null, -1, targetSprite.sprite().contents().name().toString(),
                uv.map(normal, box));
        BakedQuad quad = StaticFaceBakery.bakeQuad(box.getLeft(), box.getRight(), face, targetSprite.sprite(), normal,
                BlockModelRotation.X0_Y0, null, false, 15);
        RecolorableBakedQuad.Builder builder = new RecolorableBakedQuad.Builder(format);
        builder.setTexture(targetSprite);
        putBakedQuad(builder, quad);
        return builder.build();
    }

    public static BakedQuad buildQuad(Direction normal, Pair<Vector3f, Vector3f> box,
                                      UVMapper uv, TextureAtlasSprite targetSprite) {
        BlockElementFace face = new BlockElementFace(null, -1, targetSprite.contents().name().toString(),
                uv.map(normal, box));
        return StaticFaceBakery.bakeQuad(box.getLeft(), box.getRight(), face, targetSprite, normal,
                BlockModelRotation.X0_Y0,
                null, false, 15);
    }

    @Contract("_ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> toPair(@NotNull AABB bb) {
        return ImmutablePair.of(new Vector3f((float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16),
                new Vector3f((float) bb.maxX * 16, (float) bb.maxY * 16, (float) bb.maxZ * 16));
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> toPair(float x1, float y1, float z1, float x2, float y2,
                                                                    float z2) {
        return ImmutablePair.of(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> capOverlay(@Nullable Direction facing,
                                                                        @NotNull AABB bb, float g) {
        return capOverlay(facing, (float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16,
                (float) bb.maxX * 16, (float) bb.maxY * 16,
                (float) bb.maxZ * 16, g);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> capOverlay(@Nullable Direction facing, float x1, float y1,
                                                                        float z1, float x2, float y2, float z2,
                                                                        float g) {
        if (facing == null) return toPair(x1 - g, y1 - g, z1 - g, x2 + g, y2 + g, z2 + g);
        return switch (facing.getAxis()) {
            case X -> toPair(x1 - g, y1, z1, x2 + g, y2, z2);
            case Y -> toPair(x1, y1 - g, z1, x2, y2 + g, z2);
            case Z -> toPair(x1, y1, z1 - g, x2, y2, z2 + g);
        };
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> tubeOverlay(@Nullable Direction facing,
                                                                         @NotNull AABB bb, float g) {
        return tubeOverlay(facing, (float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16,
                (float) bb.maxX * 16, (float) bb.maxY * 16,
                (float) bb.maxZ * 16, g);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> tubeOverlay(@Nullable Direction facing, float x1,
                                                                         float y1, float z1, float x2, float y2,
                                                                         float z2, float g) {
        if (facing == null) return toPair(x1, y1, z1, x2, y2, z2);
        return switch (facing.getAxis()) {
            case X -> toPair(x1, y1 - g, z1 - g, x2, y2 + g, z2 + g);
            case Y -> toPair(x1 - g, y1, z1 - g, x2 + g, y2, z2 + g);
            case Z -> toPair(x1 - g, y1 - g, z1, x2 + g, y2 + g, z2);
        };
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> fullOverlay(@Nullable Direction facing,
                                                                         @NotNull AABB bb, float g) {
        return fullOverlay(facing, (float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16,
                (float) bb.maxX * 16, (float) bb.maxY * 16,
                (float) bb.maxZ * 16, g);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> fullOverlay(@Nullable Direction facing, float x1,
                                                                         float y1, float z1, float x2, float y2,
                                                                         float z2, float g) {
        return toPair(x1 - g, y1 - g, z1 - g, x2 + g, y2 + g, z2 + g);
    }

    public static void putBakedQuad(RecolorableBakedQuad.Builder consumer, BakedQuad quad) {
        consumer.setQuadOrientation(quad.getDirection());
        if (quad.isTinted()) {
            consumer.setQuadTint(quad.getTintIndex());
        }
        consumer.setShade(quad.isShade());
        int[] data = new int[4];
        VertexFormat formatFrom = consumer.getVertexFormat();
        VertexFormat formatTo = DefaultVertexFormat.BLOCK;
        int countFrom = formatFrom.getElements().size();
        int countTo = formatTo.getElements().size();
        int[] eMap = mapFormats(formatFrom, formatTo);
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < countFrom; e++) {
                if (eMap[e] != countTo) {
                    unpack(quad.getVertices(), data, formatTo, v, eMap[e]);
                    consumer.put(e, data);
                } else {
                    consumer.put(e);
                }
            }
        }
    }

    private static final ConcurrentMap<Pair<VertexFormat, VertexFormat>, int[]> formatMaps = new ConcurrentHashMap<>();

    public static int[] mapFormats(VertexFormat from, VertexFormat to) {
        return formatMaps.computeIfAbsent(Pair.of(from, to), pair -> generateMapping(pair.getLeft(), pair.getRight()));
    }

    private static int[] generateMapping(VertexFormat from, VertexFormat to) {
        int fromCount = from.getElements().size();
        int toCount = to.getElements().size();
        int[] eMap = new int[fromCount];

        for (int e = 0; e < fromCount; e++) {
            VertexFormatElement expected = from.getElements().get(e);
            int e2;
            for (e2 = 0; e2 < toCount; e2++) {
                VertexFormatElement current = to.getElements().get(e2);
                if (expected.getUsage() == current.getUsage() && expected.getIndex() == current.getIndex()) {
                    break;
                }
            }
            eMap[e] = e2;
        }
        return eMap;
    }

    public static void unpack(int[] from, int[] to, VertexFormat formatFrom, int v, int e) {
        int length = 4 < to.length ? 4 : to.length;
        VertexFormatElement element = formatFrom.getElements().get(e);
        int vertexStart = v * formatFrom.getIntegerSize() + formatFrom.getOffset(e);
        int count = element.getElementCount();
        VertexFormatElement.Type type = element.getType();
        int size = type.getSize();
        int mask = (256 << (8 * (size - 1))) - 1;
        for (int i = 0; i < length; i++) {
            if (i < count) {
                int pos = vertexStart + size * i;
                int index = pos >> 2;
                int offset = pos & 3;
                int bits = from[index];
                bits = bits >>> (offset * 8);
                if ((pos + size - 1) / 4 != index) {
                    bits |= from[index + 1] << ((4 - offset) * 8);
                }
                bits &= mask;
                if (type == VertexFormatElement.Type.FLOAT) {
                    to[i] = bits;
                } else if (type == VertexFormatElement.Type.UBYTE || type == VertexFormatElement.Type.USHORT) {
                    to[i] = Float.floatToRawIntBits((float) bits / mask);
                } else if (type == VertexFormatElement.Type.UINT) {
                    to[i] = Float.floatToRawIntBits((float) ((double) (bits & 0xFFFFFFFFL) / 0xFFFFFFFFL));
                } else if (type == VertexFormatElement.Type.BYTE) {
                    to[i] = Float.floatToRawIntBits(((float) (byte) bits) / (mask >> 1));
                } else if (type == VertexFormatElement.Type.SHORT) {
                    to[i] = Float.floatToRawIntBits(((float) (short) bits) / (mask >> 1));
                } else if (type == VertexFormatElement.Type.INT) {
                    to[i] = Float.floatToRawIntBits((float) ((double) (bits & 0xFFFFFFFFL) / (0xFFFFFFFFL >> 1)));
                }
            } else {
                to[i] = 0;
            }
        }
    }
}
