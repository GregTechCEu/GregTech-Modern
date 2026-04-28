package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

public final class MultiblockInWorldPreviewRenderer {

    private MultiblockInWorldPreviewRenderer() {}

    public static void cleanPreview() {}

    public static void removePreview(BlockPos pos) {}

    public static void showPreview(BlockPos pos, MultiblockControllerMachine controller,
                                   List<MultiblockShapeInfo> shapeInfos) {}

    public static void onClientTick() {}

    public static void renderInWorldPreview(PoseStack poseStack, Camera camera, float partialTicks) {}
}
