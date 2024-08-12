package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.transfer.item.BigItemStackTransfer;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

public class BigSlotWidget extends SlotWidget {

    public BigSlotWidget() {}

    public BigSlotWidget(
                         Container inventory,
                         int slotIndex,
                         int xPosition,
                         int yPosition,
                         boolean canTakeItems,
                         boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BigSlotWidget(
                         BigItemStackTransfer itemHandler,
                         int slotIndex,
                         int xPosition,
                         int yPosition,
                         boolean canTakeItems,
                         boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BigSlotWidget(
                         BigItemStackTransfer itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    public BigSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition);
    }

    @Override
    protected Slot createSlot(IItemTransfer itemHandler, int index) {
        return new WidgetSlotItemTransfer(itemHandler, index, 0, 0) {

            @Override
            public int getMaxStackSize(@Nonnull ItemStack stack) {
                if (getItemHandler() instanceof BigItemStackTransfer bigTransfer) {
                    if (!bigTransfer.isAcceptTag() && stack.hasTag()) return 0;
                    ItemStack maxAdd = stack.copy();
                    int maxInput = stack.isStackable() ? bigTransfer.getSlotLimit(index) : 1;
                    maxAdd.setCount(maxInput);
                    ItemStack currentStack = itemHandler.getStackInSlot(index);
                    itemHandler.setStackInSlot(index, ItemStack.EMPTY);
                    ItemStack remainder = itemHandler.insertItem(index, maxAdd, true);
                    itemHandler.setStackInSlot(index, currentStack);
                    return maxInput - remainder.getCount();
                } else return super.getMaxStackSize(stack);
            }
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(
                                 @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
        Position pos = getPosition();
        if (slotReference != null) {
            ItemStack itemStack = getRealStack(slotReference.getItem());
            ModularUIGuiContainer modularUIGui = gui == null ? null : gui.getModularUIGui();
            if (itemStack.isEmpty() && modularUIGui != null && modularUIGui.getQuickCrafting() &&
                    modularUIGui.getQuickCraftSlots().contains(slotReference)) { // draw split
                int splitSize = modularUIGui.getQuickCraftSlots().size();
                itemStack = gui.getModularUIContainer().getCarried();
                if (!itemStack.isEmpty() && splitSize > 1 &&
                        AbstractContainerMenu.canItemQuickReplace(slotReference, itemStack, true)) {
                    itemStack = itemStack.copy();
                    itemStack.grow(AbstractContainerMenu.getQuickCraftPlaceCount(
                            modularUIGui.getQuickCraftSlots(), modularUIGui.dragSplittingLimit, itemStack));
                    int k = Math.min(itemStack.getMaxStackSize(), slotReference.getMaxStackSize(itemStack));
                    if (itemStack.getCount() > k) {
                        itemStack.setCount(k);
                    }
                }
            }
            if (!itemStack.isEmpty()) {
                DrawerHelper.drawItemStack(graphics, itemStack, pos.x + 1, pos.y + 1, -1, " ");
                if (itemStack.getCount() > 1) {
                    drawStringFixedCorner(graphics,
                            TextFormattingUtil.formatLongToCompactString(itemStack.getCount(), 4), pos.x + 17,
                            pos.x + 17, 16777215, true, 0.5f);
                }
            }
        }
        if (overlay != null) {
            overlay.draw(graphics, mouseX, mouseY, pos.x, pos.y, 18, 18);
        }
        if (drawHoverOverlay && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(
                    graphics, getPosition().x + 1, getPosition().y + 1, 16, 16, 0x80FFFFFF);
            RenderSystem.colorMask(true, true, true, true);
        }
    }
}
