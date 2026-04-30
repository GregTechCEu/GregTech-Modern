package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.cover.data.ControllerMode;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class MachineControllerCoverUI {

    private MachineControllerCoverUI() {}

    static Widget createUIWidget(MachineControllerCover cover) {
        if (cover.getControllerMode() != null && cover.getControllable(cover.getControllerMode().side) == null) {
            cover.setControllerMode(null);
        }
        WidgetGroup group = new WidgetGroup(0, 0, 176, 95);

        group.addWidget(new LabelWidget(10, 5, "cover.machine_controller.title"));
        group.addWidget(new IntInputWidget(10, 20, 131, 20,
                cover::getMinRedstoneStrength, cover::setMinRedstoneStrength).setMin(1).setMax(15));

        cover.modeButton = new ButtonWidget(10, 45, 131, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON),
                cd -> cover.selectNextMode());
        group.addWidget((Widget) cover.modeButton);

        group.addWidget(new ToggleButtonWidget(
                146, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, cover::isInverted, cover::setInverted)
                .isMultiLang()
                .setTooltipText("cover.machine_controller.invert"));

        group.addWidget(new LabelWidget(10, 72, "cover.machine_controller.suspend_powerfail"));
        group.addWidget(new ToggleButtonWidget(147, 68, 18, 18, GuiTextures.BUTTON_POWER,
                cover::preventPowerFail, cover::setPreventPowerFail));

        cover.sideCoverSlot = new CustomItemStackHandler(1);
        group.addWidget(new PhantomSlotWidget(cover.sideCoverSlot, 0, 147, 46) {

            @Override
            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ContainerInput clickTypeIn,
                                              ItemStack stackHeld) {
                return cover.sideCoverSlot.getStackInSlot(0);
            }
        });

        updateUI(cover);

        return group;
    }

    static void updateUI(MachineControllerCover cover) {
        updateModeButton(cover);
        updateCoverSlot(cover);
    }

    private static void updateModeButton(MachineControllerCover cover) {
        if (!(cover.modeButton instanceof ButtonWidget modeButton)) {
            return;
        }

        modeButton.setButtonTexture(new GuiTextureGroup(
                GuiTextures.VANILLA_BUTTON,
                new TextTexture(cover.getControllerMode() != null ?
                        cover.getControllerMode().localeName : ControllerMode.nullLocaleName)));
    }

    private static void updateCoverSlot(MachineControllerCover cover) {
        if (cover.sideCoverSlot == null) {
            return;
        }

        if (cover.getControllerMode() == null) {
            cover.sideCoverSlot.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            var side = cover.getControllerMode().side;
            if (side == null && cover.coverHolder instanceof MachineCoverContainer coverContainer) {
                cover.sideCoverSlot.setStackInSlot(0, coverContainer.getMachine().getDefinition().asStack());
            } else {
                var attachedCover = cover.coverHolder.getCoverAtSide(side);
                cover.sideCoverSlot.setStackInSlot(0,
                        attachedCover != null ? attachedCover.getAttachItem().copy() : ItemStack.EMPTY);
            }
        }
        cover.sideCoverSlot.onContentsChanged(0);
    }
}
