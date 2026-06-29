package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;

import static com.gregtechceu.gtceu.utils.GTMath.getFirstPerpendicular;
import static com.gregtechceu.gtceu.utils.GTMath.getSecondPerpendicular;

@SuppressWarnings("DuplicatedCode")
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

    public static void renderInWorldText(MultiBufferSource multiBuf, PoseStack stack, Camera camera, String text,
                                         int colorARGB, Vec3 pos) {
        renderInWorldText(multiBuf, stack, camera, text, 0.030F, colorARGB, pos);
    }

    public static void renderInWorldText(MultiBufferSource multiBuf, PoseStack stack, Camera camera, String text,
                                         float scale, int colorARGB, Vec3 pos) {
        Font fontRender = Minecraft.getInstance().font;
        Vec3 c = pos.subtract(camera.getPosition());
        float stringMiddle = (float) fontRender.width(text) / 2.0F;
        stack.pushPose();
        stack.translate(c.x, c.y, c.z);
        stack.mulPose(camera.rotation());
        stack.scale(-scale, -scale, scale);
        Matrix4f mat = stack.last().pose();
        fontRender.drawInBatch(text, -stringMiddle, 0.0F, colorARGB, false, mat, multiBuf, Font.DisplayMode.SEE_THROUGH,
                0, 15728880);
        stack.popPose();
    }

    public static void renderLine(VertexConsumer buf, PoseStack stack, BlockPos from, BlockPos to, double thickness,
                                  int colorARGB) {
        renderLine(buf, stack, from.getCenter(), to.getCenter(), thickness, colorARGB);
    }

    public static void renderLine(VertexConsumer buf, PoseStack stack, Vec3 from, Vec3 to, double thickness,
                                  int colorARGB) {
        Vec3 law = getFirstPerpendicular(from, to).scale(thickness);
        Vec3 law2 = getSecondPerpendicular(from, to).scale(thickness);
        Vec3 topRight = from.add(law2);
        Vec3 bottomRight = from.subtract(law);
        Vec3 bottomLeft = from.subtract(law2);
        Vec3 topLeft = from.add(law);
        Vec3 topRight2 = to.add(law2);
        Vec3 bottomRight2 = to.subtract(law);
        Vec3 bottomLeft2 = to.subtract(law2);
        Vec3 topLeft2 = to.add(law);

        renderSide(buf, stack, topRight, topLeft, bottomRight, bottomLeft, colorARGB);
        renderSide(buf, stack, topRight2, topRight, bottomRight2, bottomRight, colorARGB);
        renderSide(buf, stack, topLeft2, topRight2, bottomLeft2, bottomRight2, colorARGB);
        renderSide(buf, stack, topLeft, topLeft2, bottomLeft, bottomLeft2, colorARGB);
        renderSide(buf, stack, topLeft2, topRight2, topLeft, topRight, colorARGB);
        renderSide(buf, stack, bottomLeft2, bottomRight2, bottomLeft, bottomRight, colorARGB);
    }

    public static void renderCube(VertexConsumer buf, PoseStack stack, BlockPos pos, float size, int colorARGB) {
        renderCube(buf, stack, pos.getCenter(), size, colorARGB);
    }

    public static void renderCube(VertexConsumer buf, PoseStack stack, Vec3 pos, float size, int colorARGB) {
        float half = size / 2.0F;

        AABB box = new AABB(pos.x - (double) half, pos.y - (double) half, pos.z - (double) half, pos.x + (double) half,
                pos.y + (double) half, pos.z + (double) half);

        Vec3 topRight = new Vec3(box.maxX, box.maxY, box.maxZ);
        Vec3 bottomRight = new Vec3(box.maxX, box.minY, box.maxZ);
        Vec3 bottomLeft = new Vec3(box.minX, box.minY, box.maxZ);
        Vec3 topLeft = new Vec3(box.minX, box.maxY, box.maxZ);
        Vec3 topRight2 = new Vec3(box.maxX, box.maxY, box.minZ);
        Vec3 bottomRight2 = new Vec3(box.maxX, box.minY, box.minZ);
        Vec3 bottomLeft2 = new Vec3(box.minX, box.minY, box.minZ);
        Vec3 topLeft2 = new Vec3(box.minX, box.maxY, box.minZ);
        renderSide(buf, stack, topRight, topLeft, bottomRight, bottomLeft, colorARGB);
        renderSide(buf, stack, topRight2, topRight, bottomRight2, bottomRight, colorARGB);
        renderSide(buf, stack, topLeft2, topRight2, bottomLeft2, bottomRight2, colorARGB);
        renderSide(buf, stack, topLeft, topLeft2, bottomLeft, bottomLeft2, colorARGB);
        renderSide(buf, stack, topLeft2, topRight2, topLeft, topRight, colorARGB);
        renderSide(buf, stack, bottomLeft2, bottomRight2, bottomLeft, bottomRight, colorARGB);
    }

    public static void renderAABBOutline(VertexConsumer buf, PoseStack pose, AABB aabb, double thickness,
                                         int colorARGB) {
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;

        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        // Bottom face vertices
        Vec3 bot00 = new Vec3(minX, minY, minZ);
        Vec3 bot10 = new Vec3(maxX, minY, minZ);
        Vec3 bot11 = new Vec3(maxX, minY, maxZ);
        Vec3 bot01 = new Vec3(minX, minY, maxZ);

        // Top face vertices
        Vec3 top00 = new Vec3(minX, maxY, minZ);
        Vec3 top10 = new Vec3(maxX, maxY, minZ);
        Vec3 top11 = new Vec3(maxX, maxY, maxZ);
        Vec3 top01 = new Vec3(minX, maxY, maxZ);

        // Bottom rectangle edges
        renderLine(buf, pose, bot00, bot10, thickness, colorARGB);
        renderLine(buf, pose, bot10, bot11, thickness, colorARGB);
        renderLine(buf, pose, bot11, bot01, thickness, colorARGB);
        renderLine(buf, pose, bot01, bot00, thickness, colorARGB);

        // Top rectangle edges
        renderLine(buf, pose, top00, top10, thickness, colorARGB);
        renderLine(buf, pose, top10, top11, thickness, colorARGB);
        renderLine(buf, pose, top11, top01, thickness, colorARGB);
        renderLine(buf, pose, top01, top00, thickness, colorARGB);

        // Vertical edges
        renderLine(buf, pose, bot00, top00, thickness, colorARGB);
        renderLine(buf, pose, bot10, top10, thickness, colorARGB);
        renderLine(buf, pose, bot11, top11, thickness, colorARGB);
        renderLine(buf, pose, bot01, top01, thickness, colorARGB);
    }

    private static void renderSide(VertexConsumer buf, PoseStack pose, Vec3 tr, Vec3 tl, Vec3 br, Vec3 bl,
                                   int colorARGB) {
        Matrix4f mat = pose.last().pose();
        buf.vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z).color(colorARGB).endVertex();
        buf.vertex(mat, (float) br.x, (float) br.y, (float) br.z).color(colorARGB).endVertex();
        buf.vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z).color(colorARGB).endVertex();
        buf.vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z).color(colorARGB).endVertex();
    }

    public static void renderTexturedCube(VertexConsumer buffer, PoseStack.Pose pose, Set<Direction> sidesToRender,
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
        renderColorCube(buffer, poseStack,
                (float) cuboid.minX, (float) cuboid.minY, (float) cuboid.minZ,
                (float) cuboid.maxX, (float) cuboid.maxY, (float) cuboid.maxZ,
                r, g, b, a, shade);
    }

    public static void renderColorCube(PoseStack poseStack, VertexConsumer buffer,
                                       float minX, float minY, float minZ,
                                       float maxX, float maxY, float maxZ,
                                       float red, float green, float blue, float alpha) {
        renderColorCube(buffer, poseStack, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, false);
    }

    public static void renderColorCube(VertexConsumer buffer, PoseStack poseStack,
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
