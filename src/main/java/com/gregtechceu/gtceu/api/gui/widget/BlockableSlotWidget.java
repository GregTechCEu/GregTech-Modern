package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.blaze3d.systems.RenderSystem;
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

    public BlockableSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                               boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems,
                canPutItems);
    }

    public BlockableSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
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
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isBlocked.getAsBoolean()) {
            Position pos = getPosition();
            Size size = getSize();
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            graphics.fill(pos.getX() + 1, pos.getY() + 1, pos.getX() + 1 + size.getWidth() - 2,
                    pos.getY() + 1 + size.getHeight() - 2, OVERLAY_COLOR);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
        }
    }

    @Override
    public boolean isMouseOverElement(double mouseX, double mouseY) {
        // prevent slot removal and hover highlighting when slot is blocked
        return super.isMouseOverElement(mouseX, mouseY) && !isBlocked.getAsBoolean();
    }
}
