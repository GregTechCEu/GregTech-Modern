package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
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
import java.util.Set;
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

    public static final Map<Direction, Vector3f[]> DIRECTION_POSITION_MAP = new HashMap<>() {

        {
            put(Direction.UP, new Vector3f[] { vec3f(0, 1, 1), vec3f(1, 1, 1), vec3f(1, 1, 0), vec3f(0, 1, 0) });
            put(Direction.DOWN, new Vector3f[] { vec3f(1, 0, 1), vec3f(0, 0, 1), vec3f(0, 0, 0), vec3f(1, 0, 0) });
            put(Direction.SOUTH, new Vector3f[] { vec3f(1, 1, 0), vec3f(1, 0, 0), vec3f(0, 0, 0), vec3f(0, 1, 0) });
            put(Direction.NORTH, new Vector3f[] { vec3f(0, 1, 1), vec3f(0, 0, 1), vec3f(1, 0, 1), vec3f(1, 1, 1) });
            put(Direction.EAST, new Vector3f[] { vec3f(0, 1, 0), vec3f(0, 0, 0), vec3f(0, 0, 1), vec3f(0, 1, 1) });
            put(Direction.WEST, new Vector3f[] { vec3f(1, 1, 1), vec3f(1, 0, 1), vec3f(1, 0, 0), vec3f(1, 1, 0) });
        }
    };

    public static final Map<Direction, Vector3f> DIRECTION_NORMAL_MAP = new HashMap<>() {

        {
            put(Direction.UP, vec3f(0, 1, 0));
            put(Direction.DOWN, vec3f(0, 1, 0));
            put(Direction.SOUTH, vec3f(0, 0, 1));
            put(Direction.NORTH, vec3f(0, 0, 1));
            put(Direction.EAST, vec3f(1, 0, 0));
            put(Direction.WEST, vec3f(1, 0, 0));
        }
    };

    public static void renderFluidBlockFace(Matrix4f pose, VertexConsumer vertexConsumer, Fluid fluid,
                                            FluidTextureType type, Direction face, int combinedOverlay,
                                            int combinedLight) {
        IClientFluidTypeExtensions fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = type.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        int r = FastColor.ARGB32.red(color), g = FastColor.ARGB32.green(color),
                b = FastColor.ARGB32.blue(color), a = FastColor.ARGB32.alpha(color);

        var norm = DIRECTION_NORMAL_MAP.get(face);
        var vertices = DIRECTION_POSITION_MAP.get(face);

        var vert = vertices[0];
        vertex(pose, vertexConsumer, vert.x, vert.y, vert.z, r, g, b, a, u0, v1, combinedOverlay, combinedLight, norm.x,
                norm.y, norm.z);
        vert = vertices[1];
        vertex(pose, vertexConsumer, vert.x, vert.y, vert.z, r, g, b, a, u0, v0, combinedOverlay, combinedLight, norm.x,
                norm.y, norm.z);
        vert = vertices[2];
        vertex(pose, vertexConsumer, vert.x, vert.y, vert.z, r, g, b, a, u1, v0, combinedOverlay, combinedLight, norm.x,
                norm.y, norm.z);
        vert = vertices[3];
        vertex(pose, vertexConsumer, vert.x, vert.y, vert.z, r, g, b, a, u1, v1, combinedOverlay, combinedLight, norm.x,
                norm.y, norm.z);
    }

    public static void renderFluidBlock(Matrix4f pose, VertexConsumer vertexConsumer, Fluid fluid,
                                        FluidTextureType type, int combinedOverlay, int combinedLight) {
        for (Direction face : Direction.values()) {
            renderFluidBlockFace(pose, vertexConsumer, fluid, type, face, combinedOverlay, combinedLight);
        }
    }

    public static void renderFluidBlocks(Matrix4f pose, VertexConsumer vertexConsumer, Fluid fluid,
                                         FluidTextureType type, int combinedOverlay, BlockPos origin,
                                         Set<Vector3f> offsets) {
        Vector3f prevOffset = null;
        for (var offset : offsets) {
            // get the translation offset
            Vector3f currOffset = prevOffset == null ? offset : offset.sub(prevOffset);
            // translate
            pose.translate(currOffset.x, currOffset.y, currOffset.z);
            // render block in position
            renderFluidBlock(pose, vertexConsumer, fluid, type, combinedOverlay,
                    getFluidLight(fluid, origin
                            .relative(Direction.Axis.X, (int) offset.x)
                            .relative(Direction.Axis.Y, (int) offset.y)
                            .relative(Direction.Axis.Z, (int) offset.z)));
            // update previous
            prevOffset = offset;
        }
    }

    public static int getFluidLight(Fluid fluid, BlockPos pos) {
        if (Minecraft.getInstance().level == null) return 0;
        return LevelRenderer.getLightColor(Minecraft.getInstance().level, fluid.defaultFluidState().createLegacyBlock(),
                pos);
    }

    public static void vertex(Matrix4f pose, VertexConsumer vertexConsumer, float x, float y, float z, int r, int g,
                              int b, int a, float u, float v, int overlayCoords, int lightOverlay, float v0, float v1,
                              float v2) {
        vertexConsumer
                .vertex(pose, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlayCoords)
                .uv2(lightOverlay)
                .normal(v0, v1, v2)
                .endVertex();
    }
}
