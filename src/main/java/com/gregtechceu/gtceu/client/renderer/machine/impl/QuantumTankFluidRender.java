package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;

import java.util.EnumSet;

import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumChestItemRender.*;

public class QuantumTankFluidRender extends DynamicRender<QuantumTankMachine, QuantumTankFluidRender> {

    // spotless:off
    public static final Codec<QuantumTankFluidRender> CODEC = Codec.unit(QuantumTankFluidRender::new);
    public static final DynamicRenderType<QuantumTankMachine, QuantumTankFluidRender> TYPE = new DynamicRenderType<>(QuantumTankFluidRender.CODEC);
    // spotless:on

    private static final float MIN = 0.16f;
    private static final float MAX = 0.84f;

    private static @Nullable Item CREATIVE_FLUID_ITEM = null;

    public QuantumTankFluidRender() {}

    @Override
    public DynamicRenderType<QuantumTankMachine, QuantumTankFluidRender> getType() {
        return TYPE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (CREATIVE_FLUID_ITEM == null) CREATIVE_FLUID_ITEM = GTMachines.CREATIVE_FLUID.getItem();
        if (stack.hasTag()) {
            poseStack.pushPose();
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            FluidStack stored = FluidStack.loadFluidStackFromNBT(stack.getOrCreateTagElement("stored"));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            if (storedAmount == 0 && !stored.isEmpty()) storedAmount = stored.getAmount();
            long maxAmount = stack.getOrCreateTag().getLong("maxAmount");
            // Don't need to handle locked fluids here since they don't get saved to the item
            renderTank(poseStack, buffer, Direction.NORTH, Direction.NORTH,
                    stored, storedAmount, maxAmount, FluidStack.EMPTY,
                    stack.is(CREATIVE_FLUID_ITEM));

            poseStack.popPose();
        }
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void render(QuantumTankMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        setupModelRotation(machine, poseStack);

        renderTank(poseStack, buffer, machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.getStored(), machine.getStoredAmount(), machine.getMaxAmount(), machine.getLockedFluid(),
                machine instanceof CreativeTankMachine);

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTank(PoseStack poseStack, MultiBufferSource buffer,
                           Direction frontFacing, Direction upwardsFacing,
                           FluidStack stored, long storedAmount, long maxAmount, FluidStack locked,
                           boolean isCreative) {
        FluidStack fluid = !stored.isEmpty() ? stored : locked;
        if (fluid.isEmpty()) return;

        var ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        var fluidSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluid));

        EnumSet<Direction> sidesToRender = EnumSet.of(frontFacing);
        VertexConsumer builder = buffer.getBuffer(Sheets.translucentCullBlockSheet());

        var gas = fluid.getFluid().getFluidType().isLighterThanAir();
        var percentFull = isCreative || maxAmount <= storedAmount ? 1f : (float) storedAmount / maxAmount;

        var maxTop = gas ? MAX : MIN + percentFull * (MAX - MIN);
        var minBot = gas ? MIN + (1 - percentFull) * (MAX - MIN) : MIN;
        float minY, maxY, minZ, maxZ;
        if (frontFacing.getAxis() == Direction.Axis.Y) {
            minY = MIN;
            maxY = MAX;
            if (frontFacing == Direction.UP) {
                minZ = minBot;
                maxZ = maxTop;
                // the output is already rotated in the pose stack, so we don't need to rotate it again here
                sidesToRender.add(gas ? Direction.SOUTH : Direction.NORTH);
            } else {
                // -z is top
                minZ = 1 - maxTop;
                maxZ = 1 - minBot;
                // the output is already rotated in the pose stack, so we don't need to rotate it again here
                sidesToRender.add(gas ? Direction.NORTH : Direction.SOUTH);
            }
        } else {
            minY = minBot;
            maxY = maxTop;
            minZ = MIN;
            maxZ = MAX;

            sidesToRender.add(gas ? Direction.DOWN : Direction.UP);
        }
        RenderBufferHelper.renderCube(builder, poseStack.last(), sidesToRender,
                ext.getTintColor(fluid) | 0xff000000, LightTexture.FULL_BRIGHT, fluidSprite,
                MIN, minY, minZ, MAX, maxY, maxZ);

        drawAmountText(poseStack, buffer, frontFacing, upwardsFacing, storedAmount, isCreative);
    }
}
