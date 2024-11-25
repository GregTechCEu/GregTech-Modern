package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {

    public enum FluidTextureType {

        STILL(fluidTypeExtensions -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidTypeExtensions.getStillTexture())),
        FLOWING(fluidTypeExtensions -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidTypeExtensions.getFlowingTexture())),
        OVERLAY(fluidTypeExtensions -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidTypeExtensions.getOverlayTexture()));

        private final Function<IClientFluidTypeExtensions, TextureAtlasSprite> mapper;

        FluidTextureType(Function<IClientFluidTypeExtensions, TextureAtlasSprite> mapper) {
            this.mapper = mapper;
        }

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions) {
            return mapper.apply(fluidTypeExtensions);
        }
    }

    public static Vec3 vec3(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static Vector3f vec3f(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    private static final Map<Direction, Vector3f[]> DIRECTION_POSITION_MAP = new HashMap<>() {

        {
            put(Direction.UP, new Vector3f[] { vec3f(0, 1, 1), vec3f(1, 1, 1), vec3f(1, 1, 0), vec3f(0, 1, 0) });
            put(Direction.DOWN, new Vector3f[] { vec3f(1, 0, 1), vec3f(0, 0, 1), vec3f(0, 0, 0), vec3f(1, 0, 0) });
            put(Direction.SOUTH, new Vector3f[] { vec3f(1, 1, 0), vec3f(1, 0, 0), vec3f(0, 0, 0), vec3f(0, 1, 0) });
            put(Direction.NORTH, new Vector3f[] { vec3f(0, 1, 1), vec3f(0, 0, 1), vec3f(1, 0, 1), vec3f(1, 1, 1) });
            put(Direction.EAST, new Vector3f[] { vec3f(0, 1, 0), vec3f(0, 0, 0), vec3f(0, 0, 1), vec3f(0, 1, 1) });
            put(Direction.WEST, new Vector3f[] { vec3f(1, 1, 1), vec3f(1, 0, 1), vec3f(1, 0, 0), vec3f(1, 1, 0) });
        }
    };

    public static Vector3f[] getVertices(Direction direction) {
        return DIRECTION_POSITION_MAP.get(direction);
    }

    private static final Map<Direction, Vector3f> DIRECTION_NORMAL_MAP = new HashMap<>() {

        {
            put(Direction.UP, vec3f(0, 1, 0));
            put(Direction.DOWN, vec3f(0, 1, 0));
            put(Direction.SOUTH, vec3f(0, 0, 1));
            put(Direction.NORTH, vec3f(0, 0, 1));
            put(Direction.EAST, vec3f(1, 0, 0));
            put(Direction.WEST, vec3f(1, 0, 0));
        }
    };

    public static Vector3f getNormal(Direction direction) {
        return DIRECTION_NORMAL_MAP.get(direction);
    }

    public static int getFluidLight(Fluid fluid, BlockPos pos) {
        if (Minecraft.getInstance().level == null) return 0;
        return LevelRenderer.getLightColor(Minecraft.getInstance().level, fluid.defaultFluidState().createLegacyBlock(),
                pos);
    }

    public static void vertex(Matrix4f pose, VertexConsumer vertexConsumer, float x, float y, float z, int r, int g,
                              int b, int a, float u, float v, int overlayCoords, int lightOverlay, float v0, float v1,
                              float v2) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer
                .vertex(pose, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlayCoords)
                .uv2(lightOverlay)
                .normal(v0, v1, v2)
                .endVertex();
    }

    public static Vector3f transformVertex(Vector3f vertex, Direction direction, float offsetX, float offsetY,
                                           float offsetZ) {
        float addX = offsetX, addY = offsetY, addZ = offsetZ;
        switch (direction) {
            case DOWN -> addY = -addY;
            case SOUTH -> addZ = -addZ;
            case EAST -> addX = -addX;
        }

        return vec3f(vertex.x + addX, vertex.y + addY, vertex.z + addZ);
    }
}
