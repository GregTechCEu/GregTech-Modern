package com.gregtechceu.gtceu.api.worldgen;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.MemoizedSupplier;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DimensionMarker {

    public static final int MAX_TIER = 99;

    @Getter
    public final int tier; // not only used to represent dimension tier, but also for sorting

    @Getter
    @Nullable
    private final String overrideName; // there may be other uses, so we store it

    private final MemoizedSupplier<ItemStack> iconSupplier;

    public DimensionMarker(int tier, ResourceLocation itemKey, @Nullable String overrideName) {
        this.tier = tier;
        this.overrideName = overrideName;
        this.iconSupplier = GTMemoizer.memoize(() -> BuiltInRegistries.ITEM.getOptional(itemKey)
                .map(this::getStack)
                .orElse(ItemStack.EMPTY));
    }

    public DimensionMarker(int tier, Supplier<? extends ItemLike> supplier, @Nullable String overrideName) {
        this.tier = tier;
        this.overrideName = overrideName;
        this.iconSupplier = GTMemoizer.memoize(() -> getStack(supplier.get().asItem()));
    }

    public ItemStack getIcon() {
        return iconSupplier.get();
    }

    public void register(ResourceLocation dimKey) {
        if (tier < 0 || tier >= MAX_TIER) {
            throw new IllegalArgumentException("Tier must be between 0 and " + (MAX_TIER - 1));
        }
        GTRegistries.register(GTRegistries.DIMENSION_MARKERS, dimKey, this);
    }

    private ItemStack getStack(Item item) {
        ItemStack stack = new ItemStack(item);
        if (overrideName != null) {
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable(overrideName));
        }
        return stack;
    }
}
