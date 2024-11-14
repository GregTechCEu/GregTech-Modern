package com.gregtechceu.gtceu.integration.ae2.gui.widget.slot;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

public class AEPatternViewSlotWidget extends SlotWidget {

    protected IGuiTexture occupiedTexture;

    public AEPatternViewSlotWidget() {}

    public AEPatternViewSlotWidget(
                                   Container inventory,
                                   int slotIndex,
                                   int xPosition,
                                   int yPosition,
                                   boolean canTakeItems,
                                   boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public AEPatternViewSlotWidget(
                                   IItemHandlerModifiable itemHandler,
                                   int slotIndex,
                                   int xPosition,
                                   int yPosition,
                                   boolean canTakeItems,
                                   boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public AEPatternViewSlotWidget(
                                   IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    public AEPatternViewSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition);
    }

    public AEPatternViewSlotWidget setOccupiedTexture(IGuiTexture... occupiedTexture) {
        this.occupiedTexture = occupiedTexture.length > 1 ? new GuiTextureGroup(occupiedTexture) : occupiedTexture[0];
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (occupiedTexture != null) {
            occupiedTexture.updateTick();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawBackgroundTexture(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        if (getHandler() != null && getHandler().hasItem()) {
            if (occupiedTexture != null) {
                occupiedTexture.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            }
        } else {
            if (backgroundTexture != null) {
                backgroundTexture.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            }
        }

        if (hoverTexture != null && isMouseOverElement(mouseX, mouseY)) {
            hoverTexture.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }
    }
}
