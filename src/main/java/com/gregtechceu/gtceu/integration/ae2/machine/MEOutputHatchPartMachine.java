package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidGridWidget;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableGenericStackInv;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.IGridConnectedBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class MEOutputHatchPartMachine extends MEHatchPartMachine
                                      implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEOutputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private SerializableGenericStackInv internalBuffer;

    public MEOutputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    @NotNull
    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        this.internalBuffer = new SerializableGenericStackInv(this::onChanged, slots);
        return new InaccessibleInfiniteSlot(this, this.internalBuffer);
    }

    @Override
    @NotNull
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(10, 15, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEFluidGridWidget(16, 25, 3, this.internalBuffer));

        return group;
    }

    @Override
    protected void autoIO() {
        if (getLevel().isClientSide) return;
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            if (!this.internalBuffer.isEmpty()) {
                MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
                for (int slot = 0; slot < this.internalBuffer.size(); ++slot) {
                    GenericStack item = this.internalBuffer.getStack(slot);
                    if (item == null) continue;
                    long inserted = aeNetwork.insert(item.what(), item.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (inserted > 0) {
                        item = new GenericStack(item.what(), (item.amount() - inserted));
                    }
                    this.internalBuffer.setStack(slot, item);
                }
            }
            this.updateTankSubscription();
        }
    }

    @Override
    protected void updateTankSubscription() {
        if (isWorkingEnabled() && !internalBuffer.isEmpty() && this.getLevel() != null &&
                GridHelper.getNodeHost(getLevel(), getPos().relative(getFrontFacing())) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static class InaccessibleInfiniteSlot extends NotifiableFluidTank {

        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                InaccessibleInfiniteSlot.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

        private final GenericStackInv internalBuffer;

        public InaccessibleInfiniteSlot(MetaMachine holder, GenericStackInv internalBuffer) {
            super(holder, internalBuffer.size(), 0, IO.OUT);
            this.internalBuffer = internalBuffer;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, this.handlerIO,
                    Stream.generate(() -> new FluidStorage(0) {

                        @Override
                        public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
                            return InaccessibleInfiniteSlot.this.fill(resource, simulate, notifyChanges);
                        }
                    }).limit(this.internalBuffer.size()).toArray(FluidStorage[]::new));
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
