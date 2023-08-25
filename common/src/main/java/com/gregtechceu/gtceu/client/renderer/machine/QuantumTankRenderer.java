package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote QuantumChestRenderer
 */
public class QuantumTankRenderer extends TieredHullMachineRenderer {

    public QuantumTankRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/quantum_tank"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        model = getItemBakedModel();
        if (model != null && stack.hasTag()) {
            poseStack.pushPose();
            model.getTransforms().getTransform(transformType).apply(leftHand, poseStack);
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            FluidStack tank = FluidStack.loadFromTag(stack.getOrCreateTagElement("stored"));
            renderTank(poseStack, buffer, Direction.NORTH, tank);

            poseStack.popPose();
        }
        super.renderItem(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() instanceof QuantumTankMachine machine) {
            renderTank(poseStack, buffer, machine.getFrontFacing(), machine.getStored());
        }
    }

    @Environment(EnvType.CLIENT)
    public void renderTank(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing, FluidStack stored) {
        if (!stored.isEmpty()) {
            var fluidTexture = FluidHelper.getStillTexture(stored);
            if (fluidTexture == null) {
                fluidTexture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
            }
            poseStack.pushPose();
            VertexConsumer builder = buffer.getBuffer(Sheets.translucentCullBlockSheet());
            RenderBufferUtils.renderCubeFace(poseStack, builder, 2.5f / 16, 2.5f / 16, 2.5f / 16, 13.5f / 16, 13.5f / 16, 13.5f / 16, FluidHelper.getColor(stored) | 0xff000000, 0xf000f0, fluidTexture);
            poseStack.popPose();

            poseStack.pushPose();
            RenderSystem.disableDepthTest();
            poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f, frontFacing.getStepZ() * -1 / 16f);
            RenderUtils.moveToFace(poseStack, 0, 0, 0, frontFacing);
            if (frontFacing.getAxis() == Direction.Axis.Y) {
                RenderUtils.rotateToFace(poseStack, frontFacing, frontFacing == Direction.UP ? Direction.SOUTH : Direction.NORTH);
            } else {
                RenderUtils.rotateToFace(poseStack, frontFacing, null);
            }
            var amount = TextFormattingUtil.formatLongToCompactString(stored.getAmount(), 4);
            poseStack.scale(1f / 64, 1f / 64, 0);
            poseStack.translate(-32, -32, 0);
            new TextTexture(amount).draw(GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder())), 0, 0, 0, 24, 64, 28);
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }

    @Environment(EnvType.CLIENT)
    public float reBakeCustomQuadsOffset() {
        return 0f;
    }
}
