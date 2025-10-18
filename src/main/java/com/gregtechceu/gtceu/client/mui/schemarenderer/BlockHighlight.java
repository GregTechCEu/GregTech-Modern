package com.gregtechceu.gtceu.client.mui.schemarenderer;

import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Accessors(fluent = true, chain = true)
public class BlockHighlight {

    // rendnering magic

    protected static final float[][] vertices = new float[6][12];
    static {
        int[][] intVertices = {
                { 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0 },
                { 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0 },
                { 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0 },
                { 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0 }
        };
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                int v = intVertices[i][j];
                vertices[i][j] = v == 1 ? 1.005f : -0.005f;
            }
        }
    }

    @Getter
    @Setter
    private int color;

    @Getter
    @Setter
    private boolean allSides;

    @Getter
    @Setter
    private float thickness;

    public BlockHighlight(int color) {
        this(color, true);
    }

    public BlockHighlight(int color, float frameThickness) {
        this(color, true, frameThickness);
    }

    public BlockHighlight(int color, boolean allSides) {
        this(color, allSides, 0.0f);
    }

    public BlockHighlight(int color, boolean allSides, float frameThickness) {
        this.color = color;
        this.allSides = allSides;
        this.thickness = frameThickness;
    }

    public final void renderHighlight(PoseStack pose, @Nullable BlockHitResult result, Vector3f camera) {
        if (result != null && result.getType() == HitResult.Type.BLOCK) {
            renderHighlight(pose, result.getBlockPos(), result.getDirection(), camera);
        }
    }

    public void renderHighlight(PoseStack poseStack, BlockPos pos, Direction direction, Vector3f camera) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        Color.setGlColor(this.color);
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        float distance = camera.distance(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        doRender(poseStack, direction, distance);
        poseStack.popPose();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    protected void doRender(PoseStack poseStack, @Nullable Direction direction, float distance) {
        if (this.allSides) direction = null;
        Matrix4f pose = poseStack.last().pose();
        if (this.thickness >= 0) {
            // scale frame thickness with distance to camera
            float offset = (float) (this.thickness * (1 + Math.max(0, Math.sqrt(distance) - 3) / 5.0f));
            renderFrame(pose, direction, offset);
        } else {
            renderSolid(pose, direction);
        }
    }

    public static void renderSolid(Matrix4f pose, @Nullable Direction direction) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        if (direction == null) {
            for (Direction dir : GTUtil.DIRECTIONS) {
                buildFace(pose, builder, dir);
            }
        } else {
            buildFace(pose, builder, direction);
        }
    }

    protected static void renderFrame(Matrix4f pose, @Nullable Direction side, float offset) {
        if (side == null) {
            for (Direction dir : GTUtil.DIRECTIONS) {
                buildFrameFace(pose, dir, offset);
            }
        } else {
            buildFrameFace(pose, side, offset);
        }
    }

    protected static void buildFrameFace(Matrix4f pose, @NotNull Direction side, float offset) {
        float[] vert = vertices[side.get3DDataValue()];

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        buildVertex(pose, builder, vert, 9);
        buildInnerVertex(pose, builder, vert, 9, side, offset);
        buildVertex(pose, builder, vert, 6);
        buildInnerVertex(pose, builder, vert, 6, side, offset);
        buildVertex(pose, builder, vert, 3);
        buildInnerVertex(pose, builder, vert, 3, side, offset);
        buildVertex(pose, builder, vert, 0);
        buildInnerVertex(pose, builder, vert, 0, side, offset);
        buildVertex(pose, builder, vert, 9);
        buildInnerVertex(pose, builder, vert, 9, side, offset);

        BufferUploader.drawWithShader(builder.end());
    }

    protected static void buildVertex(Matrix4f pose, BufferBuilder builder, float[] vertices, int vertexIndex) {
        float x = vertices[vertexIndex];
        float y = vertices[vertexIndex + 1];
        float z = vertices[vertexIndex + 2];
        builder.vertex(pose, x, y, z).endVertex();
    }

    private static void buildInnerVertex(Matrix4f pose, BufferBuilder builder, float[] vertices, int vertexIndex,
                                         @NotNull Direction side, float offset) {
        float x = vertices[vertexIndex];
        float y = vertices[vertexIndex + 1];
        float z = vertices[vertexIndex + 2];
        if (side.getAxis() != Direction.Axis.X) {
            if (x >= 1) x -= offset;
            else x += offset;
        }
        if (side.getAxis() != Direction.Axis.Y) {
            if (y >= 1) y -= offset;
            else y += offset;
        }
        if (side.getAxis() != Direction.Axis.Z) {
            if (z >= 1) z -= offset;
            else z += offset;
        }
        builder.vertex(pose, x, y, z).endVertex();
    }

    protected static void buildFace(Matrix4f pose, BufferBuilder builder, @NotNull Direction side) {
        float[] vert = vertices[side.ordinal()];
        buildVertex(pose, builder, vert, 0);
        buildVertex(pose, builder, vert, 3);
        buildVertex(pose, builder, vert, 6);
        buildVertex(pose, builder, vert, 9);
    }
}
