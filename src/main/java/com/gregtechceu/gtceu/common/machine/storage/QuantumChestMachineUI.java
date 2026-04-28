package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.item.ItemStack;

final class QuantumChestMachineUI {

    private QuantumChestMachineUI() {}

    static Object createUIWidget(QuantumChestMachine machine) {
        var group = new WidgetGroup(0, 0, 109, 63);
        var importItems = machine.createImportItems();
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.machine.quantum_chest.items_stored"))
                .addWidget(new LabelWidget(8, 18, () -> FormattingUtil.formatNumbers(machine.storedAmount))
                        .setTextColor(-1)
                        .setDropShadow(true))
                .addWidget(new SlotWidget(importItems, 0, 87, 5, false, true)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY)))
                .addWidget(new SlotWidget(machine.cache, 0, 87, 23, false, false)
                        .setItemHook(s -> s.copyWithCount((int) Math.min(machine.storedAmount, s.getMaxStackSize())))
                        .setBackgroundTexture(GuiTextures.SLOT))
                .addWidget(new ButtonWidget(87, 42, 18, 18,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, Icons.DOWN.scale(0.7f)), cd -> {
                            if (!cd.isRemote && !machine.stored.isEmpty()) {
                                var extracted = machine.cache.extractItem(0,
                                        (int) Math.min(machine.storedAmount, machine.stored.getMaxStackSize()), false);
                                if (!group.getGui().entityPlayer.addItem(extracted)) {
                                    net.minecraft.world.level.block.Block.popResource(
                                            group.getGui().entityPlayer.level(),
                                            group.getGui().entityPlayer.getOnPos(), extracted);
                                }
                            }
                        }))
                .addWidget(new PhantomSlotWidget(machine.lockedItem, 0, 58, 41,
                        stack -> machine.stored.isEmpty() || ItemStack.isSameItemSameComponents(stack, machine.stored))
                        .setMaxStackSize(1))
                .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
                        GuiTextures.BUTTON_ITEM_OUTPUT, machine.autoOutput::isAutoOutputItems,
                        machine.autoOutput::setAllowAutoOutputItems)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_auto_output.tooltip"))
                .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
                        GuiTextures.BUTTON_LOCK, machine::isLocked, machine::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_lock.tooltip"))
                .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
                        GuiTextures.BUTTON_VOID, () -> machine.isVoiding, b -> machine.isVoiding = b)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.item_voiding_partial.tooltip"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }
}
