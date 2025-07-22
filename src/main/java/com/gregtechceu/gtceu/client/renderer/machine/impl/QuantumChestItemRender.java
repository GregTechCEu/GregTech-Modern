package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.PoseStackExtensions;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.*;

@ExtensionMethod(PoseStackExtensions.class)
public class QuantumChestItemRender extends DynamicRender<QuantumChestMachine, QuantumChestItemRender> {

    // spotless:off
    public static final Codec<QuantumChestItemRender> CODEC = Codec.unit(QuantumChestItemRender::new);
    public static final DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> TYPE = new DynamicRenderType<>(QuantumChestItemRender.CODEC);
    // spotless:on

    private static @Nullable Item CREATIVE_CHEST_ITEM = null;

    public QuantumChestItemRender() {}

    @Override
    public DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> getType() {
        return TYPE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (CREATIVE_CHEST_ITEM == null) CREATIVE_CHEST_ITEM = GTMachines.CREATIVE_ITEM.getItem();
        if (stack.hasTag()) {
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.5f, -0.5f);

            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            float totalTick = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
            // Don't need to handle locked items here since they don't get saved to the item
            renderChestItem(poseStack, buffer, totalTick, Direction.NORTH,
                    itemStack, storedAmount, ItemStack.EMPTY, stack.is(CREATIVE_CHEST_ITEM));

            poseStack.popPose();
        }
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void render(QuantumChestMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        setupModelRotation(machine, poseStack);

        var totalTick = machine.getLevel().getGameTime() + partialTick;
        renderChestItem(poseStack, buffer, totalTick, machine.getFrontFacing(),
                machine.getStored(), machine.getStoredAmount(), machine.getLockedItem(),
                machine instanceof CreativeChestMachine);
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderChestItem(PoseStack poseStack, MultiBufferSource buffer, float totalTick, Direction frontFacing,
                                ItemStack stored, long storedAmount, ItemStack locked, boolean isCreative) {
        ItemStack itemStack = !stored.isEmpty() ? stored : locked;
        if (itemStack.isEmpty()) return;
        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (frontFacing.getAxis() == Direction.Axis.Y) {
            Quaternionf rotation = getRotation(Direction.NORTH, frontFacing);
            poseStack.mulPose(rotation);
        }
        poseStack.mulPose(new Quaternionf().rotateY(totalTick * Mth.TWO_PI / 80));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, Minecraft.getInstance().level,
                Item.getId(itemStack.getItem()) + itemStack.getDamageValue());
        poseStack.popPose();

        drawAmountText(poseStack, buffer, frontFacing, storedAmount, isCreative);
    }

    public static void setupModelRotation(MetaMachine machine, PoseStack poseStack) {
        var frontFacing = machine.getFrontFacing();
        var upwardFacing = machine.getUpwardsFacing();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        rotateMatrix(poseStack.last().pose(),
                upwardFacingAngle(upwardFacing) + (upwardFacing.getAxis() == Direction.Axis.X ? Mth.PI : 0),
                getDirectionAxis(frontFacing));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void drawAmountText(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing,
                                      long storedAmount, boolean isCreative) {
        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f,
                frontFacing.getStepZ() * -1 / 16f);

        RenderUtil.moveToFace(poseStack, 0.5f, 0.5f, 0.5f, frontFacing);
        RenderUtil.rotateToFace(poseStack, frontFacing, Direction.NORTH);
        poseStack.scale(1f / 64, 1f / 64, 0);
        poseStack.translate(-32, -32, 0);

        String text;
        int x = 0, y = 24;
        int w = 64, h = 28;
        float textX = x + w / 2.0f;
        float textY = y + h / 2.0f;

        poseStack.pushPose();
        if (isCreative) {
            text = "∞";
            poseStack.translate(textX, textY, 0);
            poseStack.scale(3.0f, 3.0f, 1.0f);
            poseStack.translate(-textX, -textY, 0);
        } else {
            text = storedAmount <= 0 ? "*" : FormattingUtil.formatNumberReadable(storedAmount, false);
        }

        Font font = Minecraft.getInstance().font;
        font.drawInBatch(text, textX - font.getSplitter().stringWidth(text) / 2.0f, textY - font.lineHeight / 2.0f,
                0xffffffff, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }
}
