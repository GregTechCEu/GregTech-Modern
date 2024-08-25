package com.gregtechceu.gtceu.client.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

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
    public static void renderRing(PoseStack poseStack, VertexConsumer buffer, float x, float y, float z, float r,
                                  float tubeRadius,
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

    public static void renderCubeFrame(BufferBuilder buffer, double minX, double minY, double minZ, double maxX,
                                       double maxY, double maxZ, float r, float g, float b, float a) {
        buffer.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    public static void renderCubeFace(PoseStack poseStack, VertexConsumer buffer, AABB cuboid, float r, float g, float b, float a,
                                      boolean shade) {
        renderCubeFace(poseStack, buffer,
                (float) cuboid.minX, (float) cuboid.minY, (float) cuboid.minZ,
                (float) cuboid.maxX, (float) cuboid.maxY, (float) cuboid.maxZ,
                r, g, b, a, shade);
    }

    public static void renderCubeFace(PoseStack poseStack, VertexConsumer buffer,
                                      float minX, float minY, float minZ,
                                      float maxX, float maxY, float maxZ,
                                      float red, float green, float blue, float alpha) {
        renderCubeFace(poseStack, buffer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, false);
    }

    public static void renderCubeFace(PoseStack poseStack, VertexConsumer buffer,
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
}
