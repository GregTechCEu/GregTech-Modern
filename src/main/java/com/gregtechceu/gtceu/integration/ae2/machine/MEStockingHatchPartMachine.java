package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.stacks.AEItemKey;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.*;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
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
public class MEStockingHatchPartMachine extends MEInputHatchPartMachine implements IMEStockingPart {

    private static final int CONFIG_SIZE = 16;

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

    public MEStockingHatchPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.autoPullTest = $ -> false;
        setOffsetBound(ticksPerCycle);
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

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        this.aeFluidHandler = new ExportOnlyAEStockingFluidList(this, CONFIG_SIZE);
        return this.aeFluidHandler;
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
            updateTankSubscription();
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

    @Override
    public void onPaintingColorChanged(int color) {
        super.onPaintingColorChanged(color);
        if (!isRemote()) {
            validateConfig();
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
    public IConfigurableSlotList getSlotList() {
        return aeFluidHandler;
    }

    @Override
    public IActionSource getActionSource() {
        return actionSource;
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        if (!isFormed()) return false;

        for (MultiblockControllerMachine controller : getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part instanceof MEStockingHatchPartMachine hatch) {
                    if (hatch == this || hatch.getPaintingColor() != this.getPaintingColor()) {
                        continue;
                    }
                    if (hatch.aeFluidHandler.hasStackInConfig(config, false)) {
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
                this.aeFluidHandler.clearInventory(0);
            } else if (updateMEStatus()) {
                markForAutoPull();
                updateTankSubscription();
            }
        }
    }

    public void setTicksPerCycle(int ticksPerCycle) {
        this.ticksPerCycle = ticksPerCycle;
        setOffsetBound(ticksPerCycle);
    }

    @Override
    public boolean isAutoPullValid(AEKey what, long amount) {
        return what instanceof AEFluidKey && autoPullTest.test(new GenericStack(what, amount));
    }

    ////////////////////////////////
    // ******* Interaction *******//
    ////////////////////////////////

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

    private class ExportOnlyAEStockingFluidList extends ExportOnlyAEFluidList {

        public ExportOnlyAEStockingFluidList(MetaMachine holder, int slots) {
            super(holder, slots, ExportOnlyAEStockingFluidSlot::new);
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
            boolean inThisHatch = super.hasStackInConfig(stack, false);
            if (inThisHatch) return true;
            if (checkExternal) {
                return testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private class ExportOnlyAEStockingFluidSlot extends ExportOnlyAEFluidSlot {

        public ExportOnlyAEStockingFluidSlot() {
            super();
        }

        public ExportOnlyAEStockingFluidSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public void setConfig(@Nullable GenericStack val) {
            GenericStack oldConfig = getConfig();
            boolean changed = !Objects.equals(oldConfig, val);
            super.setConfig(val);
            if (changed) {
                markForRefresh();
            }
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new ExportOnlyAEStockingFluidSlot(
                    this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock));
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (this.stock != null && this.config != null) {
                // Extract the items from the real net to either validate (simulate)
                // or extract (modulate) when this is called
                if (!isOnline()) return FluidStack.EMPTY;
                MEStorage aeNetwork = getMainNode().getGrid().getStorageService().getInventory();

                Actionable actionable = action.simulate() ? Actionable.SIMULATE : Actionable.MODULATE;
                var key = config.what();
                long extracted = aeNetwork.extract(key, maxDrain, actionable, actionSource);

                if (extracted > 0) {
                    FluidStack resultStack = key instanceof AEFluidKey fluidKey ?
                            AEUtil.toFluidStack(fluidKey, extracted) : FluidStack.EMPTY;
                    if (action.execute()) {
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
            return FluidStack.EMPTY;
        }
    }
}
