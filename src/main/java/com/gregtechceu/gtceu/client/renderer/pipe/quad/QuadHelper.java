package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ForgeFaceData;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public final class QuadHelper {

    private QuadHelper() {}

    public static @NotNull RecolorableBakedQuad buildQuad(Direction normal, Pair<Vector3f, Vector3f> box,
                                                 @NotNull UVMapper uv, @NotNull SpriteInformation targetSprite) {
        BlockElementFace face = new BlockElementFace(null, -1, targetSprite.sprite().contents().name().toString(),
                uv.map(normal, box), ForgeFaceData.DEFAULT);
        return StaticFaceBakery.bakeRecolorableQuad(box.getLeft(), box.getRight(), face, targetSprite, normal,
                BlockModelRotation.X0_Y0, null, true, 0);
    }

    public static @NotNull BakedQuad buildQuad(Direction normal, Pair<Vector3f, Vector3f> box,
                                               @NotNull UVMapper uv, @NotNull TextureAtlasSprite targetSprite) {
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
}
