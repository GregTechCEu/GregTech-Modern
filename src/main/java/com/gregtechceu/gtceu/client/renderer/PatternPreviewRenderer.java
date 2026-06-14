package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.drawable.schema.ISchema;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class PatternPreviewRenderer {

    public static final PatternPreviewRenderer INSTANCE = new PatternPreviewRenderer();

    private BlockPos controllerPos = BlockPos.ZERO;
    public @Nullable BaseSchemaRenderer schemaRenderer;
    private long timeoutMillis = 0;
    private long startTime = 0;

    public void draw(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera,
                     float partialTick) {
        if (!camera.isInitialized()) return;
        if (System.currentTimeMillis() - this.startTime >= this.timeoutMillis) return;
        if (this.schemaRenderer == null) return;

        Vec3 camPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        poseStack.translate(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        this.schemaRenderer.renderWorld(bufferSource, partialTick);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();

        poseStack.popPose();
    }

    public void setPreview(BlockPos controllerPos, ISchema schema, long timeoutMillis) {
        this.controllerPos = controllerPos;
        this.schemaRenderer = new BaseSchemaRenderer(schema);
        this.startTime = System.currentTimeMillis();
        this.timeoutMillis = timeoutMillis;
    }
}
