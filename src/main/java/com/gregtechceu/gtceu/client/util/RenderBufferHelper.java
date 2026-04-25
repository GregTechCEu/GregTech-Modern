package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.EnumSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class RenderBufferHelper {

    /**
     *
     * Draw a ring torus
     *
     * @param poseStack  The stack used to store the transformation matrix.
     * @param buffer     Vertex consumer, which is used to cache vertex data.
     * @param x          The coordinates of the center
     * @param y          The coordinates of the center
     * @param z          The coordinates of the center
     * @param r          The large radius of the torus, that is, the distance from the center of the torus to center of
     *                   the "pipe".
     * @param tubeRadius The small radius of the "pipe", i.e. the thickness of the "pipe".
     * @param sides      The number of subdivisions of the "pipe".
     * @param segments   The number of subdivisions for the ring.
     * @param red        color
     * @param green      color
     * @param blue       color
     * @param alpha      transparency
     * @param axis       The axial direction of the "ring pipe" determines which axis the "ring pipe" rotates around.
     */
    public static void renderRing(PoseStack poseStack, VertexConsumer buffer, float x, float y, float z,
                                  float r, float tubeRadius,
                                  int sides, int segments, float red, float green, float blue, float alpha,
                                  Direction.Axis axis) {
        Matrix4f mat = poseStack.last().pose();
        float sideDelta = (float) (2.0 * Math.PI / sides); // Subdivision angle of the "pipe"
        float ringDelta = (float) (2.0 * Math.PI / segments); // Subdivision angle of the ring
        float theta = 0; // θ, sin(θ), cos(θ) Main angle
        float cosTheta = 1.0F;
        float sinTheta = 0.0F;

        float phi, sinPhi, cosPhi; // φ, sin(φ), cos(φ) Side angle
        float dist; // The distance from the point to the center of the ring pipe

        // Iterate through each subdivision of the ring
        for (int i = 0; i < segments; i++) {
            float theta1 = theta + ringDelta;
            float cosTheta1 = Mth.cos(theta1);
            float sinTheta1 = Mth.sin(theta1);

            // Iterate through each subdivision of the "pipe"
            phi = 0;
            for (int j = 0; j <= sides; j++) {
                phi = phi + sideDelta;
                cosPhi = Mth.cos(phi);
                sinPhi = Mth.sin(phi);
                dist = r + (tubeRadius * cosPhi);

                switch (axis) {
                    case Y:
                        buffer.vertex(mat, x + sinTheta * dist, y + tubeRadius * sinPhi, z + cosTheta * dist)
                                .color(red, green, blue, alpha).endVertex();
                        buffer.vertex(mat, x + sinTheta1 * dist, y + tubeRadius * sinPhi, z + cosTheta1 * dist)
                                .color(red, green, blue, alpha).endVertex();
                        break;
                    case X:
                        buffer.vertex(mat, x + tubeRadius * sinPhi, y + sinTheta * dist, z + cosTheta * dist)
                                .color(red, green, blue, alpha).endVertex();
                        buffer.vertex(mat, x + tubeRadius * sinPhi, y + sinTheta1 * dist, z + cosTheta1 * dist)
                                .color(red, green, blue, alpha).endVertex();
                        break;
                    case Z:
                        buffer.vertex(mat, x + cosTheta * dist, y + sinTheta * dist, z + tubeRadius * sinPhi)
                                .color(red, green, blue, alpha).endVertex();
                        buffer.vertex(mat, x + cosTheta1 * dist, y + sinTheta1 * dist, z + tubeRadius * sinPhi)
                                .color(red, green, blue, alpha).endVertex();
                        break;
                }

            }
            theta = theta1;
            cosTheta = cosTheta1;
            sinTheta = sinTheta1;

        }
    }

    public static void renderCube(VertexConsumer buffer, PoseStack.Pose pose,
                                  int color, int combinedLight, TextureAtlasSprite sprite,
                                  float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        renderCube(buffer, pose, EnumSet.allOf(Direction.class),
                color, combinedLight, sprite,
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static void renderCube(VertexConsumer buffer, PoseStack.Pose pose, Set<Direction> sidesToRender,
                                  int color, int combinedLight, TextureAtlasSprite sprite,
                                  float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        float uMin = sprite.getU0(), uMax = sprite.getU1(), vMin = sprite.getV0(), vMax = sprite.getV1();

        if (sidesToRender.contains(Direction.UP))
            renderCubeFace(buffer, pose, color, combinedLight, Direction.UP,
                    minX, maxY, minZ, uMin, vMax,
                    minX, maxY, maxZ, uMax, vMax,
                    maxX, maxY, maxZ, uMax, vMin,
                    maxX, maxY, minZ, uMin, vMin);

        if (sidesToRender.contains(Direction.DOWN))
            renderCubeFace(buffer, pose, color, combinedLight, Direction.DOWN,
                    minX, minY, minZ, uMin, vMax,
                    maxX, minY, minZ, uMax, vMax,
                    maxX, minY, maxZ, uMax, vMin,
                    minX, minY, maxZ, uMin, vMin);

        if (sidesToRender.contains(Direction.NORTH))
            renderCubeFace(buffer, pose, color, combinedLight, Direction.NORTH,
                    minX, minY, minZ, uMin, vMax,
                    minX, maxY, minZ, uMax, vMax,
                    maxX, maxY, minZ, uMax, vMin,
                    maxX, minY, minZ, uMin, vMin);

        if (sidesToRender.contains(Direction.SOUTH))
            renderCubeFace(buffer, pose, color, combinedLight, Direction.SOUTH,
                    minX, minY, maxZ, uMin, vMax,
                    maxX, minY, maxZ, uMax, vMax,
                    maxX, maxY, maxZ, uMax, vMin,
                    minX, maxY, maxZ, uMin, vMin);

        if (sidesToRender.contains(Direction.WEST))
            renderCubeFace(buffer, pose, color, combinedLight, Direction.WEST,
                    minX, minY, minZ, uMin, vMax,
                    minX, minY, maxZ, uMax, vMax,
                    minX, maxY, maxZ, uMax, vMin,
                    minX, maxY, minZ, uMin, vMin);

        if (sidesToRender.contains(Direction.EAST))
            renderCubeFace(buffer, pose, color, combinedLight, Direction.EAST,
                    maxX, minY, minZ, uMin, vMax,
                    maxX, maxY, minZ, uMax, vMax,
                    maxX, maxY, maxZ, uMax, vMin,
                    maxX, minY, maxZ, uMin, vMin);
    }

    public static void renderColorCube(PoseStack poseStack, VertexConsumer buffer, AABB cuboid,
                                       float r, float g, float b, float a, boolean shade) {
        renderColorCube(poseStack, buffer,
                (float) cuboid.minX, (float) cuboid.minY, (float) cuboid.minZ,
                (float) cuboid.maxX, (float) cuboid.maxY, (float) cuboid.maxZ,
                r, g, b, a, shade);
    }

    public static void renderColorCube(PoseStack poseStack, VertexConsumer buffer,
                                       float minX, float minY, float minZ,
                                       float maxX, float maxY, float maxZ,
                                       float red, float green, float blue, float alpha) {
        renderColorCube(poseStack, buffer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, false);
    }

    public static void renderColorCube(PoseStack poseStack, VertexConsumer buffer,
                                       float minX, float minY, float minZ,
                                       float maxX, float maxY, float maxZ,
                                       float red, float green, float blue, float a,
                                       boolean shade) {
        Matrix4f pose = poseStack.last().pose();
        float r = red, g = green, b = blue;

        if (shade) {
            r *= 0.6f;
            g *= 0.6f;
            b *= 0.6f;
        }
        buffer.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex();

        if (shade) {
            r = red * 0.5f;
            g = green * 0.5f;
            b = blue * 0.5f;
        }
        buffer.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex();

        if (shade) {
            r = red;
            g = green;
            b = blue;
        }
        buffer.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        if (shade) {
            r = red * 0.8f;
            g = green * 0.8f;
            b = blue * 0.8f;
        }
        buffer.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    public static void renderCubeFace(VertexConsumer buffer, PoseStack.Pose pose,
                                      int color, int combinedLight, Direction normalDir,
                                      float x1, float y1, float z1, float u1, float v1,
                                      float x2, float y2, float z2, float u2, float v2,
                                      float x3, float y3, float z3, float u3, float v3,
                                      float x4, float y4, float z4, float u4, float v4) {
        Vector3fc normal = GTMatrixUtils.getDirectionAxis(normalDir);

        Vector3f pos = new Vector3f(x1, y1, z1);
        RenderUtil.vertex(pose, buffer, pos, normal, u1, v1, color, OverlayTexture.NO_OVERLAY, combinedLight);

        pos.set(x2, y2, z2);
        RenderUtil.vertex(pose, buffer, pos, normal, u2, v2, color, OverlayTexture.NO_OVERLAY, combinedLight);

        pos.set(x3, y3, z3);
        RenderUtil.vertex(pose, buffer, pos, normal, u3, v3, color, OverlayTexture.NO_OVERLAY, combinedLight);

        pos.set(x4, y4, z4);
        RenderUtil.vertex(pose, buffer, pos, normal, u4, v4, color, OverlayTexture.NO_OVERLAY, combinedLight);
    }
}
