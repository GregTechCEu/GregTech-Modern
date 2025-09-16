package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Supplier;

public record GraphicsComponent(double x, double y, double x2, double y2, String rendererId, CompoundTag renderData)
        implements Supplier<IMonitorRenderer> {

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
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("x2", x2);
        tag.putDouble("y2", y2);
        tag.putString("rendererId", rendererId);
        tag.put("renderData", renderData);
        return tag;
    }

    public static GraphicsComponent fromTag(Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag)) return null;
        return new GraphicsComponent(
                compoundTag.getDouble("x"),
                compoundTag.getDouble("y"),
                compoundTag.getDouble("x2"),
                compoundTag.getDouble("y2"),
                compoundTag.getString("rendererId"),
                compoundTag.getCompound("renderData"));
    }
}
