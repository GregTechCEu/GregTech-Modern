package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.IRenderSetup;
import com.gregtechceu.gtceu.client.shader.post.BloomEffect;
import com.gregtechceu.gtceu.client.shader.post.BloomType;
import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import com.gregtechceu.gtceu.client.util.EffectRenderContext;
import com.gregtechceu.gtceu.client.util.IBloomEffect;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.interpolate.Eases;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class FusionReactorRenderer extends WorkableCasingMachineRenderer {

    public FusionReactorRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof FusionReactorMachine machine &&
                !machine.isRegisteredBloomTicket()) {
            machine.setRegisteredBloomTicket(true);
            BloomEffectUtil.registerBloomRender(FusionBloomSetup.INSTANCE, getBloomType(),
                    new FusionBloomEffect(machine), blockEntity);
        }
    }

    private static BloomType getBloomType() {
        ConfigHolder.ClientConfigs.ShaderOptions.FusionBloom fusionBloom = ConfigHolder.INSTANCE.client.shader.fusionBloom;
        return BloomType.fromValue(fusionBloom.useShader ? fusionBloom.bloomStyle : -1);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(FusionReactorMachine machine, float partialTicks, PoseStack stack,
                                 VertexConsumer buffer, @NotNull EffectRenderContext context) {
        var color = machine.getColor();
        if (color == -1) return;
        int ringColor = ColorUtils.blendColor(color, -1, Eases.EaseQuadIn.getInterpolation(
                Math.abs((Math.abs(machine.getOffsetTimer() % 50) + partialTicks) - 25) / 25));
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        BlockPos pos = machine.getPos();
        Direction relativeBack = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        Direction.Axis axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        float a = ColorUtils.alpha(ringColor);
        float r = ColorUtils.red(ringColor);
        float g = ColorUtils.green(ringColor);
        float b = ColorUtils.blue(ringColor);
        RenderBufferHelper.renderRing(stack, buffer,
                pos.getX() + relativeBack.getStepX() * 7 + 0.5F,
                pos.getY() + relativeBack.getStepY() * 7 + 0.5F,
                pos.getZ() + relativeBack.getStepZ() * 7 + 0.5F,
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

    @RequiredArgsConstructor
    private final class FusionBloomEffect implements IBloomEffect {

        private final FusionReactorMachine machine;

        private static final BufferBuilder lightRingBuffer = new BufferBuilder(
                GTRenderTypes.getLightRing().bufferSize());

        @Override
        public void renderBloomEffect(@NotNull PoseStack poseStack, @NotNull BufferBuilder buffer,
                                      @NotNull EffectRenderContext context) {
            lightRingBuffer.begin(GTRenderTypes.getLightRing().mode(), GTRenderTypes.getLightRing().format());
            FusionReactorRenderer.this.renderLightRing(machine, context.partialTicks(), poseStack, lightRingBuffer,
                    context);
            BufferUploader.drawWithShader(lightRingBuffer.end());
        }

        @Override
        public boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
            return machine.getColor() != null && context.frustum()
                    .isVisible(new AABB(machine.getPos()).inflate(FusionReactorRenderer.this.getViewDistance()));
        }
    }

    private static final class FusionBloomSetup implements IRenderSetup {

        private static final FusionBloomSetup INSTANCE = new FusionBloomSetup();

        @Override
        public void preDraw(@NotNull BufferBuilder buffer) {
            BloomEffect.strength = (float) ConfigHolder.INSTANCE.client.shader.fusionBloom.strength;
            BloomEffect.baseBrightness = (float) ConfigHolder.INSTANCE.client.shader.fusionBloom.baseBrightness;
            BloomEffect.highBrightnessThreshold = (float) ConfigHolder.INSTANCE.client.shader.fusionBloom.highBrightnessThreshold;
            BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.INSTANCE.client.shader.fusionBloom.lowBrightnessThreshold;
            BloomEffect.step = 1;

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        @Override
        public void postDraw(@NotNull BufferBuilder buffer) {}
    }
}
