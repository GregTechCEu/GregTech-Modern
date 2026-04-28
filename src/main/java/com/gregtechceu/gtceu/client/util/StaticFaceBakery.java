package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import com.mojang.math.Quadrant;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class StaticFaceBakery {

    private static final ModelBaker.Interner NOOP_INTERNER = new ModelBaker.Interner() {

        @Override
        public Vector3fc vector(Vector3fc vector) {
            return vector;
        }

        @Override
        public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo materialInfo) {
            return materialInfo;
        }
    };

    private static final ModelState IDENTITY_MODEL_STATE = new ModelState() {};

    private static final CuboidFace.UVs FULL_FACE_UVS = new CuboidFace.UVs(0.0F, 0.0F, 16.0F, 16.0F);

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f,
            1.001f, 1.001f, 1.001f);
    public static final AABB OUTPUT_OVERLAY = new AABB(-.006f, -.006f, -.006f,
            1.006f, 1.006f, 1.006f);
    public static final AABB AUTO_OUTPUT_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);
    public static final AABB COVER_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite) {
        return bakeFace(cube, face, sprite, -1, 0, true);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite, int tintIndex,
                                     int lightEmission, boolean shade) {
        Vector3f min = faceMin(cube, face);
        Vector3f max = faceMax(cube, face);
        BakedQuad.MaterialInfo materialInfo = BakedQuad.MaterialInfo.of(new Material.Baked(sprite, false),
                sprite.transparency(), tintIndex, shade, lightEmission);
        return FaceBakery.bakeQuad(NOOP_INTERNER, min, max, FULL_FACE_UVS, Quadrant.R0, materialInfo, face,
                IDENTITY_MODEL_STATE, null);
    }

    private static Vector3f faceMin(AABB cube, Direction face) {
        double minX = faceCoordinateMin(cube.minX, cube.maxX);
        double minY = faceCoordinateMin(cube.minY, cube.maxY);
        double minZ = faceCoordinateMin(cube.minZ, cube.maxZ);

        return switch (face) {
            case DOWN, UP -> toModelSpace(minX, facePlane(cube, face), minZ);
            case NORTH, SOUTH -> toModelSpace(minX, minY, facePlane(cube, face));
            case WEST, EAST -> toModelSpace(facePlane(cube, face), minY, minZ);
        };
    }

    private static Vector3f faceMax(AABB cube, Direction face) {
        double maxX = faceCoordinateMax(cube.minX, cube.maxX);
        double maxY = faceCoordinateMax(cube.minY, cube.maxY);
        double maxZ = faceCoordinateMax(cube.minZ, cube.maxZ);

        return switch (face) {
            case DOWN, UP -> toModelSpace(maxX, facePlane(cube, face), maxZ);
            case NORTH, SOUTH -> toModelSpace(maxX, maxY, facePlane(cube, face));
            case WEST, EAST -> toModelSpace(facePlane(cube, face), maxY, maxZ);
        };
    }

    private static double facePlane(AABB cube, Direction face) {
        return switch (face) {
            case DOWN -> cube.minY;
            case UP -> cube.maxY;
            case NORTH -> cube.minZ;
            case SOUTH -> cube.maxZ;
            case WEST -> cube.minX;
            case EAST -> cube.maxX;
        };
    }

    private static double faceCoordinateMin(double min, double max) {
        return coversFullBlock(min, max) ? 0.0D : min;
    }

    private static double faceCoordinateMax(double min, double max) {
        return coversFullBlock(min, max) ? 1.0D : max;
    }

    private static boolean coversFullBlock(double min, double max) {
        return min <= 0.0D && max >= 1.0D;
    }

    private static Vector3f toModelSpace(double x, double y, double z) {
        return new Vector3f((float) (x * 16.0F), (float) (y * 16.0F), (float) (z * 16.0F));
    }
}
