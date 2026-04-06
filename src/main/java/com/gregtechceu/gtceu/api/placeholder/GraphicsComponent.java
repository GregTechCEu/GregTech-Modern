package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Supplier;

public record GraphicsComponent(float x, float y, float x2, float y2, String rendererId, CompoundTag renderData)
        implements Supplier<IMonitorRenderer> {

    public GraphicsComponent(double x, double y, double x2, double y2, String rendererId, CompoundTag renderData) {
        this((float) x, (float) y, (float) x2, (float) y2, rendererId, renderData);
    }

    public static final Codec<GraphicsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("x").forGetter(GraphicsComponent::x),
            Codec.FLOAT.fieldOf("y").forGetter(GraphicsComponent::y),
            Codec.FLOAT.fieldOf("x2").forGetter(GraphicsComponent::x2),
            Codec.FLOAT.fieldOf("y2").forGetter(GraphicsComponent::y2),
            Codec.STRING.fieldOf("rendererId").forGetter(GraphicsComponent::rendererId),
            CompoundTag.CODEC.fieldOf("renderData").forGetter(GraphicsComponent::renderData))
            .apply(instance, GraphicsComponent::new));

    @Override
    public IMonitorRenderer get() {
        return new IMonitorRenderer() {

            private final IMonitorRenderer renderer = PlaceholderHandler.getRenderer(rendererId, renderData);

            @Override
            public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(x, y, 0);
                assert this.renderer != null;
                this.renderer.render(machine, group, partialTick, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        };
    }

    public Tag toTag() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public static GraphicsComponent fromTag(Tag tag) {
        return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }
}
