package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

import static com.gregtechceu.gtceu.client.util.RenderUtil.*;

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

        var newVertices = new Vector3f[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            newVertices[i] = RenderUtil.transformVertex(vertices[i], face, offsetX, offsetY, offsetZ);
        }
        return newVertices;
    }

    public void drawBlocks(Set<BlockPos> offsets, PoseStack poseStack, VertexConsumer consumer,
                           Fluid fluid, RenderUtil.FluidTextureType texture,
                           int combinedOverlay, int combinedLight) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();

        for (var pos : offsets) {
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

            for (var direction : GTUtil.DIRECTIONS) {
                if (offsets.contains(pos.relative(direction))) continue;
                if (direction.getAxis() != Direction.Axis.Y) direction = direction.getOpposite();

                drawFace(poseStack.last(), consumer,
                        transformVertices(getVertices(direction), direction), getNormal(direction),
                        u0, u1, v0, v1, color, combinedOverlay, combinedLight);
            }
            poseStack.popPose();
        }
    }

    public void drawPlanes(Direction[] faces, Map<Direction, Collection<BlockPos>> directionalOffsets,
                           PoseStack poseStack, VertexConsumer consumer, Fluid fluid,
                           RenderUtil.FluidTextureType texture, int combinedOverlay, int combinedLight) {
        for (Direction face : faces) {
            if (!directionalOffsets.containsKey(face)) continue;

            drawPlane(face, directionalOffsets.get(face), poseStack, consumer,
                    fluid, texture, combinedOverlay, combinedLight);
        }
    }

    public void drawPlane(Direction face, Collection<BlockPos> offsets,
                          PoseStack poseStack, VertexConsumer consumer,
                          Fluid fluid, RenderUtil.FluidTextureType texture,
                          int combinedOverlay, BlockPos origin, @Nullable BlockAndTintGetter level) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        Vector3fc normal = getNormal(face);
        Vector3f[] vertices = transformVertices(getVertices(face), face);

        for (BlockPos offset : offsets) {
            poseStack.pushPose();
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
            drawFace(poseStack.last(), consumer, vertices, normal,
                    u0, u1, v0, v1, color,
                    combinedOverlay, RenderUtil.getFluidLight(fluid, origin.offset(offset), level));
            poseStack.popPose();
        }
    }

    public void drawPlane(Direction face, Collection<BlockPos> offsets, PoseStack poseStack, VertexConsumer consumer,
                          Fluid fluid, RenderUtil.FluidTextureType texture, int combinedOverlay, int combinedLight) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        Vector3fc normal = getNormal(face);
        Vector3f[] vertices = transformVertices(getVertices(face), face);

        for (var offset : offsets) {
            poseStack.pushPose();
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
            drawFace(poseStack.last(), consumer, vertices, normal,
                    u0, u1, v0, v1, color, combinedOverlay, combinedLight);
            poseStack.popPose();
        }
    }

    public void drawFace(Direction face, PoseStack.Pose pose, VertexConsumer consumer,
                         Fluid fluid, RenderUtil.FluidTextureType texture,
                         int combinedOverlay, int combinedLight) {
        var fluidClientInfo = IClientFluidTypeExtensions.of(fluid);
        var sprite = texture.map(fluidClientInfo);
        float u0 = sprite.getU0(), v0 = sprite.getV0(), u1 = sprite.getU1(), v1 = sprite.getV1();
        int color = fluidClientInfo.getTintColor();
        Vector3fc normal = getNormal(face);
        Vector3f[] vertices = transformVertices(getVertices(face), face);

        drawFace(pose, consumer, vertices, normal, u0, u1, v0, v1, color, combinedOverlay, combinedLight);
    }

    public void drawFace(PoseStack.Pose pose, VertexConsumer consumer, Vector3f[] vertices, Vector3fc normal,
                         float u0, float u1, float v0, float v1,
                         int argb, int combinedOverlay, int combinedLight) {
        if (properties.isOverwriteLight()) combinedLight = properties.getLight();

        RenderUtil.vertex(pose, consumer, vertices[0], normal, u0, v1, argb, combinedOverlay, combinedLight);
        RenderUtil.vertex(pose, consumer, vertices[1], normal, u0, v0, argb, combinedOverlay, combinedLight);
        RenderUtil.vertex(pose, consumer, vertices[2], normal, u1, v0, argb, combinedOverlay, combinedLight);
        RenderUtil.vertex(pose, consumer, vertices[3], normal, u1, v1, argb, combinedOverlay, combinedLight);
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
