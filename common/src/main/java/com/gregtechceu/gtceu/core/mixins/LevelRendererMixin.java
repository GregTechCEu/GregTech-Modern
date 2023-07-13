package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Invoker("renderShape")
    public static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }

    @Inject(
            method = {"renderHitOutline"},
            at = {@At("HEAD")}
    )
    private void renderHitOutline(PoseStack poseStack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (minecraft.player == null) return;
        if (minecraft.level == null) return;

        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        if (!GTToolType.MINING_HAMMER.is(mainHandItem)) return;

        if (state.isAir() || !minecraft.level.isInWorldBounds(pos) || !mainHandItem.isCorrectToolForDrops(state)) return;

        List<BlockPos> blockPositions = ToolHelper.getAOEPositions(minecraft.player, mainHandItem, pos, 1);
        List<VoxelShape> outlineShapes = new ArrayList<>();

        for (BlockPos position : blockPositions) {
            System.out.println("Block Position: " + position);
            if (!ToolHelper.aoeCanBreak(mainHandItem, minecraft.level, pos, position)) continue;

            BlockPos diffPos = position.subtract(pos);
            BlockState offsetState = minecraft.level.getBlockState(position);

            outlineShapes.add(offsetState.getShape(minecraft.level, position).move(diffPos.getX(), diffPos.getY(), diffPos.getZ()));
        }

        System.out.println(outlineShapes);

        outlineShapes.forEach(shape -> {
            renderShape(
                    poseStack,
                    consumer,
                    shape,
                    (double) pos.getX() - camX,
                    (double) pos.getY() - camY,
                    (double) pos.getZ() - camZ,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.4F);
        });

    }
}
