package com.gregtechceu.gtceu.utils.fakelevel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class BlockHighlight {

    //rendnering magic

    protected static final float[][] vertices = new float[6][12];
    static {
                int[][] intVertices = {
                {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0},
                {1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0}
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

    public BlockHighlight(int color){
        this(color,true,0.0f);
    }

    public BlockHighlight(int color, float frameThickness) {
        this(color, true, frameThickness);
    }

    public BlockHighlight(int color, boolean allSides) {
        this(color, allSides, 0.0F);
    }

    public BlockHighlight(int color, boolean allSides, float frameThickness) {
        this.color = color;
        this.allSides = allSides;
        this.thickness = frameThickness;
    }

    public final void renderHighlight(BlockHitResult result, Vector3f camera, PoseStack pose ){
        if (result != null && result.getType()== HitResult.Type.BLOCK) {
            renderHighlight(result.getBlockPos(), result.getDirection(), camera, pose);
        }
    }

    public void renderHighlight(BlockPos pos, Direction direction, Vector3f camera,  PoseStack pose ){
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        pose.pushPose();
        pose.translate(pos.getX(), pos.getY(), pos.getZ());

        float distance = camera.distance(pos.getX() + 0.5f , pos.getY() + 0.5f , pos.getZ() + 0.5f);
        doRender(direction, distance, pose);
        pose.popPose();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    protected void doRender(Direction direction, float distance, PoseStack poseStack){

        if(this.allSides) direction = null;
        if(this.thickness >= 0) {

            float d = (float) (this.thickness * (1 + Math.max(0, Math.sqrt(distance) - 3) / 5));
            renderFrame(direction, distance);
        }else{
            renderSolid(direction, poseStack);
        }
    }

    public void renderSolid(Direction direction, PoseStack poseStack){

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        if (direction == null) {
            for (int i = 0; i < 6; i++) {
                buildFace(builder,direction);
            }
        } else {
            buildFace(builder, direction);
        }

    }

    protected static void renderFrame(@Nullable Direction side,Float d){
        if(side == null) {
            for (int i = 0 ; i < 6 ; i++) {
                buildFrameFace(side, d);
            }
        } else {
            buildFrameFace(side, d);
        }
    }

    protected static void buildFrameFace(Direction side, Float d){

        float[] vert = vertices[side.get3DDataValue()];

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        buildVertex(builder, vert, 9);
        buildInnerVertex(builder, vert, 9, side, d);
        buildVertex(builder, vert, 6);
        buildInnerVertex(builder, vert, 6, side, d);
        buildVertex(builder, vert, 3);
        buildInnerVertex(builder, vert, 3, side, d);
        buildVertex(builder, vert, 0);
        buildInnerVertex(builder, vert, 0, side, d);
        buildVertex(builder, vert, 9);
        buildInnerVertex(builder, vert, 9, side, d);

        BufferUploader.drawWithShader(builder.end());

    }

    protected static void buildVertex(BufferBuilder builder, float[] vertices, int i) {
        float x = vertices[i];
        float y = vertices[i + 1];
        float z = vertices[i + 2];
        builder.vertex(x, y, z).endVertex();
    }
    private static void buildInnerVertex(BufferBuilder builder, float[] vertices, int i, Direction side, float d) {
        float x = vertices[i];
        float y = vertices[i + 1];
        float z = vertices[i + 2];
        if (side.getAxis() != Direction.Axis.X) {
            if (x >= 1) x -= d;
            else x += d;
        }
        if (side.getAxis() != Direction.Axis.Y) {
            if (y >= 1) y -= d;
            else y += d;
        }
        if (side.getAxis() != Direction.Axis.Z) {
            if (z >= 1) z -= d;
            else z += d;
        }
        builder.vertex(x, y, z).endVertex();
    }

    protected static void buildFace(BufferBuilder builder, Direction facing) {

        float[] vert = vertices[facing.ordinal()];
        buildVertex(builder, vert, 0);
        buildVertex(builder, vert, 3);
        buildVertex(builder, vert, 6);
        buildVertex(builder, vert, 9);
    }

}
