package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.DummyCraftingInput;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;

import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionHolder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote NotifiableItemStackHandler
 */
public class NotifiableItemStackHandler extends NotifiableRecipeHandlerTrait<SizedIngredient>
                                        implements ICapabilityTrait, IItemHandlerModifiable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableItemStackHandler.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Persisted(subPersisted = true)
    @DescSynced
    public final CustomItemStackHandler storage;
    private Boolean isEmpty;

    public NotifiableItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO,
                                      Function<Integer, CustomItemStackHandler> transferFactory) {
        super(machine);
        this.handlerIO = handlerIO;
        this.storage = transferFactory.apply(slots);
        this.capabilityIO = capabilityIO;
        this.storage.setOnContentsChanged(this::onContentsChanged);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO) {
        this(machine, slots, handlerIO, capabilityIO, CustomItemStackHandler::new);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO) {
        this(machine, slots, handlerIO, handlerIO);
    }

    public NotifiableItemStackHandler setFilter(Function<ItemStack, Boolean> filter) {
        this.storage.setFilter(filter::apply);
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
    public List<SizedIngredient> handleRecipeInner(IO io, RecipeHolder<GTRecipe> recipe, List<SizedIngredient> left,
                                                   @Nullable String slotName, boolean simulate) {
        return handleIngredient(io, recipe, left, simulate, this.handlerIO, storage);
    }

    @Nullable
    public static List<SizedIngredient> handleIngredient(IO io, RecipeHolder<GTRecipe> recipe, List<SizedIngredient> left,
                                                         boolean simulate, IO handlerIO,
                                                         CustomItemStackHandler storage) {
        if (io != handlerIO) return left;
        var capability = simulate ? storage.copy() : storage;
        Iterator<SizedIngredient> iterator = left.iterator();
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                SizedIngredient ingredient = iterator.next();
                SLOT_LOOKUP:
                for (int i = 0; i < capability.getSlots(); i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    // Does not look like a good implementation, but I think it's at least equal to vanilla
                    // Ingredient::test
                    if (ingredient.test(itemStack)) {
                        ItemStack[] ingredientStacks = ingredient.getItems();
                        for (ItemStack ingredientStack : ingredientStacks) {
                            if (ingredientStack.is(itemStack.getItem())) {
                                ItemStack extracted = ItemStack.EMPTY;
                                boolean didRunIngredientAction = false;
                                if (GTCEu.isKubeJSLoaded()) {
                                    // noinspection unchecked must be List<?> to be able to load without KJS.
                                    ItemStack actioned = KJSCallWrapper.applyIngredientAction(capability, i,
                                            (List<IngredientActionHolder>) recipe.value().ingredientActions);
                                    if (!actioned.isEmpty()) {
                                        extracted = actioned;
                                        didRunIngredientAction = true;
                                    }
                                }
                                if (!didRunIngredientAction) {
                                    extracted = capability.extractItem(i, ingredientStack.getCount(), false);
                                }
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
                SizedIngredient ingredient = iterator.next();
                int newCount = Integer.MIN_VALUE;
                if (ingredient.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProvider) {
                    intProvider.setItemStacks(null);
                    intProvider.setSampledCount(null);
                    newCount = intProvider.getSampledCount(GTValues.RNG);
                }
                var items = ingredient.getItems();
                if (items.length == 0) {
                    iterator.remove();
                    continue;
                }
                ItemStack output = items[0];
                if (newCount != Integer.MIN_VALUE) {
                    output.setCount(newCount);
                }
                if (!output.isEmpty()) {
                    for (int i = 0; i < capability.getSlots(); i++) {
                        ItemStack leftStack = ItemStack.EMPTY;
                        boolean didRunIngredientAction = false;
                        if (GTCEu.isKubeJSLoaded()) {
                            // noinspection unchecked must be List<?> to be able to load without KJS.
                            ItemStack actioned = KJSCallWrapper.applyIngredientAction(capability, i,
                                    (List<IngredientActionHolder>) recipe.value().ingredientActions);
                            if (!actioned.isEmpty()) {
                                leftStack = actioned;
                                didRunIngredientAction = true;
                            }
                        }
                        if (!didRunIngredientAction) {
                            leftStack = capability.insertItem(i, output.copy(), false);
                        }
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
    public List<Object> getContents() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return Arrays.asList(stacks.toArray());
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
            ItemTransferHelper.exportToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing),
                    facing.getOpposite());
        }
    }

    public void importFromNearby(@NotNull Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            ItemTransferHelper.importToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing),
                    facing.getOpposite());
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

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
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

            CraftingInput input = new DummyCraftingInput(storage);
            for (var holder : ingredientActions) {
                if (holder.filter().checkFilter(index, stack)) {
                    return holder.action().transform(stack.copy(), index, input);
                }
            }

            return ItemStack.EMPTY;
        }
    }
}
