package com.lowdragmc.lowdraglib.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

public class RenderUtils {

    public static void useScissor(int x, int y, int width, int height, Runnable runnable) {
        runnable.run();
    }

    public static void useScissor(PoseStack poseStack, int x, int y, int width, int height, Runnable runnable) {
        runnable.run();
    }

    public static void useStencil(Runnable mask, Runnable render, boolean invert) {
        mask.run();
        render.run();
    }

    public static void renderBlockOverLay(PoseStack poseStack, BlockPos pos, float r, float g, float b, float a) {}

    public static void renderCubeFace(PoseStack poseStack, BufferBuilder buffer, float x0, float y0, float z0,
                                      float x1, float y1, float z1, float u0, float v0, float u1, float v1) {}

    public static void moveToFace(PoseStack poseStack, double x, double y, double z, Direction face) {
        com.lowdragmc.lowdraglib2.client.utils.RenderUtils.moveToFace(poseStack, x, y, z, face);
    }

    public static void rotateToFace(PoseStack poseStack, Direction face, Direction spin) {
        com.lowdragmc.lowdraglib2.client.utils.RenderUtils.rotateToFace(poseStack, face, spin);
    }
}
