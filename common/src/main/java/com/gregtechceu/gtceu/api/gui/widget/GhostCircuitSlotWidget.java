package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.item.GhostCircuitItem;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.client.utils.TooltipHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;


/**
 * Used for setting a "ghost" IC for a machine
 */
public class GhostCircuitSlotWidget extends SlotWidget {

    private static final int SET_TO_ZERO = 1;
    private static final int SET_TO_EMPTY = 2;
    private static final int SET_TO_N = 3;

    private final GhostCircuitItem circuitInventory;

    public GhostCircuitSlotWidget(GhostCircuitItem circuitInventory, int slotIndex, int xPosition, int yPosition) {
        super(circuitInventory, slotIndex, xPosition, yPosition, false, false);
        this.circuitInventory = circuitInventory;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ClickData clickData = new ClickData();
            if (button == 0 && clickData.isShiftClick) {
                // open popup on shift-left-click
                // todo add this one day
            } else if (button == 0) {
                // increment on left-click
                int newValue = getNextValue(true);
                this.circuitInventory.setCircuitValue(newValue);
                writeClientAction(SET_TO_N, buf -> buf.writeVarInt(newValue));

            } else if (button == 1 && clickData.isShiftClick) {
                // clear on shift-right-click
                this.circuitInventory.setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
                writeClientAction(SET_TO_EMPTY, buf -> {});
            } else if (button == 1) {
                // decrement on right-click
                int newValue = getNextValue(false);
                this.circuitInventory.setCircuitValue(newValue);
                writeClientAction(SET_TO_N, buf -> buf.writeVarInt(newValue));
            }
            return true;
        }
        return false;
    }

    private int getNextValue(boolean increment) {
        if (increment) {
            // if at max, loop around to no circuit
            if (this.circuitInventory.getCircuitValue() == IntCircuitIngredient.CIRCUIT_MAX) {
                return GhostCircuitItemStackHandler.NO_CONFIG;
            }
            // if at no circuit, skip 0 and return 1
            if (!this.circuitInventory.hasCircuitValue()) {
                return 1;
            }
            // normal case: increment by 1
            return this.circuitInventory.getCircuitValue() + 1;
        } else {
            // if at no circuit, loop around to max
            if (!this.circuitInventory.hasCircuitValue()) {
                return IntCircuitIngredient.CIRCUIT_MAX;
            }
            // if at 1, skip 0 and return no circuit
            if (this.circuitInventory.getCircuitValue() == 1) {
                return GhostCircuitItemStackHandler.NO_CONFIG;
            }
            // normal case: decrement by 1
            return this.circuitInventory.getCircuitValue() - 1;
        }
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            int newValue = getNextValue(wheelDelta >= 0);
            this.circuitInventory.setCircuitValue(newValue);
            writeClientAction(SET_TO_N, buf -> buf.writeVarInt(newValue));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return false;
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stackHeld = player.inventory.getItemStack();

        if (IntCircuitIngredient.isIntegratedCircuit(stackHeld)) {
            this.circuitInventory.setCircuitValueFromStack(stackHeld);
            return this.circuitInventory.getStackInSlot(0).copy();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        switch (id) {
            case SET_TO_ZERO:
                this.circuitInventory.setCircuitValue(0);
                return;
            case SET_TO_EMPTY:
                this.circuitInventory.setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
                return;
            case SET_TO_N:
                this.circuitInventory.setCircuitValue(buffer.readVarInt());
        }
    }
}
