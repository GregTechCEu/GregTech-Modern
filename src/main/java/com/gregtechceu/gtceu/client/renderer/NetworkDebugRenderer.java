package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class NetworkDebugRenderer {

    private static final float CUBE_RADIUS = 0.125F;
    private static final float LINE_WIDTH = 5.0F;
    private static List<NetworkDebugData> networks = List.of();

    private NetworkDebugRenderer() {
    }

    public static void setNetworks(List<NetworkDebugData> networks) {
        NetworkDebugRenderer.networks = List.copyOf(networks);
    }

    public static void clear() {
        networks = List.of();
    }

    public static void render(PoseStack poseStack, Camera camera) {
        if (networks.isEmpty() || Minecraft.getInstance().level == null) return;

        poseStack.pushPose();
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder cubeBuffer = new BufferBuilder(256);
        cubeBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (NetworkDebugData network : networks) {
            int color = network.type().color;
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            for (BlockPos node : network.nodes()) {
                drawCube(matrix, cubeBuffer, node.getX() + 0.5F, node.getY() + 0.5F, node.getZ() + 0.5F,
                        CUBE_RADIUS, red, green, blue);
            }
        }
        BufferUploader.drawWithShader(cubeBuffer.end());

        RenderSystem.lineWidth(LINE_WIDTH);
        BufferBuilder buffer = new BufferBuilder(256);
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (NetworkDebugData network : networks) {
            int color = network.type().color;
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            for (NetworkDebugData.Edge edge : network.edges()) {
                drawLine(matrix, buffer, edge.first().getX() + 0.5F, edge.first().getY() + 0.5F,
                        edge.first().getZ() + 0.5F, edge.second().getX() + 0.5F, edge.second().getY() + 0.5F,
                        edge.second().getZ() + 0.5F, red, green, blue);
            }
        }
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    private static void drawCube(Matrix4f matrix, BufferBuilder buffer, float x, float y, float z, float radius,
                                 float red, float green, float blue) {
        float minX = x - radius;
        float minY = y - radius;
        float minZ = z - radius;
        float maxX = x + radius;
        float maxY = y + radius;
        float maxZ = z + radius;

        drawQuad(matrix, buffer, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, red,
                green, blue);
        drawQuad(matrix, buffer, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, red,
                green, blue);
        drawQuad(matrix, buffer, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, red,
                green, blue);
        drawQuad(matrix, buffer, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, red,
                green, blue);
        drawQuad(matrix, buffer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, red,
                green, blue);
        drawQuad(matrix, buffer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, red,
                green, blue);
    }

    private static void drawQuad(Matrix4f matrix, BufferBuilder buffer, float x1, float y1, float z1, float x2,
                                 float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
                                 float red, float green, float blue) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, 1.0F).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, 1.0F).endVertex();
        buffer.vertex(matrix, x3, y3, z3).color(red, green, blue, 1.0F).endVertex();
        buffer.vertex(matrix, x4, y4, z4).color(red, green, blue, 1.0F).endVertex();
    }

    private static void drawLine(Matrix4f matrix, BufferBuilder buffer, float x1, float y1, float z1, float x2,
                                 float y2, float z2, float red, float green, float blue) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, 1.0F).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, 1.0F).endVertex();
    }
}
