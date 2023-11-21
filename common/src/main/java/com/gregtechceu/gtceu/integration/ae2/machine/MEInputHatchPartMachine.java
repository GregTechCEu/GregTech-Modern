package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MEInputHatchPartMachine extends MEHatchPartMachine implements IInWorldGridNodeHost, IGridConnectedBlockEntity {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private ExportOnlyAEFluidList aeFluidTanks;

    public MEInputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    protected NotifiableFluidTank createTank(Object... args) {
        this.aeFluidTanks = new ExportOnlyAEFluidList(this, CONFIG_SIZE, 0, IO.IN);
        return aeFluidTanks;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEFluidConfigWidget(3, 10, this.aeFluidTanks.tanks));

        return group;
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
                    long total = exceedFluid.amount();
                    long inserted = aeNetwork.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE, this.actionSource);
                    if (inserted > 0) {
                        aeTank.drain(inserted, false);
                        continue;
                    } else {
                        aeTank.drain(total, false);
                    }
                }
                // Fill it
                GenericStack reqFluid = aeTank.requestStack();
                if (reqFluid != null) {
                    long extracted = aeNetwork.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE, this.actionSource);
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
        public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ExportOnlyAEFluidList.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

        @Persisted
        private final ExportOnlyAEFluid[] tanks;

        public ExportOnlyAEFluidList(MetaMachine machine, int slots, long capacity, IO io) {
            super(machine, slots, capacity, io);
            this.tanks = new ExportOnlyAEFluid[slots];
            for (int i = 0; i < slots; i ++) {
                this.tanks[i] = new ExportOnlyAEFluid(null, null);
                this.tanks[i].setOnContentsChanged(this::onContentsChanged);
            }
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return 0;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, left, simulate, this.handlerIO, Arrays.stream(this.tanks).map(tank -> new WrappingFluidStorage(tank.getCapacity(), tank)).toArray(FluidStorage[]::new));
        }

        public FluidStack drainInternal(long maxDrain, boolean simulate) {
            if (maxDrain == 0) {
                return FluidStack.empty();
            }
            FluidStack totalDrained = null;
            for (var tank : tanks) {
                if (totalDrained == null || totalDrained.isEmpty()) {
                    totalDrained = tank.drain(maxDrain, simulate);
                    if (totalDrained.isEmpty()) {
                        totalDrained = null;
                    } else {
                        maxDrain -= totalDrained.getAmount();
                    }
                } else {
                    FluidStack copy = totalDrained.copy();
                    copy.setAmount(maxDrain);
                    FluidStack drain = tank.drain(copy, simulate);
                    totalDrained.grow(drain.getAmount());
                    maxDrain -= drain.getAmount();
                }
                if (maxDrain <= 0) break;
            }
            return totalDrained == null ? FluidStack.empty() : totalDrained;
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return Arrays.stream(tanks).map(IFluidTransfer::createSnapshot).toArray(Object[]::new);
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof Object[] array && array.length == tanks.length) {
                for (int i = 0; i < array.length; i++) {
                    tanks[i].restoreFromSnapshot(array[i]);
                }
            }
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }

        private static class WrappingFluidStorage extends FluidStorage {
            private final ExportOnlyAEFluid fluid;

            public WrappingFluidStorage(long capacity, ExportOnlyAEFluid fluid) {
                super(capacity);
                this.fluid = fluid;
            }

            public WrappingFluidStorage(long capacity, Predicate<FluidStack> validator, ExportOnlyAEFluid fluid) {
                super(capacity, validator);
                this.fluid = fluid;
            }

            @Override
            @Nonnull
            public FluidStack getFluid() {
                return this.fluid.getFluid();
            }

            @NotNull
            @Override
            public FluidStack drain(FluidStack maxDrain, boolean simulate, boolean notifyChanges) {
                return fluid.drain(maxDrain, simulate, notifyChanges);
            }

            @Override
            public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChange) {
                return fluid.fill(tank, resource, simulate, notifyChange);
            }

            @Override
            public FluidStorage copy() {
                var storage = new WrappingFluidStorage(capacity, validator, this.fluid);
                storage.setFluid(super.fluid.copy());
                return storage;
            }
        }
    }

    public static class ExportOnlyAEFluid extends ExportOnlyAESlot implements IFluidStorage, IFluidTransfer {

        public ExportOnlyAEFluid(GenericStack config, GenericStack stock) {
            super(config, stock);
            //this.setOnContentsChanged(holder::onChanged);
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
            trigger();
        }

        @Override
        public FluidStack getFluid() {
            if (this.stock != null && this.stock.what() instanceof AEFluidKey fluidKey) {
                return FluidStack.create(fluidKey.getFluid(), this.stock == null ? 0 : this.stock.amount(), fluidKey.getTag());
            }
            return FluidStack.empty();
        }

        @Override
        public void setFluid(FluidStack fluid) {

        }

        @Override
        public long getFluidAmount() {
            return this.stock != null ? this.stock.amount() : 0;
        }

        @Override
        public long getCapacity() {
            // Its capacity is always 0.
            return 0;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return false;
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @NotNull
        @Override
        public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return this.drain(resource, simulate, notifyChanges);
        }

        @Override
        public boolean supportsDrain(int tank) {
            return tank == 0;
        }

        @Override
        public long fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain, boolean notifyChanges) {
            if (this.getFluid().isFluidEqual(resource)) {
                return this.drain(resource.getAmount(), doDrain, notifyChanges);
            }
            return FluidStack.empty();
        }

        @Override
        public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
            if (this.stock == null || !(this.stock.what() instanceof AEFluidKey fluidKey)) {
                return FluidStack.empty();
            }
            int drained = (int) Math.min(this.stock.amount(), maxDrain);
            FluidStack result = FluidStack.create(fluidKey.getFluid(), drained, fluidKey.getTag());
            if (!simulate) {
                this.stock = new GenericStack(this.stock.what(), this.stock.amount() - drained);
                if (this.stock.amount() == 0) {
                    this.stock = null;
                }
                if (notifyChanges) trigger();
            }
            return result;
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return Pair.of(this.config, this.stock);
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof Pair<?,?> pair) {
                this.config = (GenericStack) pair.getFirst();
                this.stock = (GenericStack) pair.getSecond();
            }
        }

        private void trigger() {
            if (onContentsChanged != null) {
                onContentsChanged.run();
            }
        }

        @Override
        public ExportOnlyAEFluid copy() {
            return new ExportOnlyAEFluid(
                    this.config == null ? null : ExportOnlyAESlot.copy(this.config),
                    this.stock == null ? null : ExportOnlyAESlot.copy(this.stock)
            );
        }
    }
}
