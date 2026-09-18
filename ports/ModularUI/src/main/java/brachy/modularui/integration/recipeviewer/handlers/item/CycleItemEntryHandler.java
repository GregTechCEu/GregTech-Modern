package brachy.modularui.integration.recipeviewer.handlers.item;

import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;

import org.jspecify.annotations.NullMarked;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@NullMarked
public class CycleItemEntryHandler implements IItemHandlerModifiable {

    @Getter
    private final List<ItemEntryList> entries;
    private List<List<ItemStack>> unwrapped = null;

    public CycleItemEntryHandler(List<ItemEntryList> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public List<List<ItemStack>> getUnwrapped() {
        if (unwrapped == null) {
            unwrapped = entries.stream()
                    .map(CycleItemEntryHandler::getStacksNullable)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return unwrapped;
    }

    private static @Nullable List<ItemStack> getStacksNullable(@Nullable ItemEntryList list) {
        if (list == null) return null;
        return list.getStacks();
    }

    public ItemEntryList getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public int getSlots() {
        return entries.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        List<ItemStack> stackList = getUnwrapped().get(slot);
        return stackList == null || stackList.isEmpty() ? ItemStack.EMPTY :
                stackList.get(Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size()));
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        if (index >= 0 && index < entries.size()) {
            entries.set(index, ItemStackList.of(stack));
            unwrapped = null;
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
