package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.items.materials.StorageComponentItem;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEOutputHatchPartMachine extends MEHatchPartMachine implements IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEOutputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private KeyStorage internalBuffer; // Do not use KeyCounter, use our simple implementation

    @Getter
    @Persisted
    protected NotifiableItemStackHandler storageSlot;

    @Nullable
    protected ISubscription storageSub;

    private long capacitySize = 0;

    public MEOutputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        this.internalBuffer = new KeyStorage();
        this.storageSlot = new NotifiableItemStackHandler(this, 1, io);
        this.storageSlot.setFilter(item -> canInsertCell(item));
        return new InaccessibleInfiniteTank(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;

        storageSub = storageSlot.addChangedListener(this::updateStorageSize);
        updateStorageSize();
    }

    private boolean canInsertCell(ItemStack item) {
        var grid = getMainNode().getGrid();
        if (item.getItem() instanceof StorageComponentItem compItem) {
            long newSize = (long) compItem.getBytes(item) * 8L;
            if (newSize >= capacitySize) {
                return true;
            } else {
                return ((MEOutputHatchPartMachine.InaccessibleInfiniteTank) (tank)).getCachedAmount() >= newSize;
            }
        }
        return false;
    }

    private void updateStorageSize() {
        if (this.storageSlot.getStackInSlot(0).getItem() instanceof StorageComponentItem compItem) {
            capacitySize = (compItem.getBytes(this.storageSlot.getStackInSlot(0)) * 8L);
        } else if (this.storageSlot.getStackInSlot(0).isEmpty()) {
            capacitySize = 64L;
        }
    }

    @Override
    public void onMachineRemoved() {
        var grid = getMainNode().getGrid();
        if (grid != null && !internalBuffer.isEmpty()) {
            for (var entry : internalBuffer) {
                grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                        Actionable.MODULATE, actionSource);
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    protected boolean shouldSubscribe() {
        return super.shouldSubscribe() && !internalBuffer.storage.isEmpty();
    }

    @Override
    protected void autoIO() {
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            var grid = getMainNode().getGrid();
            if (grid != null && !internalBuffer.isEmpty()) {
                internalBuffer.insertInventory(grid.getStorageService().getInventory(), actionSource);
            }
            this.updateTankSubscription();
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override

    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 170, 65);
        // ME Network status
        group.addWidget(new LabelWidget(5, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));
        group.addWidget(new LabelWidget(5, 10, "gtceu.gui.waiting_list"));
        // display list
        group.addWidget(new SlotWidget(storageSlot.storage, 0, 140, 0));
        group.addWidget(new AEListGridWidget.Fluid(5, 20, 3, this.internalBuffer));

        return group;
    }

    private class InaccessibleInfiniteTank extends NotifiableFluidTank {

        private CustomFluidTank[] fluidStorages;

        public InaccessibleInfiniteTank(MetaMachine holder) {
            super(holder, 0, 0, IO.OUT, IO.NONE);
            internalBuffer.setOnContentsChanged(this::onContentsChanged);
        }

        @Override
        public CustomFluidTank[] getStorages() {
            if (this.fluidStorages == null) {
                this.fluidStorages = new CustomFluidTank[] { new FluidStorageDelegate() };
            }
            return this.fluidStorages;
        }

        @Override
        public int getSize() {
            return Integer.MAX_VALUE;
        }

        private long getCachedAmount() {
            long fluidAmount = 0;
            var grid = getMainNode().getGrid();
            if (grid != null && internalBuffer.isEmpty()) {
                for (var tank : internalBuffer) {
                    fluidAmount += grid.getStorageService().getInventory().getAvailableStacks()
                            .get(tank.getKey());
                }
            }
            return fluidAmount;
        }

        private boolean canInsertFluid() {
            return getCachedAmount() < capacitySize;
        }

        @Override
        public @Nullable List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe,
                                                                      List<SizedFluidIngredient> left,
                                                                      @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, this.handlerIO, getStorages());
        }

        private class FluidStorageDelegate extends CustomFluidTank {

            public FluidStorageDelegate() {
                super(0);
            }

            @Override
            public int getCapacity() {
                return Integer.MAX_VALUE;
            }

            @Override
            public void setFluid(FluidStack fluid) {
                // NO-OP
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                var key = AEFluidKey.of(resource);
                int amount = resource.getAmount();
                int oldValue = Ints.saturatedCast(internalBuffer.storage.getOrDefault(key, 0));
                int changeValue = Math.min(Integer.MAX_VALUE - oldValue, amount);
                if (canInsertFluid()) {
                    if (changeValue > 0 && action.execute()) {
                        internalBuffer.storage.put(key, oldValue + changeValue);
                        internalBuffer.onChanged();
                    }
                }
                return changeValue;
            }

            @Override
            public boolean supportsFill(int tank) {
                return false;
            }

            @Override
            public boolean supportsDrain(int tank) {
                return false;
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return false;
            }

            @Override
            public CustomFluidTank copy() {
                // because recipe testing uses copy transfer instead of simulated operations
                return new FluidStorageDelegate() {

                    @Override
                    public int fill(FluidStack resource, FluidAction action) {
                        return super.fill(resource, FluidAction.SIMULATE);
                    }
                };
            }
        }
    }
}
