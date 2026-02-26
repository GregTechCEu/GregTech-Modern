package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.DummyCraftingInput;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

@ExtensionMethod(SizedIngredientExtensions.class)
public class NotifiableItemStackHandler extends NotifiableRecipeHandlerTrait<SizedIngredient>
                                        implements ICapabilityTrait, IItemHandlerModifiable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableItemStackHandler.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
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
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                   boolean simulate) {
        return handleRecipe(io, recipe, left, simulate, handlerIO, storage);
    }

    // TODO: See if implementable in outside callers and unstatic; or move to different common class if not
    // Notable caller is ItemRecipeHandler, used for MinerLogic
    public static List<SizedIngredient> handleRecipe(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                     boolean simulate,
                                                     IO handlerIO, CustomItemStackHandler storage) {
        if (io != handlerIO) return left;
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;

        // Temporarily remove listener so that we can broadcast the entire set of transactions once
        Runnable listener = storage.getOnContentsChanged();
        storage.setOnContentsChanged(() -> {});
        boolean changed = false;

        // Store the ItemStack in each slot after an operation
        // Necessary for simulation since we don't actually modify the slot's contents
        // Doesn't hurt for execution, and definitely cheaper than copying the entire storage
        ItemStack[] visited = new ItemStack[storage.getSlots()];
        for (var it = left.listIterator(); it.hasNext();) {
            var ingredient = it.next();
            if (ingredient.ingredient().hasNoItems()) {
                it.remove();
                continue;
            }

            ItemStack[] items;
            int amount;
            if (ingredient.getContainedCustom() instanceof IntProviderIngredient provider) {
                provider.setItemStacks(null);
                provider.setSampledCount(-1);

                ItemStack output;
                if (simulate) {
                    output = provider.getMaxSizeStack();
                    items = new ItemStack[] { output };
                } else {
                    items = provider.getItemStacks();
                    if (items.length == 0 || items[0].isEmpty()) {
                        it.remove();
                        continue;
                    }
                    output = items[0];
                }
                amount = output.getCount();
            } else {
                items = ingredient.getItems();
                if (items.length == 0 || items[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                amount = ingredient.count();
            }

            for (int slot = 0; slot < storage.getSlots(); ++slot) {
                ItemStack current = visited[slot] == null ? storage.getStackInSlot(slot) : visited[slot];
                int count = current.getCount();

                if (io == IO.IN) {
                    if (current.isEmpty()) continue;
                    if (ingredient.ingredient().test(current)) {
                        var extracted = getActioned(storage, slot, recipe.ingredientActions);
                        if (extracted == null) extracted = storage.extractItem(slot, Math.min(count, amount), simulate);
                        if (!extracted.isEmpty()) {
                            changed = true;
                            visited[slot] = extracted.copyWithCount(count - extracted.getCount());
                        }
                        amount -= extracted.getCount();
                    }
                } else { // IO.OUT
                    ItemStack output = items[0].copyWithCount(amount);
                    // Only try this slot if not visited or if visited with the same type of item
                    if (visited[slot] == null || ItemStack.isSameItemSameComponents(visited[slot], output)) {
                        if (count < output.getMaxStackSize() && count < storage.getSlotLimit(slot)) {
                            var remainder = getActioned(storage, slot, recipe.ingredientActions);
                            if (remainder == null) remainder = storage.insertItem(slot, output, simulate);
                            if (remainder.getCount() < amount) {
                                changed = true;
                                visited[slot] = output.copyWithCount(count + amount - remainder.getCount());
                            }
                            amount = remainder.getCount();
                        }
                    }
                }

                if (amount <= 0) {
                    it.remove();
                    break;
                }
            }
            // Modify ingredient if we didn't finish it off
            if (amount > 0) {
                it.set(ingredient.copyWithCount(amount));
            }
        }

        storage.setOnContentsChanged(listener);
        if (changed && !simulate) listener.run();

        return left.isEmpty() ? null : left;
    }

    private static @Nullable ItemStack getActioned(CustomItemStackHandler storage, int index, List<?> actions) {
        if (!GTCEu.Mods.isKubeJSLoaded()) return null;
        // noinspection unchecked
        var actioned = KJSCallWrapper.applyIngredientAction(storage, index, (List<IngredientActionHolder>) actions);
        if (!actioned.isEmpty()) return actioned;
        return null;
    }

    @Override
    public RecipeCapability<SizedIngredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    public int getSlots() {
        return storage.getSlots();
    }

    @Override
    public int getSize() {
        return getSlots();
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

    public static class KJSCallWrapper {

        public static ItemStack applyIngredientAction(CustomItemStackHandler storage, int index,
                                                      List<IngredientActionHolder> ingredientActions) {
            var stack = storage.getStackInSlot(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            DummyCraftingInput container = new DummyCraftingInput(storage);
            for (var action : ingredientActions) {
                if (action.filter().checkFilter(index, stack)) {
                    return action.action().transform(stack.copy(), index, container);
                }
            }

            return ItemStack.EMPTY;
        }
    }
}
