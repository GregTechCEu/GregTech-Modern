package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.BloomUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import com.lowdragmc.shimmer.client.shader.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;

import static net.minecraft.util.FastColor.ARGB32.*;

public class FusionRingRender extends DynamicRender<FusionReactorMachine, FusionRingRender> {

    // spotless:off
    public static final Codec<FusionRingRender> CODEC = Codec.unit(FusionRingRender::new);
    public static final DynamicRenderType<FusionReactorMachine, FusionRingRender> TYPE = new DynamicRenderType<>(FusionRingRender.CODEC);
    // spotless:on

    public static final float FADEOUT = 60;

    protected float delta = 0;
    protected int lastColor = -1;

    public FusionRingRender() {}

    @Override
    public DynamicRenderType<FusionReactorMachine, FusionRingRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(FusionReactorMachine machine, Vec3 cameraPos) {
        return machine.recipeLogic.isWorking() || delta > 0;
    }

    @Override
    public void render(FusionReactorMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.recipeLogic.isWorking() && delta <= 0) {
            return;
        }
        if (GTCEu.Mods.isShimmerLoaded()) {
            PoseStack finalStack = RenderUtils.copyPoseStack(poseStack);
            BloomUtils.entityBloom(source -> renderLightRing(machine, partialTick, finalStack,
                    source.getBuffer(GTRenderTypes.getLightRing())));
        } else {
            renderLightRing(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks, PoseStack stack,
                                 VertexConsumer buffer) {
        var color = machine.getColor();
        var alpha = 1f;
        if (machine.recipeLogic.isWorking()) {
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
        var back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelative(front, upwards, flipped).getAxis();
        var r = Mth.lerp(lerpFactor, red(lastColor), 255) / 255f;
        var g = Mth.lerp(lerpFactor, green(lastColor), 255) / 255f;
        var b = Mth.lerp(lerpFactor, blue(lastColor), 255) / 255f;
        RenderBufferHelper.renderRing(stack, buffer,
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, alpha, axis);
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorMachine machine) {
        return machine.recipeLogic.isWorking() || delta > 0;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public AABB getRenderBoundingBox(FusionReactorMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance() / 2.0D);
    }
}
