package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MEInputHatchPartMachine extends MEHatchPartMachine
                                     implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEInputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private ExportOnlyAEFluidList aeFluidTanks;

    public MEInputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    @NotNull
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        this.aeFluidTanks = new ExportOnlyAEFluidList(this, slots, 0, IO.IN);
        return aeFluidTanks;
    }

    @Override
    @NotNull
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEFluidConfigWidget(3, 0, this.aeFluidTanks.tanks));

        return group;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tankSubs = this.aeFluidTanks.addChangedListener(this::updateTankSubscription);
    }

    @Override
    protected void autoIO() {
        if (getLevel().isClientSide) return;
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEFluid aeTank : this.aeFluidTanks.tanks) {
                // Try to clear the wrong fluid
                GenericStack exceedFluid = aeTank.exceedStack();
                if (exceedFluid != null) {
                    int total = (int) exceedFluid.amount();
                    int inserted = (int) aeNetwork.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE, this.actionSource);
                    if (inserted > 0) {
                        aeTank.drain(inserted, IFluidHandler.FluidAction.EXECUTE);
                        continue;
                    } else {
                        aeTank.drain(total, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                // Fill it
                GenericStack reqFluid = aeTank.requestStack();
                if (reqFluid != null) {
                    long extracted = aeNetwork.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (extracted > 0) {
                        aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                    }
                }
            }
            this.updateTankSubscription();
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public static class ExportOnlyAEFluidList extends NotifiableFluidTank {

        public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                ExportOnlyAEFluidList.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

        @Persisted
        private final ExportOnlyAEFluid[] tanks;
        private CustomFluidTank[] fluidStorages;

        public ExportOnlyAEFluidList(MetaMachine machine, int slots, int capacity, IO io) {
            super(machine, slots, capacity, io);
            this.tanks = new ExportOnlyAEFluid[slots];
            for (int i = 0; i < slots; i++) {
                this.tanks[i] = new ExportOnlyAEFluid(null, null);
                this.tanks[i].setOnContentsChanged(this::onContentsChanged);
            }
            this.fluidStorages = null;
        }

        @Override
        public CustomFluidTank[] getStorages() {
            if(this.fluidStorages == null) {
                this.fluidStorages = Arrays.stream(this.tanks).map(tank -> new WrappingFluidStorage(tank.getCapacity(), tank)).toArray(CustomFluidTank[]::new);
                return this.fluidStorages;
            } else {
                return this.fluidStorages;
            }
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, this.handlerIO, getStorages());
        }

        public FluidStack drainInternal(int maxDrain, FluidAction action) {
            if (maxDrain == 0) {
                return FluidStack.EMPTY;
            }
            FluidStack totalDrained = null;
            for (var tank : tanks) {
                if (totalDrained == null || totalDrained.isEmpty()) {
                    totalDrained = tank.drain(maxDrain, action);
                    if (totalDrained.isEmpty()) {
                        totalDrained = null;
                    } else {
                        maxDrain -= totalDrained.getAmount();
                    }
                } else {
                    FluidStack copy = totalDrained.copy();
                    copy.setAmount(maxDrain);
                    FluidStack drain = tank.drain(copy, action);
                    totalDrained.grow(drain.getAmount());
                    maxDrain -= drain.getAmount();
                }
                if (maxDrain <= 0) break;
            }
            return totalDrained == null ? FluidStack.EMPTY : totalDrained;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }

        private static class WrappingFluidStorage extends CustomFluidTank {
            private final ExportOnlyAEFluid fluid;

            public WrappingFluidStorage(int capacity, ExportOnlyAEFluid fluid) {
                super(capacity);
                this.fluid = fluid;
            }

            public WrappingFluidStorage(int capacity, Predicate<FluidStack> validator, ExportOnlyAEFluid fluid) {
                super(capacity, validator);
                this.fluid = fluid;
            }

            @Override
            @NotNull
            public FluidStack getFluid() {
                return this.fluid.getFluid();
            }

            @NotNull
            @Override
            public FluidStack drain(FluidStack maxDrain, FluidAction action) {
                return fluid.drain(maxDrain, action);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return fluid.fill(resource, action);
            }

            @Override
            public CustomFluidTank copy() {
                var storage = new WrappingFluidStorage(capacity, validator, this.fluid);
                storage.setFluid(super.fluid.copy());
                return storage;
            }
        }
    }

    public static class ExportOnlyAEFluid extends ExportOnlyAESlot implements IFluidHandler, IFluidTank {

        public ExportOnlyAEFluid(GenericStack config, GenericStack stock) {
            super(config, stock);
        }

        public ExportOnlyAEFluid() {
            super();
        }

        @Override
        public void addStack(GenericStack stack) {
            if (this.stock == null) {
                this.stock = stack;
            } else {
                this.stock = GenericStack.sum(this.stock, stack);
            }
            onContentsChanged();
        }

        @Override
        @NotNull
        public FluidStack getFluid() {
            if (this.stock != null && this.stock.what() instanceof AEFluidKey fluidKey) {
                // TODO fix nbt once AE2 1.20.5 is out
                return new FluidStack(fluidKey.getFluid(), this.stock == null ? 0 : (int) this.stock.amount()/*, fluidKey.getTag()*/);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getFluidAmount() {
            return this.stock != null ? (int) this.stock.amount() : 0;
        }

        @Override
        public int getCapacity() {
            // Its capacity is always 0.
            return 0;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return false;
        }

        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (FluidStack.isSameFluidSameComponents(this.getFluid(), resource)) {
                return this.drain(resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        @NotNull
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (this.stock == null || !(this.stock.what() instanceof AEFluidKey fluidKey)) {
                return FluidStack.EMPTY;
            }
            int drained = (int) Math.min(this.stock.amount(), maxDrain);
            // TODO fix nbt once AE2 1.20.5 is out
            FluidStack result = new FluidStack(fluidKey.getFluid(), drained/*, fluidKey.getTag()*/);
            if (action == FluidAction.EXECUTE) {
                this.stock = new GenericStack(this.stock.what(), this.stock.amount() - drained);
                if (this.stock.amount() == 0) {
                    this.stock = null;
                }
                onContentsChanged();
            }
            return result;
        }


        //@Override
        public void onContentsChanged() {
            if (onContentsChanged != null) {
                onContentsChanged.run();
            }
        }

        @Override
        public ExportOnlyAEFluid copy() {
            return new ExportOnlyAEFluid(
                    this.config == null ? null : ExportOnlyAESlot.copy(this.config),
                    this.stock == null ? null : ExportOnlyAESlot.copy(this.stock));
        }
    }
}
