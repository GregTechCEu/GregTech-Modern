package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class MEInputBusPartMachine extends MEBusPartMachine implements IInWorldGridNodeHost, IGridConnectedBlockEntity {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputBusPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);
    private final static int CONFIG_SIZE = 16;

    private ExportOnlyAEItemList aeItemHandler;
    private IGrid aeProxy;

    public MEInputBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    public void autoIO() {
        if (getLevel().isClientSide) return;
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;
        
        if (this.updateMEStatus()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (ExportOnlyAEItem aeSlot : this.aeItemHandler.inventory) {
                // Try to clear the wrong item
                GenericStack exceedItem = aeSlot.exceedStack();
                if (exceedItem != null) {
                    long total = exceedItem.amount();
                    long inserted = aeNetwork.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE, this.actionSource);
                    if (inserted > 0) {
                        aeSlot.extractItem(0, (int) inserted, false);
                        continue;
                    } else {
                        aeSlot.extractItem(0, (int) total, false);
                    }
                }
                // Fill it
                GenericStack reqItem = aeSlot.requestStack();
                if (reqItem != null) {
                    long extracted = aeNetwork.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE, this.actionSource);
                    if (extracted != 0) {
                        aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                    }
                }
            }
            this.updateInventorySubscription();
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEItemConfigWidget(3, 10, this.aeItemHandler.inventory));

        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static class ExportOnlyAEItemList extends NotifiableItemStackHandler {
        public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ExportOnlyAEItemList.class, NotifiableItemStackHandler.MANAGED_FIELD_HOLDER);

        @Persisted
        ExportOnlyAEItem[] inventory;

        public ExportOnlyAEItemList(MetaMachine holder, int slots) {
            super(holder, slots, IO.IN);
            this.inventory = new ExportOnlyAEItem[CONFIG_SIZE];
            for (int i = 0; i < CONFIG_SIZE; i ++) {
                this.inventory[i] = new ExportOnlyAEItem(null, null);
            }
            for (ExportOnlyAEItem slot : this.inventory) {
                slot.setOnContentsChanged(this::onContentsChanged);
            }
        }

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            this.machine.onChanged();
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, left, simulate, this.handlerIO, new ItemStackTransfer(NonNullList.of(ItemStack.EMPTY, Arrays.stream(inventory).map(item -> item.getStackInSlot(0)).toArray(ItemStack[]::new))) {
                @NotNull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                    ItemStack extracted = super.extractItem(slot, amount, simulate, notifyChanges);
                    if (!extracted.isEmpty()) {
                        inventory[slot].extractItem(0, amount, simulate, notifyChanges);
                    }
                    return extracted;
                }
            });
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            // NO-OP
        }

        @Override
        public int getSlots() {
            return MEInputBusPartMachine.CONFIG_SIZE;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < CONFIG_SIZE) {
                return this.inventory[slot].getStackInSlot(0);
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= 0 && slot < CONFIG_SIZE) {
                return this.inventory[slot].extractItem(0, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }


        @NotNull
        @Override
        public Object createSnapshot() {
            return Arrays.stream(inventory).map(IItemTransfer::createSnapshot).toArray(Object[]::new);
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof Object[] array && array.length == inventory.length) {
                for (int i = 0; i < array.length; i++) {
                    inventory[i].restoreFromSnapshot(array[i]);
                }
            }
        }
    }

    public static class ExportOnlyAEItem extends ExportOnlyAESlot implements IItemTransfer {

        public ExportOnlyAEItem(GenericStack config, GenericStack stock) {
            super(config, stock);
        }

        public ExportOnlyAEItem() {
            super();
        }

        @Override
        public ExportOnlyAEItem copy() {
            return new ExportOnlyAEItem(
                    this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock)
            );
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            // NO-OP
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            return stack;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0 && this.stock != null) {
                return this.stock.what() instanceof AEItemKey itemKey ? itemKey.toStack((int) this.stock.amount()) : ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            if (slot == 0 && this.stock != null) {
                int extracted = (int) Math.min(this.stock.amount(), amount);
                ItemStack result = this.stock.what() instanceof AEItemKey itemKey ? itemKey.toStack((int) this.stock.amount()) : ItemStack.EMPTY.copy();
                result.setCount(extracted);
                if (!simulate) {
                    this.stock = ExportOnlyAESlot.copy(this.stock, this.stock.amount() - extracted);
                    if (this.stock.amount() == 0) {
                        this.stock = null;
                    }
                }
                if (notifyChanges && this.onContentsChanged != null) {
                    this.onContentsChanged.run();
                }
                return result;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void addStack(GenericStack stack) {
            if (this.stock == null) {
                this.stock = stack;
            } else {
                this.stock = GenericStack.sum(this.stock, stack);
            }
            this.onContentsChanged.run();
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
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
    }
}
