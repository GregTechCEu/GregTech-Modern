package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IAutoPullPart;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStockingHatchPartMachine extends MEInputHatchPartMachine implements IMEStockingPart, IAutoPullPart {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEStockingHatchPartMachine.class, MEInputHatchPartMachine.MANAGED_FIELD_HOLDER);

    private static final int CONFIG_SIZE = 16;

    @DescSynced
    @Persisted
    @Getter
    private boolean autoPull;

    private Predicate<GenericStack> autoPullTest;

    public MEStockingHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.autoPullTest = $ -> false;
    }

    @Override
    protected @NotNull NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        this.aeFluidTanks = new ExportOnlyAEStockingFluidList(this, CONFIG_SIZE);
        return this.aeFluidTanks;
    }

    @Override
    public void autoIO() {
        if (!this.isWorkingEnabled()) return;

        if (this.updateMEStatus()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEFluidSlot slot : aeFluidTanks.getInventory()) {
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
            this.updateTankSubscription();
        }

        if (!autoPull || !(getOffsetTimer() % 100 == 0)) return;
        refreshList();
    }

    @Override
    protected void flushInventory() {
        // no-op, nothing to send back to the network
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        this.autoPullTest = stack -> !this.testConfiguredInOtherPart(stack);
        if (getLevel() instanceof ServerLevel serverLevel) {
            // wait for 1 tick
            // we should not access the part list at this time
            serverLevel.getServer().tell(new TickTask(0, this::validateConfig));
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        this.autoPullTest = $ -> false;
        if (this.autoPull) {
            this.aeFluidTanks.clearInventory(0);
        }
        super.removedFromController(controller);
    }

    @Override
    public IConfigurableSlotList getSlotList() {
        return aeFluidTanks;
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        if (!isFormed()) return false;

        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof MEStockingHatchPartMachine hatch) {
                    if (hatch == this) continue;
                    if (hatch.aeFluidTanks.hasStackInConfig(config, false)) {
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
                this.aeFluidTanks.clearInventory(0);
            } else if (updateMEStatus()) {
                this.refreshList();
                updateTankSubscription();
            }
        }
    }

    private void refreshList() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            aeFluidTanks.clearInventory(0);
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
            if (!(what instanceof AEFluidKey fluidKey)) continue;

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);
            if (request == 0) continue;

            // Ensure that it is valid to configure with this stack
            if (autoPullTest != null && !autoPullTest.test(new GenericStack(fluidKey, amount))) continue;

            var slot = this.aeFluidTanks.getInventory()[index];
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
            slot.onContentsChanged();
            index++;
        }

        aeFluidTanks.clearInventory(index);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IAutoPullPart.super.attachConfigurators(configuratorPanel);
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private class ExportOnlyAEStockingFluidList extends ExportOnlyAEFluidList {

        public ExportOnlyAEStockingFluidList(MetaMachine holder, int slots) {
            super(holder, slots);
            this.inventory = new ExportOnlyAEStockingFluidSlot[slots];
            for (int i = 0; i < slots; i++) {
                this.inventory[i] = new ExportOnlyAEStockingFluidSlot();
            }
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

        public ExportOnlyAEStockingFluidSlot(GenericStack config, GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new ExportOnlyAEStockingFluidSlot(
                    this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock));
        }

        @Override
        public @NotNull FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
            if (this.stock != null && this.config != null) {
                // Extract the items from the real net to either validate (simulate)
                // or extract (modulate) when this is called
                if (!isOnline()) return FluidStack.empty();
                MEStorage aeNetwork = getMainNode().getGrid().getStorageService().getInventory();

                Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                var key = config.what();
                long extracted = aeNetwork.extract(key, maxDrain, action, actionSource);

                if (extracted > 0) {
                    FluidStack resultStack = this.stock.what() instanceof AEFluidKey fluidKey ?
                            AEUtil.toFluidStack(fluidKey, extracted) : FluidStack.empty();
                    // may as well update the display here
                    this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                    if (this.stock.amount() == 0) {
                        this.stock = null;
                    }
                    if (notifyChanges && this.onContentsChanged != null) {
                        this.onContentsChanged.run();
                    }
                    return resultStack;
                }
            }
            return FluidStack.empty();
        }
    }
}
