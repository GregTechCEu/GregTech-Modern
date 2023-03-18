package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.lowdragmc.lowdraglib.utils.Vector3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;


/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote BlockHighLightRenderer
 */
@Environment(EnvType.CLIENT)
public class BlockHighLightRenderer {

    @SuppressWarnings("ConstantValue")
    public static void renderBlockHighLight(PoseStack poseStack, Camera camera, BlockHitResult target, MultiBufferSource multiBufferSource, float partialTick) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level != null && player != null) {
            var held = player.getMainHandItem();
            var blockPos = target.getBlockPos();

            var toolType = held.getItem() instanceof GTToolItem toolItem ? toolItem.getToolType() : null;
            var blockEntity = level.getBlockEntity(blockPos);

            if (toolType != null && blockEntity instanceof IToolGridHighLight gridHighLight && gridHighLight.shouldRenderGrid(player, held, toolType)) {
                Vec3 pos = camera.getPosition();
                poseStack.pushPose();
                poseStack.translate(-pos.x, -pos.y, -pos.z);
                var buffer = multiBufferSource.getBuffer(RenderType.lines());
                RenderSystem.lineWidth(3);

                drawGridOverlays(poseStack, buffer, target.getDirection(), new AABB(blockPos), side -> gridHighLight.isSideUsed(player, toolType, side));

                poseStack.popPose();
                return;
            }

            ICoverable coverable = GTCapabilityHelper.getCoverable(level, blockPos, target.getDirection());
            if (coverable != null && CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover, coverDef -> ICoverable.canPlaceCover(coverDef, coverable))) {
                Vec3 pos = camera.getPosition();
                poseStack.pushPose();
                poseStack.translate(-pos.x, -pos.y, -pos.z);
                var buffer = multiBufferSource.getBuffer(RenderType.lines());
                RenderSystem.lineWidth(3);

                drawGridOverlays(poseStack, buffer, target.getDirection(), new AABB(blockPos), coverable::hasCover);

                poseStack.popPose();
            }
        }
    }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private static void drawGridOverlays(PoseStack poseStack, VertexConsumer buffer, Direction facing, AABB box, Predicate<Direction> test) {
        rColour = gColour = 0.2F + (float) Math.sin((float) (System.currentTimeMillis() % (Math.PI * 800)) / 800) / 2;
        bColour = 1f;

        Vector3 topRight = new Vector3(box.maxX, box.maxY, box.maxZ);
        Vector3 bottomRight = new Vector3(box.maxX, box.minY, box.maxZ);
        Vector3 bottomLeft = new Vector3(box.minX, box.minY, box.maxZ);
        Vector3 topLeft = new Vector3(box.minX, box.maxY, box.maxZ);
        Vector3 shift = new Vector3(0.25, 0, 0);
        Vector3 shiftVert = new Vector3(0, 0.25, 0);

        Vector3 cubeCenter = new Vector3(box.getCenter());

        topRight.subtract(cubeCenter);
        bottomRight.subtract(cubeCenter);
        bottomLeft.subtract(cubeCenter);
        topLeft.subtract(cubeCenter);

        boolean leftBlocked;
        boolean topBlocked;
        boolean rightBlocked;
        boolean bottomBlocked;
        boolean frontBlocked = test.test(facing);
        boolean backBlocked = test.test(facing.getOpposite());
        final Vector3 down = new Vector3(0.0, -1.0, 0.0);

        switch (facing) {
            case WEST -> {
                topRight.rotate(Math.PI / 2, down);
                bottomRight.rotate(Math.PI / 2, down);
                bottomLeft.rotate(Math.PI / 2, down);
                topLeft.rotate(Math.PI / 2, down);
                shift.rotate(Math.PI / 2, down);
                shiftVert.rotate(Math.PI / 2, down);

                leftBlocked = test.test(Direction.NORTH);
                topBlocked = test.test(Direction.UP);
                rightBlocked = test.test(Direction.SOUTH);
                bottomBlocked = test.test(Direction.DOWN);
            }
            case EAST -> {
                topRight.rotate(-Math.PI / 2, down);
                bottomRight.rotate(-Math.PI / 2, down);
                bottomLeft.rotate(-Math.PI / 2, down);
                topLeft.rotate(-Math.PI / 2, down);
                shift.rotate(-Math.PI / 2, down);
                shiftVert.rotate(-Math.PI / 2, down);

                leftBlocked = test.test(Direction.SOUTH);
                topBlocked = test.test(Direction.UP);
                rightBlocked = test.test(Direction.NORTH);
                bottomBlocked = test.test(Direction.DOWN);
            }
            case NORTH -> {
                topRight.rotate(Math.PI, down);
                bottomRight.rotate(Math.PI, down);
                bottomLeft.rotate(Math.PI, down);
                topLeft.rotate(Math.PI, down);
                shift.rotate(Math.PI, down);
                shiftVert.rotate(Math.PI, down);

                leftBlocked = test.test(Direction.EAST);
                topBlocked = test.test(Direction.UP);
                rightBlocked = test.test(Direction.WEST);
                bottomBlocked = test.test(Direction.DOWN);
            }
            case UP -> {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(-Math.PI / 2, side);
                bottomRight.rotate(-Math.PI / 2, side);
                bottomLeft.rotate(-Math.PI / 2, side);
                topLeft.rotate(-Math.PI / 2, side);
                shift.rotate(-Math.PI / 2, side);
                shiftVert.rotate(-Math.PI / 2, side);

                leftBlocked = test.test(Direction.WEST);
                topBlocked = test.test(Direction.NORTH);
                rightBlocked = test.test(Direction.EAST);
                bottomBlocked = test.test(Direction.SOUTH);
            }
            case DOWN -> {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(Math.PI / 2, side);
                bottomRight.rotate(Math.PI / 2, side);
                bottomLeft.rotate(Math.PI / 2, side);
                topLeft.rotate(Math.PI / 2, side);
                shift.rotate(Math.PI / 2, side);
                shiftVert.rotate(Math.PI / 2, side);

                leftBlocked = test.test(Direction.WEST);
                topBlocked = test.test(Direction.SOUTH);
                rightBlocked = test.test(Direction.EAST);
                bottomBlocked = test.test(Direction.NORTH);
            }
            default -> {
                leftBlocked = test.test(Direction.WEST);
                topBlocked = test.test(Direction.UP);
                rightBlocked = test.test(Direction.EAST);
                bottomBlocked = test.test(Direction.DOWN);
            }
        }

        topRight.add(cubeCenter);
        bottomRight.add(cubeCenter);
        bottomLeft.add(cubeCenter);
        topLeft.add(cubeCenter);

        var mat = poseStack.last().pose();
        // straight top bottom lines
        drawLine(mat, buffer, topRight.copy().add(shift.copy().multiply(-1)),
                bottomRight.copy().add(shift.copy().multiply(-1)));

        drawLine(mat, buffer, bottomLeft.copy().add(shift), topLeft.copy().add(shift));

        // straight side to side lines
        drawLine(mat, buffer, topLeft.copy().add(shiftVert.copy().multiply(-1)),
                topRight.copy().add(shiftVert.copy().multiply(-1)));

        drawLine(mat, buffer, bottomLeft.copy().add(shiftVert),
                bottomRight.copy().add(shiftVert));

        if (leftBlocked) {
            drawLine(mat, buffer, topLeft.copy().add(shiftVert.copy().multiply(-1))
                    , bottomLeft.copy().add(shiftVert.copy()).add(shift));

            drawLine(mat, buffer, topLeft.copy().add(shiftVert.copy().multiply(-1)).add(shift)
                    , bottomLeft.copy().add(shiftVert));
        }
        if (topBlocked) {
            drawLine(mat, buffer, topLeft.copy().add(shift)
                    , topRight.copy().add(shift.copy().multiply(-1)).add(shiftVert.copy().multiply(-1)));

            drawLine(mat, buffer, topLeft.copy().add(shift).add(shiftVert.copy().multiply(-1))
                    , topRight.copy().add(shift.copy().multiply(-1)));
        }
        if (rightBlocked) {
            drawLine(mat, buffer, topRight.copy().add(shiftVert.copy().multiply(-1))
                    , bottomRight.copy().add(shiftVert.copy()).add(shift.copy().multiply(-1)));

            drawLine(mat, buffer, topRight.copy().add(shiftVert.copy().multiply(-1)).add(shift.copy().multiply(-1))
                    , bottomRight.copy().add(shiftVert));
        }
        if (bottomBlocked) {
            drawLine(mat, buffer, bottomLeft.copy().add(shift)
                    , bottomRight.copy().add(shift.copy().multiply(-1)).add(shiftVert));

            drawLine(mat, buffer, bottomLeft.copy().add(shift).add(shiftVert)
                    , bottomRight.copy().add(shift.copy().multiply(-1)));
        }
        if (frontBlocked) {
            drawLine(mat, buffer, topLeft.copy().add(shift).add(shiftVert.copy().multiply(-1))
                    , bottomRight.copy().add(shift.copy().multiply(-1)).add(shiftVert));

            drawLine(mat, buffer, topRight.copy().add(shift.copy().multiply(-1)).add(shiftVert.copy().multiply(-1))
                    , bottomLeft.copy().add(shift).add(shiftVert));
        }
        if (backBlocked) {
            Vector3 localXShift = new Vector3(0, 0, 0); // Set up translations for the current X.
            for (int i = 0; i < 2; i++) {
                Vector3 localXShiftVert = new Vector3(0, 0, 0);
                for (int j = 0; j < 2; j++) {
                    drawLine(mat, buffer, topLeft.copy().add(localXShift).add(localXShiftVert), 
                            topLeft.copy().add(localXShift).add(localXShiftVert).add(shift).subtract(shiftVert));

                    drawLine(mat, buffer, topLeft.copy().add(localXShift).add(localXShiftVert).add(shift), 
                            topLeft.copy().add(localXShift).add(localXShiftVert).subtract(shiftVert));

                    localXShiftVert.add(bottomLeft.copy().subtract(topLeft).add(shiftVert)); // Move by the vector from the top to the bottom, minus the shift from the edge.
                }
                localXShift.add(topRight.copy().subtract(topLeft).subtract(shift)); // Move by the vector from the left to the right, minus the shift from the edge.
            }
        }

    }

    private static void drawLine(Matrix4f mat, VertexConsumer buffer, Vector3 from, Vector3 to) {
        var normal = from.copy().subtract(to);
        
        buffer.vertex(mat, (float) from.x, (float) from.y, (float) from.z).color(rColour, gColour, bColour, 1f).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        buffer.vertex(mat, (float) to.x, (float) to.y, (float) to.z).color(rColour, gColour, bColour, 1f).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

}
