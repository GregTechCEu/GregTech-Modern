package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.utils.Vector3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

import java.util.function.Function;


/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote BlockHighLightRenderer
 */
@Environment(EnvType.CLIENT)
public class BlockHighLightRenderer {

    public static void renderBlockHighLight(PoseStack poseStack, Camera camera, BlockHitResult target, MultiBufferSource multiBufferSource, float partialTick) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level != null && player != null) {
            var held = player.getMainHandItem();
            var blockPos = target.getBlockPos();

            var toolType = held.getItem() instanceof GTToolItem toolItem ? toolItem.getToolType() : null;
            var blockEntity = level.getBlockEntity(blockPos);

            if (toolType != null && blockEntity instanceof IToolGridHighLight gridHighLight) {
                Vec3 pos = camera.getPosition();
                poseStack.pushPose();
                poseStack.translate(-pos.x, -pos.y, -pos.z);
                if (gridHighLight.shouldRenderGrid(player, held, toolType)) {
                    var buffer = multiBufferSource.getBuffer(RenderType.lines());
                    RenderSystem.lineWidth(3);
                    drawGridOverlays(poseStack, buffer, target, side -> gridHighLight.sideTips(player, toolType, side));
                } else {
                    var facing = target.getDirection();
                    var texture = gridHighLight.sideTips(player, toolType, facing);
                    if (texture != null) {
                        RenderSystem.disableDepthTest();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        poseStack.translate(facing.getStepX() * 0.01, facing.getStepY() * 0.01, facing.getStepZ() * 0.01);
                        RenderUtils.moveToFace(poseStack, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facing);
                        if (facing.getAxis() == Direction.Axis.Y) {
                            RenderUtils.rotateToFace(poseStack, facing, Direction.SOUTH);
                        } else {
                            RenderUtils.rotateToFace(poseStack, facing, null);
                        }
                        poseStack.scale(1f / 16, 1f / 16, 0);
                        poseStack.translate(-8, -8, 0);
                        texture.copy().draw(poseStack, 0, 0, 4, 4, 8, 8);
                        RenderSystem.disableBlend();
                        RenderSystem.enableDepthTest();
                    }
                }
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

                drawGridOverlays(poseStack, buffer, target, side -> coverable.hasCover(side) ? null : GuiTextures.TOOL_ATTACH_COVER);

                poseStack.popPose();
            }

        }
    }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private static void drawGridOverlays(PoseStack poseStack, VertexConsumer buffer, BlockHitResult blockHitResult, Function<Direction, ResourceTexture> test) {
        rColour = gColour = 0.2F + (float) Math.sin((float) (System.currentTimeMillis() % (Math.PI * 800)) / 800) / 2;
        bColour = 1f;
        var blockPos = blockHitResult.getBlockPos();
        var facing = blockHitResult.getDirection();
        var box = new AABB(blockPos);
        var attachSide = ICoverable.traceCoverSide(blockHitResult);
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

        ResourceTexture leftBlocked;
        ResourceTexture topBlocked;
        ResourceTexture rightBlocked;
        ResourceTexture bottomBlocked;
        ResourceTexture frontBlocked = test.apply(facing);
        ResourceTexture backBlocked = test.apply(facing.getOpposite());
        boolean hoverLeft, hoverTop, hoverRight, hoverBottom, hoverFront, hoverBack;
        hoverFront = attachSide == facing;
        hoverBack = attachSide == facing.getOpposite();
        final Vector3 down = new Vector3(0.0, -1.0, 0.0);

        switch (facing) {
            case WEST -> {
                topRight.rotate(Math.PI / 2, down);
                bottomRight.rotate(Math.PI / 2, down);
                bottomLeft.rotate(Math.PI / 2, down);
                topLeft.rotate(Math.PI / 2, down);
                shift.rotate(Math.PI / 2, down);
                shiftVert.rotate(Math.PI / 2, down);

                leftBlocked = test.apply(Direction.NORTH);
                topBlocked = test.apply(Direction.UP);
                rightBlocked = test.apply(Direction.SOUTH);
                bottomBlocked = test.apply(Direction.DOWN);
                hoverLeft = attachSide == Direction.NORTH;
                hoverTop = attachSide == Direction.UP;
                hoverRight = attachSide == Direction.SOUTH;
                hoverBottom = attachSide == Direction.DOWN;
            }
            case EAST -> {
                topRight.rotate(-Math.PI / 2, down);
                bottomRight.rotate(-Math.PI / 2, down);
                bottomLeft.rotate(-Math.PI / 2, down);
                topLeft.rotate(-Math.PI / 2, down);
                shift.rotate(-Math.PI / 2, down);
                shiftVert.rotate(-Math.PI / 2, down);

                leftBlocked = test.apply(Direction.SOUTH);
                topBlocked = test.apply(Direction.UP);
                rightBlocked = test.apply(Direction.NORTH);
                bottomBlocked = test.apply(Direction.DOWN);
                hoverLeft = attachSide == Direction.SOUTH;
                hoverTop = attachSide == Direction.UP;
                hoverRight = attachSide == Direction.NORTH;
                hoverBottom = attachSide == Direction.DOWN;
            }
            case NORTH -> {
                topRight.rotate(Math.PI, down);
                bottomRight.rotate(Math.PI, down);
                bottomLeft.rotate(Math.PI, down);
                topLeft.rotate(Math.PI, down);
                shift.rotate(Math.PI, down);
                shiftVert.rotate(Math.PI, down);

                leftBlocked = test.apply(Direction.EAST);
                topBlocked = test.apply(Direction.UP);
                rightBlocked = test.apply(Direction.WEST);
                bottomBlocked = test.apply(Direction.DOWN);
                hoverLeft = attachSide == Direction.EAST;
                hoverTop = attachSide == Direction.UP;
                hoverRight = attachSide == Direction.WEST;
                hoverBottom = attachSide == Direction.DOWN;
            }
            case UP -> {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(-Math.PI / 2, side);
                bottomRight.rotate(-Math.PI / 2, side);
                bottomLeft.rotate(-Math.PI / 2, side);
                topLeft.rotate(-Math.PI / 2, side);
                shift.rotate(-Math.PI / 2, side);
                shiftVert.rotate(-Math.PI / 2, side);

                leftBlocked = test.apply(Direction.EAST);
                topBlocked = test.apply(Direction.SOUTH);
                rightBlocked = test.apply(Direction.WEST);
                bottomBlocked = test.apply(Direction.NORTH);
                hoverLeft = attachSide == Direction.EAST;
                hoverTop = attachSide == Direction.SOUTH;
                hoverRight = attachSide == Direction.WEST;
                hoverBottom = attachSide == Direction.NORTH;
            }
            case DOWN -> {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(Math.PI / 2, side);
                bottomRight.rotate(Math.PI / 2, side);
                bottomLeft.rotate(Math.PI / 2, side);
                topLeft.rotate(Math.PI / 2, side);
                shift.rotate(Math.PI / 2, side);
                shiftVert.rotate(Math.PI / 2, side);

                leftBlocked = test.apply(Direction.WEST);
                topBlocked = test.apply(Direction.SOUTH);
                rightBlocked = test.apply(Direction.EAST);
                bottomBlocked = test.apply(Direction.NORTH);
                hoverLeft = attachSide == Direction.WEST;
                hoverTop = attachSide == Direction.SOUTH;
                hoverRight = attachSide == Direction.EAST;
                hoverBottom = attachSide == Direction.NORTH;
            }
            default -> {
                leftBlocked = test.apply(Direction.WEST);
                topBlocked = test.apply(Direction.UP);
                rightBlocked = test.apply(Direction.EAST);
                bottomBlocked = test.apply(Direction.DOWN);
                hoverLeft = attachSide == Direction.WEST;
                hoverTop = attachSide == Direction.UP;
                hoverRight = attachSide == Direction.EAST;
                hoverBottom = attachSide == Direction.DOWN;
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

        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.translate(facing.getStepX() * 0.01, facing.getStepY() * 0.01, facing.getStepZ() * 0.01);
        RenderUtils.moveToFace(poseStack, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facing);
        if (facing.getAxis() == Direction.Axis.Y) {
            RenderUtils.rotateToFace(poseStack, facing, Direction.SOUTH);
        } else {
            RenderUtils.rotateToFace(poseStack, facing, null);
        }
        poseStack.scale(1f / 16, 1f / 16, 0);
        poseStack.translate(-8, -8, 0);

        if (leftBlocked != null) {
             leftBlocked.copy().scale(0.9f).setColor(hoverLeft ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 0, 6, 4, 4);
        }
        if (topBlocked != null) {
             topBlocked.copy().scale(0.9f).setColor(hoverTop ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 6, 0, 4, 4);
        }
        if (rightBlocked != null) {
             rightBlocked.copy().scale(0.9f).setColor(hoverRight ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 12, 6, 4, 4);
        }
        if (bottomBlocked != null) {
             bottomBlocked.copy().scale(0.9f).setColor(hoverBottom ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 6, 12, 4, 4);
        }
        if (frontBlocked != null) {
             frontBlocked.copy().scale(0.9f).setColor(hoverFront ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 6, 6, 4, 4);
        }
        if (backBlocked != null) {
             backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 0, 0, 4, 4);
             backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 12, 0, 4, 4);
             backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 0, 12, 4, 4);
             backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(poseStack, 0, 0, 12, 12, 4, 4);
        }
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    private static void drawLine(Matrix4f mat, VertexConsumer buffer, Vector3 from, Vector3 to) {
        var normal = from.copy().subtract(to);
        
        buffer.vertex(mat, (float) from.x, (float) from.y, (float) from.z).color(rColour, gColour, bColour, 1f).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        buffer.vertex(mat, (float) to.x, (float) to.y, (float) to.z).color(rColour, gColour, bColour, 1f).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

}
