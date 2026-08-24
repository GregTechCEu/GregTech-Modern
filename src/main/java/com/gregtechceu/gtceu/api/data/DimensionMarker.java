package com.gregtechceu.gtceu.api.data;

import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.MemoizedSupplier;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DimensionMarker {

    public static final int MAX_TIER = 99;

    @Getter
    @Setter
    public int tier; // not only used to represent dimension tier, but also for sorting

    @Getter
    @Setter
    private @Nullable Component overrideName; // there may be other uses, so we store it

    private MemoizedSupplier<ItemStack> iconSupplier;

    public DimensionMarker(int tier, Supplier<? extends ItemLike> supplier, @Nullable Component overrideName) {
        this.tier = tier;
        this.overrideName = overrideName;
        this.iconSupplier = GTMemoizer.memoize(() -> getStack(supplier.get().asItem()));

        if (tier < 0 || tier > MAX_TIER) {
            throw new IllegalArgumentException("Tier must be between 0 and " + MAX_TIER);
        }
    }

    public void setIcon(Supplier<? extends ItemLike> supplier) {
        this.iconSupplier = GTMemoizer.memoize(() -> getStack(supplier.get().asItem()));
    }

    public ItemStack getIcon() {
        return iconSupplier.get();
    }

    private ItemStack getStack(Item item) {
        ItemStack stack = new ItemStack(item);
        if (overrideName != null) {
            stack.set(DataComponents.ITEM_NAME, overrideName);
        }
        return stack;
    }
}
