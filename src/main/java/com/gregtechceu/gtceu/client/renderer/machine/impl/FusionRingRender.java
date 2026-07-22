package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.bloom.*;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL11;

import static net.minecraft.util.FastColor.ARGB32.*;

public class FusionRingRender extends DynamicRender<FusionReactorMachine, FusionRingRender> {

    // spotless:off
    public static final Codec<FusionRingRender> CODEC = Codec.unit(FusionRingRender::new);
    public static final DynamicRenderType<FusionReactorMachine, FusionRingRender> TYPE = new DynamicRenderType<>(FusionRingRender.CODEC);
    // spotless:on

    public static final float FADEOUT = 60;

    public FusionRingRender() {}

    @Override
    public DynamicRenderType<FusionReactorMachine, FusionRingRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(FusionReactorMachine machine, Vec3 cameraPos) {
        return (machine.recipeLogic.isWorking() || machine.delta > 0) && super.shouldRender(machine, cameraPos);
    }

    @Override
    public void render(FusionReactorMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.recipeLogic.isWorking() && machine.delta <= 0) {
            return;
        }

        if (machine.getRegisteredBloomTicket().isValid() && !machine.isFormed()) {
            machine.getRegisteredBloomTicket().invalidate();
        }
        if (!machine.getRegisteredBloomTicket().isValid() && BloomShaderManager.isBloomActive()) {
            BloomRenderTicket ticket = BloomHandler.registerBloomRender(FusionBloomEffect.SETUP,
                    new FusionBloomEffect(machine), machine);

            machine.setRegisteredBloomTicket(ticket);
        }

        renderLightRing(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.lightRing()));
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks,
                                 PoseStack stack, VertexConsumer buffer) {
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        float alpha = 1f;
        if (machine.recipeLogic.isWorking()) {
            machine.lastColor = machine.getColor();
            machine.delta = FADEOUT;
        } else {
            alpha = machine.delta / FADEOUT;
            machine.lastColor = color(Mth.floor(alpha * 255), red(machine.lastColor), green(machine.lastColor),
                    blue(machine.lastColor));
            machine.delta -= Minecraft.getInstance().getDeltaFrameTime();
        }

        final var lerpFactor = Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25;
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        var r = Mth.lerp(lerpFactor, red(machine.lastColor), 255) / 255f;
        var g = Mth.lerp(lerpFactor, green(machine.lastColor), 255) / 255f;
        var b = Mth.lerp(lerpFactor, blue(machine.lastColor), 255) / 255f;
        RenderBufferHelper.renderRing(stack, buffer,
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                6, 0.2F, 10, 20,
                r, g, b, alpha, axis);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorMachine machine) {
        return machine.recipeLogic.isWorking() || machine.delta > 0;
    }

    @Override
    public AABB getRenderBoundingBox(FusionReactorMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(getViewDistance() / 2.0D);
    }

    @RequiredArgsConstructor
    private final class FusionBloomEffect implements IBloomEffect {

        private final FusionReactorMachine machine;

        private static final IRenderSetup SETUP = new IRenderSetup() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void preDraw(BufferBuilder buffer) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public void postDraw(BufferBuilder buffer) {
                BufferUploader.drawWithShader(buffer.end());
            }
        };

        @Override
        public void renderBloomEffect(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
            BlockPos pos = machine.getBlockPos();

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            FusionRingRender.this.renderLightRing(machine, context.partialTicks(), poseStack, buffer);
            poseStack.popPose();
        }

        @Override
        public boolean shouldRenderBloomEffect(EffectRenderContext context) {
            return FusionRingRender.this.shouldRenderOffScreen(machine) &&
                    context.frustum().isVisible(FusionRingRender.this.getRenderBoundingBox(machine));
        }
    }
}
