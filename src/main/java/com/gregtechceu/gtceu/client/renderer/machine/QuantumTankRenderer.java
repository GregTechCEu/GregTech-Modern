package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.*;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote QuantumChestRenderer
 */
public class QuantumTankRenderer extends TieredHullMachineRenderer {

    private static final float MIN = 0.16f;
    private static final float MAX = 0.84f;

    private static Item CREATIVE_FLUID_ITEM = null;

    public QuantumTankRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/quantum_tank"));
    }

    public QuantumTankRenderer(int tier, ResourceLocation modelLocation) {
        super(tier, modelLocation);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        if (CREATIVE_FLUID_ITEM == null) CREATIVE_FLUID_ITEM = GTMachines.CREATIVE_FLUID.getItem();
        model = getItemBakedModel();
        if (model != null && stack.hasTag()) {
            poseStack.pushPose();
            model.getTransforms().getTransform(transformType).apply(leftHand, poseStack);
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            FluidStack stored = FluidStack.loadFluidStackFromNBT(stack.getOrCreateTagElement("stored"));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            if (storedAmount == 0 && !stored.isEmpty()) storedAmount = stored.getAmount();
            long maxAmount = stack.getOrCreateTag().getLong("maxAmount");
            // Don't need to handle locked fluids here since they don't get saved to the item
            renderTank(poseStack, buffer, Direction.NORTH, stored, storedAmount, maxAmount, FluidStack.EMPTY,
                    stack.is(CREATIVE_FLUID_ITEM));

            poseStack.popPose();
        }
        super.renderItem(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof QuantumTankMachine machine) {
            poseStack.pushPose();
            var frontFacing = machine.getFrontFacing();
            var upwardFacing = machine.getUpwardsFacing();
            poseStack.translate(.5, .5, .5);
            rotateMatrix(poseStack.last().pose(),
                    upwardFacingAngle(upwardFacing) + (upwardFacing.getAxis() == Direction.Axis.X ? Mth.PI : 0),
                    frontFacing.getStepX(), frontFacing.getStepY(), frontFacing.getStepZ());
            poseStack.translate(-.5, -.5, -.5);
            renderTank(poseStack, buffer, frontFacing, machine.getStored(), machine.getStoredAmount(),
                    machine.getMaxAmount(), machine.getLockedFluid(), machine instanceof CreativeTankMachine);
            poseStack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTank(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing, FluidStack stored,
                           long storedAmount, long maxAmount, FluidStack locked, boolean isCreative) {
        FluidStack fluid = !stored.isEmpty() ? stored : locked;
        if (fluid.isEmpty()) return;

        var ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        var texture = ext.getStillTexture();
        var fluidTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        poseStack.pushPose();
        VertexConsumer builder = buffer.getBuffer(Sheets.translucentCullBlockSheet());
        var gas = fluid.getFluid().getFluidType().isLighterThanAir();
        var percentFull = isCreative || maxAmount <= storedAmount ? 1f : (float) storedAmount / maxAmount;
        var facingYAxis = frontFacing.getAxis() == Direction.Axis.Y;
        var maxTop = gas ? MAX : MIN + percentFull * (MAX - MIN);
        var minBot = gas ? MIN + (1 - percentFull) * (MAX - MIN) : MIN;
        float minY, maxY, minZ, maxZ;
        if (facingYAxis) {
            minY = MIN;
            maxY = MAX;
            if (frontFacing == Direction.UP) {
                minZ = minBot;
                maxZ = maxTop;
            } else {
                // -z is top
                minZ = 1 - maxTop;
                maxZ = 1 - minBot;
            }
        } else {
            minY = minBot;
            maxY = maxTop;
            minZ = MIN;
            maxZ = MAX;
        }
        RenderBufferUtils.renderCubeFace(poseStack, builder, MIN, minY, minZ, MAX, maxY, maxZ,
                ext.getTintColor() | 0xff000000, 0xf000f0, fluidTexture);
        poseStack.popPose();

        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        poseStack.translate(frontFacing.getStepX() * -1 / 16f, frontFacing.getStepY() * -1 / 16f,
                frontFacing.getStepZ() * -1 / 16f);
        RenderUtils.moveToFace(poseStack, 0, 0, 0, frontFacing);
        if (facingYAxis) {
            RenderUtils.rotateToFace(poseStack, frontFacing,
                    frontFacing == Direction.UP ? Direction.SOUTH : Direction.NORTH);
        } else {
            RenderUtils.rotateToFace(poseStack, frontFacing, null);
        }
        poseStack.scale(1f / 64, 1f / 64, 0);
        poseStack.translate(-32, -32, 0);
        TransformTexture text;
        if (isCreative) {
            text = new TextTexture("∞").setDropShadow(false).scale(3.0f);
        } else {
            var amount = stored.isEmpty() ? "*" : FormattingUtil.formatNumberReadable(storedAmount, true);
            text = new TextTexture(amount).setDropShadow(false);
        }
        text.draw(GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack,
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder())),
                0, 0, 0, 24, 64, 28);
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public float reBakeCustomQuadsOffset() {
        return 0f;
    }
}
