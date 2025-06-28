package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

import static com.gregtechceu.gtceu.client.util.RenderUtil.*;
import static net.minecraft.util.FastColor.ARGB32.*;

public class FluidBlockRenderer {

    public static final MapCodec<FluidBlockRenderer> CODEC = Properties.CODEC
            .xmap(FluidBlockRenderer::new, FluidBlockRenderer::getProperties);

    @Getter
    private final Properties properties;

    protected FluidBlockRenderer(Properties properties) {
        this.properties = properties;
    }

    public Vector3f[] transformVertices(Vector3fc[] vertices, Direction face) {
        float offsetX = properties.offsetX, offsetY = properties.offsetY, offsetZ = properties.offsetZ;

        switch (face.getAxis()) {
            case X -> offsetX += properties.offsetFace;
            case Y -> offsetY += properties.offsetFace;
            case Z -> offsetZ += properties.offsetFace;
        }

        var newVertices = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            newVertices[i] = RenderUtil.transformVertex(vertices[i], face, offsetX, offsetY, offsetZ);
        }
        return newVertices;
    }

    public void drawBlocks(Set<BlockPos> offsets, Matrix4f pose, VertexConsumer consumer,
                           Fluid fluid,
                           RenderUtil.FluidTextureType texture, int combinedOverlay, int combinedLight) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        int r = red(color), g = green(color), b = blue(color), a = alpha(color);

        for (var pos : offsets) {
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            for (var direction : Direction.values()) {
                if (offsets.contains(pos.relative(direction))) continue;
                if (direction != Direction.UP && direction != Direction.DOWN) direction = direction.getOpposite();
                drawFace(pose, consumer,
                        transformVertices(getVertices(direction), direction),
                        getNormal(direction),
                        u0, u1, v0, v1,
                        r, g, b, a,
                        combinedOverlay, combinedLight);
            }
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        }
    }

    public void drawPlanes(Direction[] faces, Map<Direction, Collection<BlockPos>> directionalOffsets, Matrix4f pose,
                           VertexConsumer consumer, Fluid fluid, RenderUtil.FluidTextureType texture,
                           int combinedOverlay, int combinedLight) {
        for (var face : faces) {
            if (!directionalOffsets.containsKey(face)) continue;
            drawPlane(face, directionalOffsets.get(face), pose, consumer, fluid, texture, combinedOverlay,
                    combinedLight);
        }
    }

    public void drawPlane(Direction face, Collection<BlockPos> offsets, Matrix4f pose, VertexConsumer consumer,
                          Fluid fluid, RenderUtil.FluidTextureType texture, int combinedOverlay, BlockPos origin) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        int r = red(color), g = green(color), b = blue(color), a = alpha(color);
        var normal = getNormal(face);
        var vertices = transformVertices(getVertices(face), face);

        BlockPos prevOffset = null;
        for (var offset : offsets) {
            BlockPos currOffset = prevOffset == null ? offset : offset.subtract(prevOffset);
            pose.translate(currOffset.getX(), currOffset.getY(), currOffset.getZ());
            drawFace(pose, consumer, vertices, normal, u0, u1, v0, v1, r, g, b, a, combinedOverlay,
                    RenderUtil.getFluidLight(fluid, origin.offset(currOffset)));
            prevOffset = offset;
        }
    }

    public void drawPlane(Direction face, Collection<BlockPos> offsets, Matrix4f pose, VertexConsumer consumer,
                          Fluid fluid, RenderUtil.FluidTextureType texture, int combinedOverlay, int combinedLight) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        int r = red(color), g = green(color), b = blue(color), a = alpha(color);
        var normal = getNormal(face);
        var vertices = transformVertices(getVertices(face), face);

        BlockPos prevOffset = null;
        for (var offset : offsets) {
            BlockPos currOffset = prevOffset == null ? offset : offset.subtract(prevOffset);
            pose.translate(currOffset.getX(), currOffset.getY(), currOffset.getZ());
            drawFace(pose, consumer, vertices, normal, u0, u1, v0, v1, r, g, b, a, combinedOverlay, combinedLight);
            prevOffset = offset;
        }
    }

    public void drawFace(Direction face, Matrix4f pose, VertexConsumer consumer, Fluid fluid,
                         RenderUtil.FluidTextureType texture, int combinedOverlay, int combinedLight) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        int r = red(color), g = green(color), b = blue(color), a = alpha(color);
        var normal = getNormal(face);
        var vertices = transformVertices(getVertices(face), face);
        drawFace(pose, consumer, vertices, normal, u0, u1, v0, v1, r, g, b, a, combinedOverlay, combinedLight);
    }

    public void drawFace(Matrix4f pose, VertexConsumer consumer, Vector3f[] vertices, Vector3fc normal,
                         float u0, float u1, float v0, float v1,
                         int r, int g, int b, int a,
                         int combinedOverlay, int combinedLight) {
        if (properties.isOverwriteLight()) combinedLight = properties.getLight();

        var vert = vertices[0];
        RenderUtil.vertex(pose, consumer, vert.x, vert.y, vert.z,
                r, g, b, a,
                u0, v1,
                combinedOverlay, combinedLight, normal.x(), normal.y(), normal.z());

        vert = vertices[1];
        RenderUtil.vertex(pose, consumer, vert.x, vert.y, vert.z,
                r, g, b, a,
                u0, v0,
                combinedOverlay, combinedLight, normal.x(), normal.y(), normal.z());

        vert = vertices[2];
        RenderUtil.vertex(pose, consumer, vert.x, vert.y, vert.z,
                r, g, b, a,
                u1, v0,
                combinedOverlay, combinedLight, normal.x(), normal.y(), normal.z());

        vert = vertices[3];
        RenderUtil.vertex(pose, consumer, vert.x, vert.y, vert.z,
                r, g, b, a,
                u1, v1,
                combinedOverlay, combinedLight, normal.x(), normal.y(), normal.z());
    }

    @Data
    public static class Properties {

        // spotless:off
        public static final MapCodec<Properties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("offset_x", 0.0f).forGetter(Properties::getOffsetX),
                Codec.FLOAT.optionalFieldOf("offset_y", 0.0f).forGetter(Properties::getOffsetY),
                Codec.FLOAT.optionalFieldOf("offset_z", 0.0f).forGetter(Properties::getOffsetZ),
                Codec.FLOAT.optionalFieldOf("offset_face", 0.0f).forGetter(Properties::getOffsetFace),
                Codec.BOOL.optionalFieldOf("overwrite_light", false).forGetter(Properties::isOverwriteLight),
                Codec.intRange(0, LightEngine.MAX_LEVEL).optionalFieldOf("block_light", 0).forGetter(Properties::getBlockLight),
                Codec.intRange(0, LightEngine.MAX_LEVEL).optionalFieldOf("sky_light", 0).forGetter(Properties::getSkyLight)
        ).apply(instance, Properties::of));
        // spotless:on

        private float offsetX = 0;
        private float offsetY = 0;
        private float offsetZ = 0;
        private float offsetFace = 0;
        private boolean overwriteLight = false;
        private int light = 0;

        public Properties() {}

        public static Properties of(float offsetX, float offsetY, float offsetZ, float offsetFace,
                                    boolean overwriteLight, int light) {
            Properties p = new Properties();
            p.setOffsetX(offsetX);
            p.setOffsetY(offsetY);
            p.setOffsetZ(offsetZ);
            p.setOffsetFace(offsetFace);
            p.setOverwriteLight(overwriteLight);
            p.setLight(light);
            return p;
        }

        private int getBlockLight() {
            return LightTexture.block(this.light);
        }

        private int getSkyLight() {
            return LightTexture.sky(this.light);
        }

        private static Properties of(float offsetX, float offsetY, float offsetZ, float offsetFace,
                                     boolean overwriteLight, int blockLight, int skyLight) {
            return of(offsetX, offsetY, offsetZ, offsetFace, overwriteLight, LightTexture.pack(blockLight, skyLight));
        }
    }

    public static class Builder {

        private final Properties properties;

        public Builder() {
            properties = new Properties();
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder setFaceOffset(float offset) {
            properties.setOffsetFace(offset);
            return this;
        }

        public Builder setOffset(Vector3f offset) {
            return setOffset(offset.x, offset.y, offset.z);
        }

        public Builder setOffset(float offsetX, float offsetY, float offsetZ) {
            properties.setOffsetX(offsetX);
            properties.setOffsetY(offsetY);
            properties.setOffsetZ(offsetZ);
            return this;
        }

        public Builder setForcedLight(int light) {
            properties.setLight(light);
            properties.setOverwriteLight(true);
            return this;
        }

        public Builder setForcedLight(int block, int sky) {
            properties.setLight(LightTexture.pack(block, sky));
            properties.setOverwriteLight(true);
            return this;
        }

        public FluidBlockRenderer getRenderer() {
            return new FluidBlockRenderer(properties);
        }
    }
}
