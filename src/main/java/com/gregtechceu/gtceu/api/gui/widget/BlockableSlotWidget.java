package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.Container;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Basically just your normal SlotWidget, but can render the slot as "grayed-out" with a Supplier value. */
public class BlockableSlotWidget extends SlotWidget {

    private static final int OVERLAY_COLOR = 0x80404040;

    private BooleanSupplier isBlocked = () -> false;

    public BlockableSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                               boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BlockableSlotWidget(IItemTransfer itemHandler, int slotIndex, int xPosition, int yPosition,
                               boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BlockableSlotWidget(IItemTransfer itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    public BlockableSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition);
    }

    public BlockableSlotWidget setIsBlocked(BooleanSupplier isBlocked) {
        this.isBlocked = isBlocked;
        return this;
    }

    @Override
    public void drawInBackground(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isBlocked.getAsBoolean()) {
            Position pos = getPosition();
            Size size = getSize();
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(graphics, pos.getX() + 1, pos.getY() + 1, size.getWidth() - 2, size.getHeight() - 2, OVERLAY_COLOR);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
        }
    }

    @Override
    public boolean isMouseOverElement(double mouseX, double mouseY) {
        return super.isMouseOverElement(mouseX, mouseY) && !isBlocked.getAsBoolean();
    }
}