package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.*;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStockingBusPartMachine extends MEInputBusPartMachine implements IMEStockingPart {

    @SyncToClient
    @SaveField
    @Getter
    private boolean autoPull;

    @Getter
    @Setter
    @SaveField
    private int minStackSize = 1;
    @Getter
    @SaveField
    private int ticksPerCycle = 40;

    @Setter
    private Predicate<GenericStack> autoPullTest;

    public MEStockingBusPartMachine(BlockEntityCreationInfo info) {
        super(info, new ExportOnlyAEStockingItemList(CONFIG_SIZE));
        this.autoPullTest = $ -> false;
        setOffsetBound(ticksPerCycle);
        this.aeItemHandler = (ExportOnlyAEItemList) getInventory();
        ((ExportOnlyAEStockingItemList) getInventory()).setStockingBusPartMachine(this);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void addedToController(MultiblockControllerMachine controller, String name) {
        super.addedToController(controller, name);
        IMEStockingPart.super.addedToController(controller, name);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        IMEStockingPart.super.removedFromController(controller);
        super.removedFromController(controller);
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        if (!isWorkingEnabled()) {
            return;
        }
        if (!shouldSyncME()) {
            return;
        }
        if (ticksPerCycle == 0) {
            ticksPerCycle = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;
        }
        if (updateMEStatus()) {
            updateInventorySubscription();
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        boolean wasOnline = isOnline();
        super.onMainNodeStateChanged(reason);
        if (isOnline() == wasOnline) {
            return;
        }
        if (isOnline()) {
            if (isAutoPull()) {
                markForAutoPull();
            }
            markForRefresh();
        } else {
            if (isAutoPull()) {
                getSlotList().clearInventory(0);
            } else {
                for (int i = 0; i < getSlotList().getConfigurableSlots(); i++) {
                    IConfigurableSlot slot = getSlotList().getConfigurableSlot(i);
                    if (slot == null) {
                        continue;
                    }
                    slot.setStock(null);
                }
            }
        }
    }

    /*
     * @Override
     * public void attachSideTabs(TabsWidget sideTabs) {
     * sideTabs.setMainTab(this); // removes the cover configurator, it's pointless and clashes with layout.
     * }
     */

    @Override
    protected void flushInventory() {
        // no-op, nothing to send back to the network
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        super.setDistinct(isDistinct);
        if (!isRemote() && !isDistinct) {
            // Ensure that our configured items won't match any other buses in the multiblock.
            // Needed since we allow duplicates in distinct mode on, but not off
            validateConfig();
        }
    }

    @Override
    public void onPaintingColorChanged(int color) {
        super.onPaintingColorChanged(color);
        if (!isRemote()) {
            validateConfig();
        }
    }

    @Override
    public IConfigurableSlotList getSlotList() {
        return aeItemHandler;
    }

    @Override
    public IActionSource getActionSource() {
        return actionSource;
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        // In distinct mode, we don't need to check other buses since only one bus can run a recipe at a time.
        if (!isFormed() || isDistinct()) return false;

        // Otherwise, we need to test for if the item is configured
        // in any stocking bus in the multi (besides ourselves).
        for (MultiblockControllerMachine controller : getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part instanceof MEStockingBusPartMachine bus) {
                    if (bus == this || bus.isDistinct() || bus.getPaintingColor() != this.getPaintingColor()) {
                        continue;
                    }
                    if (bus.aeItemHandler.hasStackInConfig(config, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("autoPull");
            if (!this.autoPull) {
                this.aeItemHandler.clearInventory(0);
            } else if (updateMEStatus()) {
                markForAutoPull();
                updateInventorySubscription();
            }
        }
    }

    public void setTicksPerCycle(int ticksPerCycle) {
        this.ticksPerCycle = ticksPerCycle;
        setOffsetBound(ticksPerCycle);
    }

    @Override
    public boolean isAutoPullValid(AEKey what, long amount) {
        return what instanceof AEItemKey && autoPullTest.test(new GenericStack(what, amount));
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            setAutoPull(!autoPull);
            if (autoPull) {
                context.getPlayer().sendSystemMessage(
                        Component.translatable("gtceu.machine.me.stocking_auto_pull_enabled"));
            } else {
                context.getPlayer().sendSystemMessage(
                        Component.translatable("gtceu.machine.me.stocking_auto_pull_disabled"));
            }
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

    @Override
    protected CompoundTag writeConfigToTag() {
        if (!autoPull) {
            CompoundTag tag = super.writeConfigToTag();
            tag.putBoolean("AutoPull", false);
            return tag;
        }
        // if in auto-pull, no need to write actual configured slots, but still need to write the ghost circuit
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("AutoPull", true);
        tag.putByte("GhostCircuit",
                (byte) circuitSlot.getCurrentCircuit());
        return tag;
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.getBoolean("AutoPull")) {
            // if being set to auto-pull, no need to read the configured slots
            this.setAutoPull(true);
            circuitSlot.setCurrentCircuit(tag.getByte("GhostCircuit"));
            return;
        }
        // set auto pull first to avoid issues with clearing the config after reading from the data stick
        this.setAutoPull(false);
        super.readConfigFromTag(tag);
    }

    private static class ExportOnlyAEStockingItemList extends ExportOnlyAEItemList {

        @Getter
        @Nullable
        MEStockingBusPartMachine stockingBusPartMachine;

        public ExportOnlyAEStockingItemList(int slots) {
            super(slots, ExportOnlyAEStockingItemSlot::new);
        }

        public void setStockingBusPartMachine(@Nullable MEStockingBusPartMachine stockingBusPartMachine) {
            this.stockingBusPartMachine = stockingBusPartMachine;
            for (var slot : inventory) {
                ((ExportOnlyAEStockingItemSlot) (slot)).setStockingBusPartMachine(stockingBusPartMachine);
            }
        }

        @Override
        public boolean isAutoPull() {
            return Objects.requireNonNull(getStockingBusPartMachine()).isAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            boolean inThisBus = super.hasStackInConfig(stack, false);
            if (inThisBus) return true;
            if (checkExternal) {
                return Objects.requireNonNull(getStockingBusPartMachine()).testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private static class ExportOnlyAEStockingItemSlot extends ExportOnlyAEItemSlot {

        @Getter
        @Setter
        @Nullable
        MEStockingBusPartMachine stockingBusPartMachine;

        public ExportOnlyAEStockingItemSlot() {
            super();
        }

        public ExportOnlyAEStockingItemSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public void setConfig(@Nullable GenericStack val) {
            GenericStack oldConfig = getConfig();
            boolean changed = !Objects.equals(oldConfig, val);
            super.setConfig(val);
            if (changed && stockingBusPartMachine != null) {
                stockingBusPartMachine.markForRefresh();
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && this.stock != null && stockingBusPartMachine != null) {
                if (this.config != null) {
                    // Extract the items from the real net to either validate (simulate)
                    // or extract (modulate) when this is called
                    if (!stockingBusPartMachine.isOnline()) return ItemStack.EMPTY;
                    MEStorage aeNetwork = stockingBusPartMachine.getMainNode().getGrid().getStorageService()
                            .getInventory();

                    Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                    var key = config.what();
                    long extracted = aeNetwork.extract(key, amount, action, stockingBusPartMachine.actionSource);

                    if (extracted > 0) {
                        ItemStack resultStack = key instanceof AEItemKey itemKey ?
                                itemKey.toStack((int) extracted) : ItemStack.EMPTY;
                        if (!simulate) {
                            // may as well update the display here
                            this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                            if (this.stock.amount() == 0) {
                                this.stock = null;
                            }
                            if (this.onContentsChanged != null) {
                                this.onContentsChanged.run();
                            }
                        }
                        return resultStack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ExportOnlyAEStockingItemSlot copy() {
            return new ExportOnlyAEStockingItemSlot(
                    this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock));
        }
    }
}
