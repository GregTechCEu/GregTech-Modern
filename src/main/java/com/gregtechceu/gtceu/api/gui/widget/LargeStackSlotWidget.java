package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.transfer.item.LargeStackItemHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/** Slot widget that displays and manipulates stacks larger than the vanilla item limit. */
public class LargeStackSlotWidget extends SlotWidget {

    public LargeStackSlotWidget() {}

    public LargeStackSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                                boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    @Override
    protected Slot createSlot(IItemHandlerModifiable itemHandler, int index) {
        return new LargeStackSlot(itemHandler, index, 0, 0);
    }

    @Override
    public List<Component> getTooltipTexts() {
        List<Component> tooltips = super.getTooltipTexts();
        if (slotReference != null && slotReference.getMaxStackSize(slotReference.getItem()) != 0 &&
                slotReference.getItem().getMaxStackSize() != slotReference.getMaxStackSize(slotReference.getItem())) {
            tooltips.add(Component.translatable("gtceu.gui.large_stack.amount",
                    FormattingUtil.formatNumbers(slotReference.getItem().getCount()),
                    FormattingUtil.formatNumbers(slotReference.getMaxStackSize(slotReference.getItem()))));
        }
        return tooltips;
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
        Position pos = getPosition();
        if (slotReference != null) {
            ItemStack stack = getRealStack(slotReference.getItem());
            ModularUIGuiContainer modularUI = gui == null ? null : gui.getModularUIGui();
            if (stack.isEmpty() && modularUI != null && modularUI.getQuickCrafting() &&
                    modularUI.getQuickCraftSlots().contains(slotReference)) {
                int splitSize = modularUI.getQuickCraftSlots().size();
                stack = gui.getModularUIContainer().getCarried();
                if (!stack.isEmpty() && splitSize > 1 &&
                        AbstractContainerMenu.canItemQuickReplace(slotReference, stack, true)) {
                    stack = stack.copy();
                    stack.grow(AbstractContainerMenu.getQuickCraftPlaceCount(modularUI.getQuickCraftSlots(),
                            modularUI.dragSplittingLimit, stack));
                    stack.setCount(Math.min(stack.getCount(), slotReference.getMaxStackSize(stack)));
                }
            }

            if (!stack.isEmpty()) {
                String amount = slotReference.getMaxStackSize(slotReference.getItem()) == 0 ? null :
                        TextFormattingUtil.formatLongToCompactString(stack.getCount(), 3);
                DrawerHelper.drawItemStack(graphics, stack, pos.x + 1, pos.y + 1, -1, "");
                if (amount != null) drawAmount(graphics, amount, pos);
            }
        }

        drawOverlay(graphics, mouseX, mouseY, partialTicks);
        if (drawHoverOverlay && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(graphics, getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    private static void drawAmount(GuiGraphics graphics, String amount, Position pos) {
        Font font = Minecraft.getInstance().font;
        int width = font.width(amount);
        float scale = Math.min(1.0f, 15.0f / width);

        graphics.pose().pushPose();
        graphics.pose().translate(pos.x + 17, pos.y + 18, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, amount, -width, -font.lineHeight, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    public class LargeStackSlot extends WidgetSlotItemHandler {

        private final IItemHandlerModifiable itemHandler;
        private final int index;

        public LargeStackSlot(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.itemHandler = itemHandler;
            this.index = index;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            if (itemHandler instanceof LargeStackItemHandler handler) return handler.getStackLimit(index, stack);
            return super.getMaxStackSize(stack);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            ItemStack current = itemHandler.getStackInSlot(index);
            return super.mayPlace(stack) &&
                    (current.getCount() <= current.getMaxStackSize() || ItemStack.isSameItemSameTags(stack, current));
        }

        @Override
        public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
            return super.tryRemove(Math.min(count, itemHandler.getStackInSlot(index).getMaxStackSize()), decrement,
                    player);
        }
    }
}
