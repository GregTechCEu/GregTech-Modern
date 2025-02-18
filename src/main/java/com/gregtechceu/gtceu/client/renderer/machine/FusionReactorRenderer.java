package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.BloomUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import com.lowdragmc.shimmer.client.shader.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

import static net.minecraft.util.FastColor.ARGB32.*;

public class FusionReactorRenderer extends WorkableCasingMachineRenderer {

    public static final float FADEOUT = 60;

    protected float delta = 0;
    protected int lastColor = -1;

    public FusionReactorRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof FusionReactorMachine machine) {
            if (machine.getColor() == -1 && delta < 0) {
                return;
            }
            if (GTCEu.Mods.isShimmerLoaded()) {
                PoseStack finalStack = RenderUtils.copyPoseStack(stack);
                BloomUtils.entityBloom(source -> renderLightRing(machine, partialTicks, finalStack, source));
            } else {
                renderLightRing(machine, partialTicks, stack, buffer);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks, PoseStack stack,
                                 MultiBufferSource buffer) {
        var color = machine.getColor();
        var alpha = 1f;
        if (color != -1) {
            lastColor = color;
            delta = FADEOUT;
        } else {
            alpha = delta / FADEOUT;
            lastColor = color(Mth.floor(alpha * 255), red(lastColor), green(lastColor), blue(lastColor));
            delta -= Minecraft.getInstance().getDeltaFrameTime();
        }

        final var lerpFactor = Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25;
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        var r = Mth.lerp(lerpFactor, red(lastColor), 255) / 255f;
        var g = Mth.lerp(lerpFactor, green(lastColor), 255) / 255f;
        var b = Mth.lerp(lerpFactor, blue(lastColor), 255) / 255f;
        RenderBufferHelper.renderRing(stack, buffer.getBuffer(GTRenderTypes.getLightRing()),
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, alpha, axis);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getViewDistance() {
        return 32;
    }
}
