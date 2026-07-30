package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.integration.ae2.gridservice.IStockingService;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
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
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface IMEStockingPart extends IAutoPullPart, IMuiMachine {

    default void addedToController(MultiblockControllerMachine controller, String name) {
        // ensure that no other stocking bus on this multiblock is configured to hold the same item.
        // that we have in our own bus.
        setAutoPullTest(stack -> !this.testConfiguredInOtherPart(stack));
        // also ensure that our current config is valid given other inputs
        // wait for 1 tick
        // we should not access the part list at this time
        controller.scheduleForNextServerTick(this::validateConfig);
    }

    default void removedFromController(MultiblockControllerMachine controller) {
        setAutoPullTest($ -> false);
        if (isAutoPull()) {
            getSlotList().clearInventory(0);
        }
    }

    IConfigurableSlotList getSlotList();

    IManagedGridNode getMainNode();

    IActionSource getActionSource();

    boolean isOnline();

    default Set<AEKey> getStockingKeys() {
        Set<AEKey> keys = new HashSet<>();
        IConfigurableSlotList slots = getSlotList();
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            GenericStack config = slots.getConfigurableSlot(i).getConfig();
            // Don't trust IntelliJ here, it surely CAN be null
            if (config != null) {
                keys.add(config.what());
            }
        }
        return keys;
    }

    default boolean applyStockSilent(Object2LongMap<AEKey> changed) {
        IConfigurableSlotList slots = getSlotList();
        int min = getMinStackSize();
        boolean anyChanged = false;
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            IConfigurableSlot slot = slots.getConfigurableSlot(i);
            GenericStack config = slot.getConfig();
            // Don't trust IntelliJ here, it surely CAN be null
            if (config == null) {
                continue;
            }
            AEKey key = config.what();
            if (!changed.containsKey(key)) {
                continue;
            }
            long amount = changed.getLong(key);
            GenericStack newStock;
            if (amount >= min) {
                newStock = new GenericStack(key, amount);
            } else {
                newStock = null;
            }
            if (slot.setStockSilent(newStock)) {
                anyChanged = true;
            }
        }
        return anyChanged;
    }

    default void notifyStockChanged() {
        getSlotList().onContentsChanged();
    }

    default boolean isCycleDue() {
        int interval = getTicksPerCycle();
        if (interval <= 0) {
            interval = 40;
        }
        return self().getOffsetTimer() % interval == 0;
    }

    boolean isAutoPullValid(AEKey what, long amount);

    default void applyAutoPull(List<GenericStack> selection) {
        IConfigurableSlotList slots = getSlotList();
        int slotCount = slots.getConfigurableSlots();
        int filled = 0;
        boolean changed = false;
        for (GenericStack stack : selection) {
            if (filled >= slotCount) {
                break;
            }
            IConfigurableSlot slot = slots.getConfigurableSlot(filled++);
            GenericStack newConfig = new GenericStack(stack.what(), 1);
            if (!newConfig.equals(slot.getConfig())) {
                changed = true;
            }
            slot.setConfig(newConfig);
            if (slot.setStockSilent(stack)) {
                changed = true;
            }
        }
        slots.clearInventory(filled);
        if (changed) {
            slots.onContentsChanged();
        }
    }

    default void markForRefresh() {
        if (self().isRemote()) {
            return;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid != null) {
            grid.getService(IStockingService.class).markForRefresh(this);
        }
    }

    default void markForAutoPull() {
        if (self().isRemote()) {
            return;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid != null) {
            grid.getService(IStockingService.class).markForAutoPull(this);
        }
    }

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
                                .child(Text.lang("gtceu.gui.me_network.min_stack_size").asWidget())
                                .child(new TextFieldWidget()
                                        .size(120, 18)
                                        .value(SyncHandlers.intNumber(this::getMinStackSize, this::setMinStackSize))
                                        .setNumbers(1, Integer.MAX_VALUE))
                                .child(Text.lang("gtceu.gui.me_network.ticks_per_cycle").asWidget())
                                .child(new TextFieldWidget()
                                        .size(120, 18)
                                        .value(SyncHandlers.intNumber(this::getTicksPerCycle, this::setTicksPerCycle))
                                        .setNumbers(1, 200))
                                .margin(5)));

        return MachineUIPanelBuilder.panelBuilder(this.self())
                .rightConfigurators(f -> {
                    f.child(new ToggleButton()
                            .value(new BoolValue.Dynamic(this::isAutoPull, this::setAutoPull))
                            .stateOverlay(GTGuiTextures.BUTTON_AUTO_PULL)
                            .tooltipAutoUpdate(true)
                            .tooltipBuilder(r -> r
                                    .addLine(Text.lang("gtceu.gui.me_network.auto_pull_toggle"))))
                            .child(new ButtonWidget<>()
                                    .size(18)
                                    .onMousePressed((context, b) -> {
                                        settingsPanelHandler.openPanel();
                                        return true;
                                    })
                                    .overlay(new ItemDrawable(GTItems.TOOL_DATA_STICK.asItem()).asIcon().size(16))
                                    .tooltip(new RichTooltip()
                                            .addLine(Text.lang("gtceu.gui.me_network.stocking_settings"))));

                });
    }
}
