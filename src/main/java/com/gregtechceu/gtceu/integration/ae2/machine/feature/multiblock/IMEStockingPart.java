package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.api.stacks.GenericStack;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IKey;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

public interface IMEStockingPart extends IAutoPullPart {

    @Override
    default void addedToController(MultiblockControllerMachine controller) {
        // ensure that no other stocking bus on this multiblock is configured to hold the same item.
        // that we have in our own bus.
        setAutoPullTest(stack -> !this.testConfiguredInOtherPart(stack));
        // also ensure that our current config is valid given other inputs
        if (self().getLevel() instanceof ServerLevel serverLevel) {
            // wait for 1 tick
            // we should not access the part list at this time
            serverLevel.getServer().tell(new TickTask(0, this::validateConfig));
        }
    }

    @Override
    default void removedFromController(MultiblockControllerMachine controller) {
        setAutoPullTest($ -> false);
        if (isAutoPull()) {
            getSlotList().clearInventory(0);
        }
    }

    IConfigurableSlotList getSlotList();

    /**
     * @return True if the passed stack is found as a configuration in any other stocking buses on the multiblock.
     */
    boolean testConfiguredInOtherPart(@Nullable GenericStack config);

    /**
     * Test for if any of our configured items are in another stocking bus on the multi
     * we are attached to. Prevents dupes in certain situations.
     */
    default void validateConfig() {
        var slots = getSlotList();
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            var slot = slots.getConfigurableSlot(i);
            if (slot.getConfig() != null) {
                GenericStack configuredStack = slot.getConfig();
                if (testConfiguredInOtherPart(configuredStack)) {
                    slot.setConfig(null);
                    slot.setStock(null);
                }
            }
        }
    }

    int getMinStackSize();

    void setMinStackSize(int newSize);

    int getTicksPerCycle();

    void setTicksPerCycle(int newSize);

    @Override
    default MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IPanelHandler settingsPanelHandler = syncManager.syncedPanel("stocking_settings", true,
                (sm, sh) -> PopupPanel.createPopupPanel("stocking_settings_panel", 140, 70)
                        .child(Flow.col()
                                .coverChildren()
                                .child(IKey.lang("gtceu.gui.me_network.min_stack_size").asWidget())
                                .child(new TextFieldWidget()
                                        .size(120, 18)
                                        .value(SyncHandlers.intNumber(this::getMinStackSize, this::setMinStackSize))
                                        .setNumbers(1, Integer.MAX_VALUE))
                                .child(IKey.lang("gtceu.gui.me_network.ticks_per_cycle").asWidget())
                                .child(new TextFieldWidget()
                                        .size(120, 18)
                                        .value(SyncHandlers.intNumber(this::getTicksPerCycle, this::setTicksPerCycle))
                                        .setNumbers(1, 200))
                                .margin(5)));

        return MachineUIPanelBuilder.defaultPanelBuilder(this.self())
                .rightConfigurators(f -> {
                    f.child(new ToggleButton()
                            .value(new BoolValue.Dynamic(this::isAutoPull, this::setAutoPull))
                            .stateOverlay(GTGuiTextures.BUTTON_AUTO_PULL)
                            .tooltipAutoUpdate(true)
                            .tooltipBuilder(r -> r
                                    .addLine(IKey.lang("gtceu.gui.me_network.auto_pull_toggle"))))
                            .child(new ButtonWidget<>()
                                    .size(18)
                                    .onMousePressed((x, y, b) -> {
                                        settingsPanelHandler.openPanel();
                                        return true;
                                    })
                                    .overlay(new ItemDrawable(GTItems.TOOL_DATA_STICK.asItem()).asIcon().size(16))
                                    .tooltip(new RichTooltip()
                                            .addLine(IKey.lang("gtceu.gui.me_network.stocking_settings"))));

                });
    }
}
