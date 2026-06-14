package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class PatternPreviewRenderer {

    public static final PatternPreviewRenderer INSTANCE = new PatternPreviewRenderer();

    private BlockAndTintGetter level = Minecraft.getInstance().level;
    private BlockPos controllerPos = BlockPos.ZERO;
    public Map<BlockPos, BlockInfo> blocks = new HashMap<>();
    private long durationMillis = 0;
    private long startTime = 0;

    public void tick(PoseStack pose, MultiBufferSource.BufferSource bufferSource, Camera camera) {
        if (GameRenderer.getPositionColorShader() == null || !camera.isInitialized()) return;
        if (level == null) return;
        if (System.currentTimeMillis() > this.startTime + this.durationMillis) return;
        if (blocks.isEmpty()) return;

        Vec3 offset = camera.getPosition().reverse();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        pose.pushPose();
        pose.translate(offset.x + controllerPos.getX(), offset.y + controllerPos.getY(),
                offset.z + controllerPos.getZ());

        // TODO instancing of some sort cause this kills fps :wilted_rose:
        for (var entry : blocks.entrySet()) {
            pose.pushPose();
            pose.translate(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
            // to make the block smaller/non-full
            pose.scale(0.8f, 0.8f, 0.8f);
            pose.translate(0.1f, 0.1f, 0.1f);
            RenderUtil.drawBlock(level, BlockPos.ZERO, entry.getValue().getBlockState(), bufferSource, pose);
            pose.popPose();
        }

        pose.popPose();
        bufferSource.endBatch();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public void setPreview(BlockPos controllerPos, Map<BlockPos, BlockInfo> blocks, long durationMillis) {
        this.controllerPos = controllerPos;
        this.blocks = blocks;
        this.startTime = System.currentTimeMillis();
        this.durationMillis = durationMillis;
    }
}
