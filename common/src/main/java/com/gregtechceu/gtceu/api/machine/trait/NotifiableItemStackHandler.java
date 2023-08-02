package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote NotifiableItemStackHandler
 */
public class NotifiableItemStackHandler extends NotifiableRecipeHandlerTrait<Ingredient> implements ICapabilityTrait, IItemTransfer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableItemStackHandler.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter @Setter
    private long timeStamp;
    @Persisted @DescSynced
    public final ItemStackTransfer storage;
    private Boolean isEmpty;

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO, Function<Integer, ItemStackTransfer> transferFactory) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = handlerIO;
        this.storage = transferFactory.apply(slots);
        this.capabilityIO = capabilityIO;
        this.storage.setOnContentsChanged(this::onContentChanged);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO) {
        this(machine, slots, handlerIO, capabilityIO, ItemStackTransfer::new);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO) {
        this(machine, slots, handlerIO, handlerIO);
    }

    public NotifiableItemStackHandler setFilter(Function<ItemStack, Boolean> filter) {
        this.storage.setFilter(filter);
        return this;
    }

    protected void onContentChanged() {
        isEmpty = null;
        updateTimeStamp(machine.getLevel());
        notifyListeners();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
        return handleIngredient(io, left, simulate, this.handlerIO, storage);
    }

    @Nullable
    public static List<Ingredient> handleIngredient(IO io, List<Ingredient> left, boolean simulate, IO handlerIO, ItemStackTransfer storage) {
        if (io != handlerIO) return left;
        var capability = simulate ? storage.copy() : storage;
        Iterator<Ingredient> iterator = left.iterator();
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                SLOT_LOOKUP:
                for (int i = 0; i < capability.getSlots(); i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    //Does not look like a good implementation, but I think it's at least equal to vanilla Ingredient::test
                    if (ingredient.test(itemStack)) {
                        ItemStack[] ingredientStacks = ingredient.getItems();
                        for (ItemStack ingredientStack : ingredientStacks) {
                            if (ingredientStack.is(itemStack.getItem())) {
                                ItemStack extracted = capability.extractItem(i, ingredientStack.getCount(), false);
                                ingredientStack.setCount(ingredientStack.getCount() - extracted.getCount());
                                if (ingredientStack.isEmpty()) {
                                    iterator.remove();
                                    break SLOT_LOOKUP;
                                }
                            }
                        }
                    }
                }
            }
        } else if (io == IO.OUT) {
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                var items = ingredient.getItems();
                if (items.length == 0) {
                    iterator.remove();
                    continue;
                }
                ItemStack output = items[0];
                if (!output.isEmpty()) {
                    for (int i = 0; i < capability.getSlots(); i++) {
                        ItemStack leftStack = capability.insertItem(i, output.copy(), false);
                        output.setCount(leftStack.getCount());
                        if (output.isEmpty()) break;
                    }
                }
                if (output.isEmpty()) iterator.remove();
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    public int getSlots() {
        return storage.getSlots();
    }

    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (int i = 0; i < storage.getSlots(); i++) {
                if (!storage.getStackInSlot(i).isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public void exportToNearby(Direction... facings) {
        if (isEmpty()) return;
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            ItemTransferHelper.exportToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

    public void importFromNearby(Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            ItemTransferHelper.importToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

    //////////////////////////////////////
    //*******     Capability    ********//
    //////////////////////////////////////
    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return storage.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        storage.setStackInSlot(index, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (canCapInput()) {
            return storage.insertItem(slot, stack, simulate);
        }
        return stack;
    }

    public ItemStack insertItemInternal(int slot, @NotNull ItemStack stack, boolean simulate) {
        return storage.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (canCapOutput()) {
            return storage.extractItem(slot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        return storage.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return storage.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return storage.isItemValid(slot, stack);
    }

}
