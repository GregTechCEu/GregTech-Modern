package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import org.joml.Matrix4f;

import static com.gregtechceu.gtceu.client.util.RenderBufferHelper.*;
import static com.gregtechceu.gtceu.utils.GTMath.*;

@OnlyIn(Dist.CLIENT)
public class PipenetDebugRenderer extends RenderType {

    public static final PipenetDebugRenderer INSTANCE = new PipenetDebugRenderer();
    private static final int WHITE = FastColor.ARGB32.color(255, 255, 255, 255);
    private final RenderType QUADS_RENDER;

    public PipenetDebugRenderer() {
        super("", DefaultVertexFormat.POSITION_COLOR_NORMAL, Mode.LINES, 0, false, false, () -> {}, () -> {});

        TransparencyStateShard STO = new TransparencyStateShard("sto", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        this.QUADS_RENDER = create("gt_pipe_debug_quads", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256, false,
                false, CompositeState.builder()
                        .setTransparencyState(STO)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setTextureState(NO_TEXTURE)
                        .createCompositeState(true));
    }

    public void tick(PoseStack stack, MultiBufferSource.BufferSource multiBuf, Matrix4f pro, Camera camera) {
        if (GameRenderer.getPositionColorShader() != null && camera.isInitialized()) {
            Vec3 offset = camera.getPosition().reverse();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableCull();

            stack.pushPose();
            stack.translate(offset.x, offset.y, offset.z);

            var buf = multiBuf.getBuffer(QUADS_RENDER);
            drawQuads(stack, buf);

            stack.popPose();
            RenderSystem.enableCull();

            RenderSystem.disableBlend();

            drawText(stack, multiBuf, pro, camera);

            multiBuf.endBatch();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    private void drawQuads(PoseStack stack, VertexConsumer buf) {

        renderAABBOutline(buf, stack, new AABB(new BlockPos(0, 10, 0)), 0.01, WHITE);

        renderCube(buf, stack, BlockPos.ZERO, 1, WHITE);
        renderLine(buf, stack, BlockPos.ZERO, BlockPos.ZERO.below(10), 0.01, WHITE);
    }

    private void drawText(PoseStack stack, MultiBufferSource.BufferSource multiBuf, Matrix4f pro, Camera camera) {
        renderInWorldText(multiBuf, stack, camera, "rendering is so cool", WHITE,
                getCenter(BlockPos.ZERO, BlockPos.ZERO.atY(3)));
    }
}
