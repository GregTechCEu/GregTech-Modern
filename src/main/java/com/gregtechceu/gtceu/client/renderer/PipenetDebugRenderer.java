package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;

import static com.gregtechceu.gtceu.client.util.RenderBufferHelper.*;
import static com.gregtechceu.gtceu.utils.GTMath.*;

@OnlyIn(Dist.CLIENT)
public class PipenetDebugRenderer extends RenderType {

    public static final PipenetDebugRenderer INSTANCE = new PipenetDebugRenderer();
    private static final int WHITE = FastColor.ARGB32.color(255, 255, 255, 255);
    private static VertexBuffer VBO = null;
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

        this.QUADS_RENDER = create("gt_pipe_debug_quads", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256, false, false, CompositeState.builder()
                .setTransparencyState(STO)
                .setDepthTestState(NO_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setShaderState(POSITION_COLOR_SHADER)
                .setLightmapState(NO_LIGHTMAP)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setTextureState(NO_TEXTURE)
                .createCompositeState(true));
    }

    public static void hook(RenderLevelStageEvent event) {
        if (event.getStage() == Stage.AFTER_PARTICLES) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && player.getMainHandItem().getItem() == GTItems.PIPENET_DEBUG_VIEWER.get()) {
                INSTANCE.tick(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), event.getProjectionMatrix(), event.getCamera());
            }
        }

    }

    public void createVBO() {
        if (VBO != null) {
            VBO.close();
        }

        BufferBuilder buf = new BufferBuilder(this.QUADS_RENDER.bufferSize() * 8);
        buf.begin(this.QUADS_RENDER.mode(), this.QUADS_RENDER.format());
        PoseStack stack = new PoseStack();

        drawQuads(stack, buf);

        BufferBuilder.RenderedBuffer rendered = buf.end();
        VBO = new VertexBuffer(Usage.DYNAMIC);
        VBO.bind();
        VBO.upload(rendered);
        VertexBuffer.unbind();
    }

    public void tick(PoseStack stack, MultiBufferSource.BufferSource multiBuf, Matrix4f pro, Camera camera) {
        if (GameRenderer.getPositionColorShader() != null && camera.isInitialized()) {
            Vec3 offset = camera.getPosition().reverse();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();

            /// IF UPDATE THEN NEW VBO
            this.createVBO();

            if (VBO != null) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.disableCull();
                stack.pushPose();
                stack.translate(offset.x, offset.y, offset.z);
                VBO.bind();
                VBO.drawWithShader(stack.last().pose(), pro, GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();
                stack.popPose();
                RenderSystem.enableCull();
            }

            RenderSystem.disableBlend();

            drawText(stack, multiBuf, pro, camera);

            multiBuf.endBatch();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }

    }


    private void drawQuads(PoseStack stack, BufferBuilder buf) {
        renderCube(buf, stack, BlockPos.ZERO, 2, WHITE);
        drawLine(buf, stack, BlockPos.ZERO, BlockPos.ZERO.below(10), 0.01, WHITE);
    }

    private void drawText(PoseStack stack, MultiBufferSource.BufferSource multiBuf, Matrix4f pro, Camera camera) {
        renderInWorldText(multiBuf, stack, camera, "rendering is so cool", WHITE, getCenter(BlockPos.ZERO, BlockPos.ZERO.atY(3)), camera.getPosition().reverse());
    }
}
