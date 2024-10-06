package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;
import com.lowdragmc.shimmer.client.shader.RenderUtils;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public class FusionReactorRenderer extends WorkableCasingMachineRenderer {

    public FusionReactorRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof FusionReactorMachine machine) {
            renderLightRing(machine, partialTicks, stack, buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks, PoseStack stack,
                                 MultiBufferSource buffer) {
        var color = machine.getColor();
        if (color == -1) return;
        int ringColor = ColorUtils.blendColor(color, -1, Eases.EaseQuadIn.getInterpolation(
                Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25));
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        Direction relativeBack = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        Direction.Axis axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        float a = ColorUtils.alpha(ringColor);
        float r = ColorUtils.red(ringColor);
        float g = ColorUtils.green(ringColor);
        float b = ColorUtils.blue(ringColor);
        RenderBufferHelper.renderRing(stack, buffer.getBuffer(GTRenderTypes.getLightRing()),
                relativeBack.getStepX() * 7 + 0.5F,
                relativeBack.getStepY() * 7 + 0.5F,
                relativeBack.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, a, axis);
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
