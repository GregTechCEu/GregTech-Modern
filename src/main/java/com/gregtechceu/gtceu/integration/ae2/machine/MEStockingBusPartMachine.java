package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStockingBusPartMachine extends MEInputBusPartMachine implements IMEStockingPart {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEStockingBusPartMachine.class, MEInputBusPartMachine.MANAGED_FIELD_HOLDER);

    @DescSynced
    @Persisted
    @Getter
    private boolean autoPull;

    @Setter
    private Predicate<GenericStack> autoPullTest;

    public MEStockingBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.autoPullTest = $ -> false;
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        IMEStockingPart.super.addedToController(controller);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        IMEStockingPart.super.removedFromController(controller);
        super.removedFromController(controller);
    }

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyAEStockingItemList(this, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (this.updateMEStatus()) {
            // Update the visual display for the fake items. This also is important for the item handler's
            // getStackInSlot() method, as it uses the cached items set here.
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEItemSlot slot : this.aeItemHandler.getInventory()) {
                var config = slot.getConfig();
                if (config == null) {
                    slot.setStock(null);
                } else {
                    // Try to fill the slot
                    var key = config.what();
                    long result = aeNetwork.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                    slot.setStock(new GenericStack(key, result));
                    slot.onContentsChanged();
                }
            }

            this.updateInventorySubscription();
        }
        if (!autoPull || !(getOffsetTimer() % 100 == 0)) return;
        refreshList();
    }

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
    public IConfigurableSlotList getSlotList() {
        return aeItemHandler;
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        // In distinct mode, we don't need to check other buses since only one bus can run a recipe at a time.
        if (!isFormed() || isDistinct()) return false;

        // Otherwise, we need to test for if the item is configured
        // in any stocking bus in the multi (besides ourselves).
        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof MEStockingBusPartMachine bus) {
                    // We don't need to check for ourselves, as this case is handled elsewhere.
                    if (bus == this || bus.isDistinct()) continue;
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
            if (!this.autoPull) {
                this.aeItemHandler.clearInventory(0);
            } else if (updateMEStatus()) {
                this.refreshList();
                updateInventorySubscription();
            }
        }
    }

    /**
     * Refresh the configuration list in auto-pull mode.
     * Sets the config to the first 16 valid items found in the network.
     */
    private void refreshList() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            aeItemHandler.clearInventory(0);
            return;
        }

        MEStorage networkStorage = grid.getStorageService().getInventory();
        var counter = networkStorage.getAvailableStacks();
        int index = 0;
        for (Object2LongMap.Entry<AEKey> entry : counter) {
            if (index >= CONFIG_SIZE) break;
            AEKey what = entry.getKey();
            long amount = entry.getLongValue();

            if (amount <= 0) continue;
            if (!(what instanceof AEItemKey itemKey)) continue;

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);
            if (request == 0) continue;

            // Ensure that it is valid to configure with this stack
            if (autoPullTest != null && !autoPullTest.test(new GenericStack(itemKey, amount))) continue;

            var slot = this.aeItemHandler.getInventory()[index];
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
            slot.onContentsChanged();
            index++;
        }

        aeItemHandler.clearInventory(index);
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IMEStockingPart.super.attachConfigurators(configuratorPanel);
        super.attachConfigurators(configuratorPanel);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            setAutoPull(!autoPull);
            if (autoPull) {
                playerIn.sendSystemMessage(
                        Component.translatable("gtceu.machine.me.stocking_auto_pull_enabled"));
            } else {
                playerIn.sendSystemMessage(
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
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0)));
        return tag;
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.getBoolean("AutoPull")) {
            // if being set to auto-pull, no need to read the configured slots
            this.setAutoPull(true);
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
            return;
        }
        // set auto pull first to avoid issues with clearing the config after reading from the data stick
        this.setAutoPull(false);
        super.readConfigFromTag(tag);
    }

    private class ExportOnlyAEStockingItemList extends ExportOnlyAEItemList {

        public ExportOnlyAEStockingItemList(MetaMachine holder, int slots) {
            super(holder, slots, ExportOnlyAEStockingItemSlot::new);
        }

        @Override
        public boolean isAutoPull() {
            return autoPull;
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
                return testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private class ExportOnlyAEStockingItemSlot extends ExportOnlyAEItemSlot {

        public ExportOnlyAEStockingItemSlot() {
            super();
        }

        public ExportOnlyAEStockingItemSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            if (slot == 0 && this.stock != null) {
                if (this.config != null) {
                    // Extract the items from the real net to either validate (simulate)
                    // or extract (modulate) when this is called
                    if (!isOnline()) return ItemStack.EMPTY;
                    MEStorage aeNetwork = getMainNode().getGrid().getStorageService().getInventory();

                    Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                    var key = config.what();
                    long extracted = aeNetwork.extract(key, amount, action, actionSource);

                    if (extracted > 0) {
                        ItemStack resultStack = key instanceof AEItemKey itemKey ?
                                itemKey.toStack((int) extracted) : ItemStack.EMPTY;
                        if (!simulate) {
                            // may as well update the display here
                            this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                            if (this.stock.amount() == 0) {
                                this.stock = null;
                            }
                            if (notifyChanges && this.onContentsChanged != null) {
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
