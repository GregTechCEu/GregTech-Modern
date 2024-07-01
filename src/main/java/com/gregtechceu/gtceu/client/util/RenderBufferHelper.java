package com.gregtechceu.gtceu.client.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
                        buffer.addVertex(mat, x + sinTheta * dist, y + tubeRadius * sinPhi, z + cosTheta * dist)
                                .setColor(red, green, blue, alpha);
                        buffer.addVertex(mat, x + sinTheta1 * dist, y + tubeRadius * sinPhi, z + cosTheta1 * dist)
                                .setColor(red, green, blue, alpha);
                        break;
                    case X:
                        buffer.addVertex(mat, x + tubeRadius * sinPhi, y + sinTheta * dist, z + cosTheta * dist)
                                .setColor(red, green, blue, alpha);
                        buffer.addVertex(mat, x + tubeRadius * sinPhi, y + sinTheta1 * dist, z + cosTheta1 * dist)
                                .setColor(red, green, blue, alpha);
                        break;
                    case Z:
                        buffer.addVertex(mat, x + cosTheta * dist, y + sinTheta * dist, z + tubeRadius * sinPhi)
                                .setColor(red, green, blue, alpha);
                        buffer.addVertex(mat, x + cosTheta1 * dist, y + sinTheta1 * dist, z + tubeRadius * sinPhi)
                                .setColor(red, green, blue, alpha);
                        break;
                }

            }
            theta = theta1;
            cosTheta = cosTheta1;
            sinTheta = sinTheta1;

        }
    }
}
