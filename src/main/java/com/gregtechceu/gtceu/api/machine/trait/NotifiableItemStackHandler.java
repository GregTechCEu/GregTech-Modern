package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.item.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.CircuitMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.CustomMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.IntersectionMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemTagMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.NBTPredicateItemStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.PartialNBTItemStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.StrictNBTItemStackMapIngredient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class NotifiableItemStackHandler extends NotifiableRecipeHandlerTrait<ItemIngredient>
                                        implements ICapabilityTrait, IItemHandlerModifiable {

    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Persisted
    @DescSynced
    public final CustomItemStackHandler storage;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private boolean shouldSearchContent = true;
    private Boolean isEmpty;

    public NotifiableItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO,
                                      IntFunction<CustomItemStackHandler> storageFactory) {
        super(machine);
        this.handlerIO = handlerIO;
        this.storage = storageFactory.apply(slots);
        this.capabilityIO = capabilityIO;
        this.storage.setOnContentsChanged(this::onContentsChanged);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO) {
        this(machine, slots, handlerIO, capabilityIO, CustomItemStackHandler::new);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO) {
        this(machine, slots, handlerIO, handlerIO);
    }

    public NotifiableItemStackHandler setFilter(Predicate<ItemStack> filter) {
        this.storage.setFilter(filter);
        return this;
    }

    public void onContentsChanged() {
        isEmpty = null;
        notifyListeners();
    }

    @Override
    public boolean handleRecipe(IO io, GTRecipe recipe, List<ItemIngredient> left, boolean simulate) {
        return handleRecipe(io, recipe, left, simulate, handlerIO, storage);
    }

    // Notable caller is ItemRecipeHandler, used for MinerLogic
    public static boolean handleRecipe(IO io, GTRecipe recipe, List<ItemIngredient> left, boolean simulate,
                                                 IO handlerIO, CustomItemStackHandler storage) {
        if (! handlerIO.support(io)) return false;

        Runnable listener = storage.getOnContentsChanged();
        storage.setOnContentsChanged(() -> {});
        boolean changed = false;

        ItemStack[] visited = new ItemStack[storage.getSlots()];
        for (var it = left.listIterator(); it.hasNext();) {
            var ingredient = it.next();
            int amount;

            if (io == IO.IN) {
                if (simulate) {
                    amount = ingredient.getCount();
                } else {
                    ItemStack stack = ingredient.toStack();
                    if (stack.isEmpty()) {
                        it.remove();
                        continue;
                    }
                    amount = stack.getCount();
                }

                for (int slot = 0; slot < storage.getSlots(); ++slot) {
                    ItemStack current = visited[slot] == null ? storage.getStackInSlot(slot) : visited[slot];
                    int count = current.getCount();
                    if (current.isEmpty()) continue;
                    if (ingredient.test(current)) {
                        var extracted = storage.extractItem(slot, Math.min(count, amount), simulate);
                        if (!extracted.isEmpty()) {
                            changed = true;
                            visited[slot] = extracted.copyWithCount(count - extracted.getCount());
                        }
                        amount -= extracted.getCount();
                    }
                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }
            } else {
                ItemStack outputStack;
                if (simulate) {
                    ItemStack[] items = ingredient.getItems();
                    if (items.length == 0 || items[0].isEmpty()) {
                        it.remove();
                        continue;
                    }
                    outputStack = items[0];
                    amount = ingredient.getCount();
                } else {
                    outputStack = ingredient.toStack();
                    if (outputStack.isEmpty()) {
                        it.remove();
                        continue;
                    }
                    amount = outputStack.getCount();
                }

                for (int slot = 0; slot < storage.getSlots(); ++slot) {
                    ItemStack current = visited[slot] == null ? storage.getStackInSlot(slot) : visited[slot];
                    int count = current.getCount();
                    ItemStack output = outputStack.copyWithCount(amount);
                    if (visited[slot] == null || GTUtil.isSameItemSameTags(visited[slot], output)) {
                        if (count < output.getMaxStackSize() && count < storage.getSlotLimit(slot)) {
                            var remainder = storage.insertItem(slot, output, simulate);
                            if (remainder.getCount() < amount) {
                                changed = true;
                                visited[slot] = output.copyWithCount(count + amount - remainder.getCount());
                            }
                            amount = remainder.getCount();
                        }
                    }
                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }
            }

            if (amount > 0) {
                it.set(ingredient.copyWithCount(amount));
            }
        }

        storage.setOnContentsChanged(listener);
        if (changed && !simulate) listener.run();

        return left.isEmpty();
    }

    @Override
    public RecipeCapability<ItemIngredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    public int getSlots() {
        return storage.getSlots();
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return new ArrayList<>(stacks);
    }

    @Override
    public @NotNull List<AbstractMapIngredient> getMapIngredients() {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack stack = getStackInSlot(i);
            ingredients.addAll(mapItemStack(stack));

        }
        return ingredients;
    }

    public static List<AbstractMapIngredient> mapItemStack(ItemStack stack) {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        if (stack.isEmpty()) return ingredients;
        if (stack.is(GTItems.PROGRAMMED_CIRCUIT.get())) {
            ingredients.addAll(CircuitMapIngredient.from(IntCircuitBehaviour.getCircuitConfiguration(stack)));
            return ingredients;
        }
        ingredients.addAll(ItemMapIngredient.from(stack));
        ingredients.addAll(ItemTagMapIngredient.from(stack));
        ingredients.addAll(StrictNBTItemStackMapIngredient.from(stack));
        ingredients.addAll(PartialNBTItemStackMapIngredient.from(stack));
        ingredients.addAll(NBTPredicateItemStackMapIngredient.from(stack));
        ingredients.addAll(IntersectionMapIngredient.from(stack));
        ingredients.addAll(CustomMapIngredient.from(stack));
        return ingredients;
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                amount += stack.getCount();
            }
        }
        return amount;
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

    public void exportToNearby(@NotNull Direction... facings) {
        if (isEmpty()) return;
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            var filter = getMachine().getItemCapFilter(facing, IO.OUT);
            GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(this, adj, filter));
        }
    }

    public void importFromNearby(@NotNull Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            var filter = getMachine().getItemCapFilter(facing, IO.IN);
            GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(adj, this, filter));
        }
    }

    //////////////////////////////////////
    // ******* Capability ********//
    //////////////////////////////////////
    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return storage.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int index, @NotNull ItemStack stack) {
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
