package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.items.materials.StorageComponentItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @Author GlodBlock
 * @Description The Output Bus that can directly send its contents to ME storage network.
 * @Date 2023/4/19-20:37
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEOutputBusPartMachine extends MEBusPartMachine implements IMachineLife, IInteractedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEOutputBusPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private KeyStorage internalBuffer; // Do not use KeyCounter, use our simple implementation
    @Getter
    @Persisted
    protected NotifiableItemStackHandler storageSlot;

    @Nullable
    protected ISubscription storageSub;

    private long capacitySize = 0;

    public MEOutputBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.OUT, args);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.internalBuffer = new KeyStorage();
        this.storageSlot = new NotifiableItemStackHandler(this, 1, io);
        this.storageSlot.setFilter(item -> canInsertCell(item));
        return new InaccessibleInfiniteHandler(this);
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
                return ((InaccessibleInfiniteHandler) (getInventory())).getCachedAmount() >= newSize;
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
    public void autoIO() {
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            var grid = getMainNode().getGrid();
            if (grid != null && !internalBuffer.isEmpty()) {
                internalBuffer.insertInventory(grid.getStorageService().getInventory(), actionSource);
            }
            this.updateInventorySubscription();
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
        group.addWidget(new AEListGridWidget.Item(5, 20, 3, this.internalBuffer));

        return group;
    }

    private class InaccessibleInfiniteHandler extends NotifiableItemStackHandler {

        private ItemStackTransfer itemTransfer;

        public InaccessibleInfiniteHandler(MetaMachine holder) {
            super(holder, 1, IO.OUT, IO.NONE);
            internalBuffer.setOnContentsChanged(this::onContentsChanged);
        }

        public ItemStackTransfer getTransfer() {
            if (this.itemTransfer == null) {
                this.itemTransfer = new ItemStackTransferDelegate();
            }
            return itemTransfer;
        }

        @Override
        public int getSize() {
            return Integer.MAX_VALUE;
        }

        private long getCachedAmount() {
            long itemAmount = 0;
            var grid = getMainNode().getGrid();
            if (grid != null && !internalBuffer.isEmpty()) {
                for (var slot : internalBuffer) {
                    itemAmount += grid.getStorageService().getInventory().getAvailableStacks()
                            .get(slot.getKey());
                }
            }
            return itemAmount;
        }

        private boolean canInsertItem() {
            return getCachedAmount() < capacitySize;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public @Nullable List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                            @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, handlerIO, getTransfer());
        }

        @NoArgsConstructor
        private class ItemStackTransferDelegate extends ItemStackTransfer {

            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public int getSlotLimit(int slot) {
                return Integer.MAX_VALUE; // todo add me components for sizing
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                // NO-OP
            }

            @Override
            public ItemStack insertItem(
                                        int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
                var key = AEItemKey.of(stack);
                int count = stack.getCount();
                long oldValue = internalBuffer.storage.getOrDefault(key, 0);
                long changeValue = Math.min(Long.MAX_VALUE - oldValue, count);
                if (canInsertItem()) {
                    if (changeValue > 0) {
                        if (!simulate) {
                            internalBuffer.storage.put(key, oldValue + changeValue);
                            internalBuffer.onChanged();
                        }
                        return stack.copyWithCount((int) (count - changeValue));
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public ItemStackTransfer copy() {
                // because recipe testing uses copy transfer instead of simulated operations
                return new ItemStackTransferDelegate() {

                    @Override
                    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
                        return super.insertItem(slot, stack, true, notifyChanges);
                    }
                };
            }
        }
    }
}
