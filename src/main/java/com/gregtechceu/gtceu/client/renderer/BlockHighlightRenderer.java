package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;

import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote BlockHighlightRenderer
 */
@OnlyIn(Dist.CLIENT)
public class BlockHighlightRenderer {

    public static void renderBlockHighlight(PoseStack poseStack, Camera camera, BlockHitResult target,
                                            MultiBufferSource multiBufferSource, float partialTick) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level != null && player != null) {
            ItemStack held = player.getMainHandItem();
            BlockPos blockPos = target.getBlockPos();

            Set<GTToolType> toolType = ToolHelper.getToolTypes(held);
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            // draw tool grid highlight
            if ((!toolType.isEmpty()) || (held.isEmpty() && player.isShiftKeyDown())) {
                IToolGridHighlight gridHighlight = null;
                if (blockEntity instanceof IToolGridHighlight highLight) {
                    gridHighlight = highLight;
                } else if (level.getBlockState(blockPos).getBlock() instanceof IToolGridHighlight highLight) {
                    gridHighlight = highLight;
                } else if (toolType.contains(GTToolType.WRENCH)) {
                    var behavior = CustomBlockRotations.getCustomRotation(level.getBlockState(blockPos).getBlock());
                    if (behavior != null && behavior.showGrid()) {
                        gridHighlight = new IToolGridHighlight() {

                            @Override
                            public ResourceTexture sideTips(Player player, BlockPos pos, BlockState state,
                                                            Set<GTToolType> toolTypes, Direction side) {
                                return behavior.showSideTip(state, side) ? GuiTextures.TOOL_FRONT_FACING_ROTATION :
                                        null;
                            }
                        };
                    }
                }
                if (gridHighlight == null) {
                    return;
                }
                var state = level.getBlockState(blockPos);
                Vec3 pos = camera.getPosition();
                poseStack.pushPose();
                poseStack.translate(-pos.x, -pos.y, -pos.z);
                if (gridHighlight.shouldRenderGrid(player, blockPos, state, held, toolType)) {
                    var buffer = multiBufferSource.getBuffer(RenderType.lines());
                    RenderSystem.lineWidth(3);
                    final IToolGridHighlight finalGridHighlight = gridHighlight;
                    drawGridOverlays(poseStack, buffer, target,
                            side -> finalGridHighlight.sideTips(player, blockPos, state, toolType, side));
                } else {
                    var facing = target.getDirection();
                    var texture = gridHighlight.sideTips(player, blockPos, state, toolType, facing);
                    if (texture != null) {
                        RenderSystem.disableDepthTest();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        poseStack.translate(facing.getStepX() * 0.01, facing.getStepY() * 0.01,
                                facing.getStepZ() * 0.01);
                        RenderUtils.moveToFace(poseStack, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facing);
                        if (facing.getAxis() == Direction.Axis.Y) {
                            RenderUtils.rotateToFace(poseStack, facing, Direction.SOUTH);
                        } else {
                            RenderUtils.rotateToFace(poseStack, facing, null);
                        }
                        poseStack.scale(1f / 16, 1f / 16, 0);
                        poseStack.translate(-8, -8, 0);
                        texture.copy()
                                .draw(GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack,
                                        MultiBufferSource.immediate(Tesselator.getInstance().getBuilder())), 0, 0, 4, 4,
                                        8, 8);
                        RenderSystem.disableBlend();
                        RenderSystem.enableDepthTest();
                    }
                }
                poseStack.popPose();
                return;
            }

            // draw cover grid highlight
            ICoverable coverable = GTCapabilityHelper.getCoverable(level, blockPos, target.getDirection());
            if (coverable != null && CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover,
                    coverDef -> ICoverable.canPlaceCover(coverDef, coverable))) {
                Vec3 pos = camera.getPosition();
                poseStack.pushPose();
                poseStack.translate(-pos.x, -pos.y, -pos.z);
                var buffer = multiBufferSource.getBuffer(RenderType.lines());
                RenderSystem.lineWidth(3);

                drawGridOverlays(poseStack, buffer, target,
                        side -> coverable.hasCover(side) ? null : GuiTextures.TOOL_ATTACH_COVER);

                poseStack.popPose();
            }

            // draw pipe connection grid highlight
            var pipeType = held.getItem() instanceof PipeBlockItem pipeBlockItem ? pipeBlockItem.getBlock().pipeType :
                    null;
            if (pipeType instanceof IPipeType<?> type && blockEntity instanceof PipeBlockEntity<?, ?> pipeBlockEntity &&
                    pipeBlockEntity.getPipeType().type().equals(type.type())) {
                Vec3 pos = camera.getPosition();
                poseStack.pushPose();
                poseStack.translate(-pos.x, -pos.y, -pos.z);
                var buffer = multiBufferSource.getBuffer(RenderType.lines());
                RenderSystem.lineWidth(3);

                drawGridOverlays(poseStack, buffer, target, side -> level.isEmptyBlock(blockPos.relative(side)) ?
                        pipeBlockEntity.getPipeTexture(true) : null);

                poseStack.popPose();
            }
        }
    }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private static void drawGridOverlays(PoseStack poseStack, VertexConsumer buffer, BlockHitResult blockHitResult,
                                         Function<Direction, ResourceTexture> test) {
        rColour = gColour = 0.2F + (float) Math.sin((float) (System.currentTimeMillis() % (Mth.PI * 800)) / 800) / 2;
        bColour = 1f;
        var blockPos = blockHitResult.getBlockPos();
        var facing = blockHitResult.getDirection();
        var box = new AABB(blockPos);
        var attachSide = ICoverable.traceCoverSide(blockHitResult);
        var topRight = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ);
        var bottomRight = new Vector3f((float) box.maxX, (float) box.minY, (float) box.maxZ);
        var bottomLeft = new Vector3f((float) box.minX, (float) box.minY, (float) box.maxZ);
        var topLeft = new Vector3f((float) box.minX, (float) box.maxY, (float) box.maxZ);
        var shift = new Vector3f(0.25f, 0, 0);
        var shiftVert = new Vector3f(0, 0.25f, 0);

        var cubeCenter = box.getCenter().toVector3f();

        topRight.sub(cubeCenter);
        bottomRight.sub(cubeCenter);
        bottomLeft.sub(cubeCenter);
        topLeft.sub(cubeCenter);

        ResourceTexture leftBlocked;
        ResourceTexture topBlocked;
        ResourceTexture rightBlocked;
        ResourceTexture bottomBlocked;
        ResourceTexture frontBlocked = test.apply(facing);
        ResourceTexture backBlocked = test.apply(facing.getOpposite());
        boolean hoverLeft, hoverTop, hoverRight, hoverBottom, hoverFront, hoverBack;
        hoverFront = attachSide == facing;
        hoverBack = attachSide == facing.getOpposite();
        final Vector3f down = new Vector3f(0, -1, 0);

        switch (facing) {
            case WEST -> {
                topRight.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, down));
                bottomRight.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, down));
                bottomLeft.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, down));
                topLeft.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, down));
                shift.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, down));
                shiftVert.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, down));

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
                topRight.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, down));
                bottomRight.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, down));
                bottomLeft.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, down));
                topLeft.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, down));
                shift.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, down));
                shiftVert.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, down));

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
                topRight.rotate(new Quaternionf().rotateAxis(Mth.PI, down));
                bottomRight.rotate(new Quaternionf().rotateAxis(Mth.PI, down));
                bottomLeft.rotate(new Quaternionf().rotateAxis(Mth.PI, down));
                topLeft.rotate(new Quaternionf().rotateAxis(Mth.PI, down));
                shift.rotate(new Quaternionf().rotateAxis(Mth.PI, down));
                shiftVert.rotate(new Quaternionf().rotateAxis(Mth.PI, down));

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
                Vector3f side = new Vector3f(1, 0, 0);
                topRight.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, side));
                bottomRight.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, side));
                bottomLeft.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, side));
                topLeft.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, side));
                shift.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, side));
                shiftVert.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, side));

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
                Vector3f side = new Vector3f(1, 0, 0);
                topRight.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, side));
                bottomRight.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, side));
                bottomLeft.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, side));
                topLeft.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, side));
                shift.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, side));
                shiftVert.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, side));

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
        drawLine(mat, buffer, new Vector3f(topRight).add(new Vector3f(shift).mul(-1)),
                new Vector3f(bottomRight).add(new Vector3f(shift).mul(-1)));

        drawLine(mat, buffer, new Vector3f(bottomLeft).add(shift), new Vector3f(topLeft).add(shift));

        // straight side to side lines
        drawLine(mat, buffer, new Vector3f(topLeft).add(new Vector3f(shiftVert).mul(-1)),
                new Vector3f(topRight).add(new Vector3f(shiftVert).mul(-1)));

        drawLine(mat, buffer, new Vector3f(bottomLeft).add(shiftVert),
                new Vector3f(bottomRight).add(shiftVert));

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

        var graphics = GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack,
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()));
        if (leftBlocked != null) {
            leftBlocked.copy().scale(0.9f).setColor(hoverLeft ? -1 : 0x44ffffff).draw(graphics, 0, 0, 0, 6, 4, 4);
        }
        if (topBlocked != null) {
            topBlocked.copy().scale(0.9f).setColor(hoverTop ? -1 : 0x44ffffff).draw(graphics, 0, 0, 6, 0, 4, 4);
        }
        if (rightBlocked != null) {
            rightBlocked.copy().scale(0.9f).setColor(hoverRight ? -1 : 0x44ffffff).draw(graphics, 0, 0, 12, 6, 4, 4);
        }
        if (bottomBlocked != null) {
            bottomBlocked.copy().scale(0.9f).setColor(hoverBottom ? -1 : 0x44ffffff).draw(graphics, 0, 0, 6, 12, 4, 4);
        }
        if (frontBlocked != null) {
            frontBlocked.copy().scale(0.9f).setColor(hoverFront ? -1 : 0x44ffffff).draw(graphics, 0, 0, 6, 6, 4, 4);
        }
        if (backBlocked != null) {
            backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(graphics, 0, 0, 0, 0, 4, 4);
            backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(graphics, 0, 0, 12, 0, 4, 4);
            backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(graphics, 0, 0, 0, 12, 4, 4);
            backBlocked.copy().scale(0.9f).setColor(hoverBack ? -1 : 0x44ffffff).draw(graphics, 0, 0, 12, 12, 4, 4);
        }
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    private static void drawLine(Matrix4f mat, VertexConsumer buffer, Vector3f from, Vector3f to) {
        var normal = new Vector3f(from).sub(to);

        buffer.vertex(mat, from.x, from.y, from.z).color(rColour, gColour, bColour, 1f)
                .normal(normal.x, normal.y, normal.z).endVertex();
        buffer.vertex(mat, to.x, to.y, to.z).color(rColour, gColour, bColour, 1f).normal(normal.x, normal.y, normal.z)
                .endVertex();
    }
}
