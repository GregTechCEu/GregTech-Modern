package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagOrCycleItemStackHandler implements IItemHandlerModifiable {

    @Getter
    private List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> stacks;

    private List<List<ItemStack>> unwrapped = null;

    public TagOrCycleItemStackHandler(List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> stacks) {
        updateStacks(stacks);
    }

    public void updateStacks(List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> stacks) {
        this.stacks = new ArrayList<>(stacks);
        this.unwrapped = null;
    }

    public List<List<ItemStack>> getUnwrapped() {
        if (unwrapped == null) {
            unwrapped = stacks.stream()
                    .map(tagOrItem -> {
                        if (tagOrItem == null) {
                            return null;
                        }
                        return tagOrItem.map(
                                tagList -> tagList
                                        .stream()
                                        .flatMap(pair -> BuiltInRegistries.ITEM.getTag(pair.getFirst())
                                                .map(holderSet -> holderSet.stream()
                                                        .map(holder -> new ItemStack(holder.value(), pair.getSecond())))
                                                .orElseGet(Stream::empty))
                                        .toList(),
                                Function.identity());
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return unwrapped;
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        List<ItemStack> stackList = getUnwrapped().get(slot);
        return stackList == null || stackList.isEmpty() ? ItemStack.EMPTY :
                stackList.get(Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size()));
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        if (index >= 0 && index < stacks.size()) {
            stacks.set(index, Either.right(List.of(stack)));
            unwrapped = null;
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
