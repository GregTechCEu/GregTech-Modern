package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Used for setting a "ghost" IC for a machine
 */
public class GhostCircuitSlotWidget extends SlotWidget {

    private static final int SET_TO_ZERO = 1;
    private static final int SET_TO_EMPTY = 2;
    private static final int SET_TO_N = 3;

    private static final int NO_CONFIG = -1;

    @Getter
    private IItemHandlerModifiable circuitInventory;
    @Nullable
    private Widget configurator;

    public GhostCircuitSlotWidget() {
        super();
    }

    public void setCircuitInventory(IItemHandlerModifiable circuitInventory) {
        this.circuitInventory = circuitInventory;
        setHandlerSlot(circuitInventory, 0);
    }

    public boolean isConfiguratorOpen() {
        return configurator != null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (button == 0 && Screen.hasShiftDown()) {
                // open popup on shift-left-click
                if (!isConfiguratorOpen()) {
                    this.gui.widget(configurator = createConfigurator());
                } else {
                    this.gui.mainGroup.removeWidget(configurator);
                    configurator = null;
                }
            } else if (button == 0) {
                // increment on left-click
                int newValue = getNextValue(true);
                setCircuitValue(newValue);
            } else if (button == 1 && Screen.hasShiftDown()) {
                // clear on shift-right-click
                this.circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
                writeClientAction(SET_TO_EMPTY, buf -> {});
            } else if (button == 1) {
                // decrement on right-click
                int newValue = getNextValue(false);
                setCircuitValue(newValue);
            }
            return true;
        }
        return false;
    }

    private int getNextValue(boolean increment) {
        int currentValue = IntCircuitBehaviour.getCircuitConfiguration(this.circuitInventory.getStackInSlot(0));
        if (increment) {
            // if at max, loop around to no circuit
            if (currentValue == IntCircuitBehaviour.CIRCUIT_MAX) {
                return 0;
            }
            // if at no circuit, skip 0 and return 1
            if (this.circuitInventory.getStackInSlot(0).isEmpty()) {
                return 1;
            }
            // normal case: increment by 1
            return currentValue + 1;
        } else {
            // if at no circuit, loop around to max
            if (this.circuitInventory.getStackInSlot(0).isEmpty()) {
                return IntCircuitBehaviour.CIRCUIT_MAX;
            }
            // if at 1, skip 0 and return no circuit
            if (currentValue == 1) {
                return NO_CONFIG;
            }
            // normal case: decrement by 1
            return currentValue - 1;
        }
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isConfiguratorOpen()) return true;
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            int newValue = getNextValue(wheelDelta >= 0);
            setCircuitValue(newValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    public void setCircuitValue(int newValue) {
        if (newValue == NO_CONFIG) {
            this.circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
            writeClientAction(SET_TO_EMPTY, buf -> {});
        } else {
            this.circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            writeClientAction(SET_TO_N, buf -> buf.writeVarInt(newValue));
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        switch (id) {
            case SET_TO_ZERO -> this.circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(0));
            case SET_TO_EMPTY -> this.circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
            case SET_TO_N -> this.circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(buffer.readVarInt()));
        }
    }

    public Widget createConfigurator() {
        var group = new WidgetGroup(0, 0, 174, 132);
        group.addWidget(new LabelWidget(9, 8, "Programmed Circuit Configuration"));
        group.addWidget(new SlotWidget(this.circuitInventory, 0, (group.getSize().width - 18) / 2, 20,
                !ConfigHolder.INSTANCE.machines.ghostCircuit, !ConfigHolder.INSTANCE.machines.ghostCircuit)
                .setBackground(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY)));
        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            group.addWidget(new ButtonWidget((group.getSize().width - 18) / 2, 20, 18, 18, IGuiTexture.EMPTY,
                    clickData -> {
                        if (!clickData.isRemote) {
                            circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
                        }
                    }));
        }
        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                group.addWidget(new ButtonWidget(5 + (18 * y), 48 + (18 * x), 18, 18,
                        new GuiTextureGroup(GuiTextures.SLOT,
                                new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                        clickData -> {
                            if (!clickData.isRemote) {
                                ItemStack stack = circuitInventory.getStackInSlot(0).copy();
                                if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                                    IntCircuitBehaviour.setCircuitConfiguration(stack, finalIdx);
                                    circuitInventory.setStackInSlot(0, stack);
                                } else if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                                    circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(finalIdx));
                                }
                            }
                        }));
                idx++;
            }
        }
        for (int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            group.addWidget(new ButtonWidget(5 + (18 * x), 102, 18, 18,
                    new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                    clickData -> {
                        if (!clickData.isRemote) {
                            ItemStack stack = circuitInventory.getStackInSlot(0).copy();
                            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                                IntCircuitBehaviour.setCircuitConfiguration(stack, finalIdx);
                                circuitInventory.setStackInSlot(0, stack);
                            } else if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                                circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(finalIdx));
                            }
                        }
                    }));
        }
        group.setBackground(GuiTextures.BACKGROUND);
        return group;
    }
}
