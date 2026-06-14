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
    private long timeoutMillis = 0;
    private long startTime = 0;

    public void tick(PoseStack pose, MultiBufferSource.BufferSource bufferSource, Camera camera) {
        if (!camera.isInitialized()) return;
        if (System.currentTimeMillis() - this.startTime >= this.timeoutMillis) return;
        if (this.blocks.isEmpty()) return;

        Vec3 camPos = camera.getPosition();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        pose.translate(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());

        // TODO instancing of some sort cause this kills fps :wilted_rose:
        for (var entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState realState = level.getBlockState(controllerPos.mutable().move(pos));
            BlockState drawnState = entry.getValue().getBlockState();
            if (drawnState.is(realState.getBlock())) continue;

            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            // make the block smaller
            pose.scale(0.8f, 0.8f, 0.8f);
            pose.translate(0.1f, 0.1f, 0.1f);
            RenderUtil.drawBlock(level, pos, drawnState, bufferSource, pose);
            pose.popPose();
        }

        pose.popPose();
        bufferSource.endBatch();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public void setPreview(BlockPos controllerPos, Map<BlockPos, BlockInfo> blocks, long timeoutMillis) {
        this.controllerPos = controllerPos;
        this.blocks = blocks;
        this.startTime = System.currentTimeMillis();
        this.timeoutMillis = timeoutMillis;
    }
}
