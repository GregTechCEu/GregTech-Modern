package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidGridWidget;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableGenericStackInv;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.security.IActionSource;
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
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
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

    private static class InaccessibleInfiniteSlot extends NotifiableFluidTank implements IItemHandlerModifiable {

        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                InaccessibleInfiniteSlot.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

        private final GenericStackInv internalBuffer;

        public InaccessibleInfiniteSlot(MetaMachine holder, GenericStackInv internalBuffer) {
            super(holder, internalBuffer.size(), 0, IO.OUT);
            this.internalBuffer = internalBuffer;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            GenericStack stack1 = GenericStack.fromItemStack(stack);
            this.internalBuffer.insert(slot, stack1.what(), stack1.amount(), Actionable.MODULATE);
            this.machine.onChanged();
        }

        @Override
        public List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedFluidIngredient> left,
                                                            @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, this.handlerIO,
                    Stream.generate(() -> new CustomFluidTank(0) {

                        @Override
                        public int fill(FluidStack resource, FluidAction action) {
                            return InaccessibleInfiniteSlot.this.fill(resource, action);
                        }
                    }).limit(this.internalBuffer.size()).toArray(CustomFluidTank[]::new));
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                GenericStack stack1 = GenericStack.fromItemStack(stack);
                this.internalBuffer.insert(stack1.what(), stack1.amount(), Actionable.MODULATE,
                        this.machine instanceof MEBusPartMachine host ? host.actionSource : IActionSource.empty());
                this.machine.onChanged();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE - 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
