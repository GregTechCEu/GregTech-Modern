package com.gregtechceu.gtceu.api.data;

import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.Validator;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.MemoizedSupplier;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DimensionMarker {

    public static final int MAX_TIER = 99;

    @Getter
    public final ResourceKey<Level> dimension;

    @Getter
    public final int tier; // not only used to represent dimension tier, but also for sorting

    @Getter
    @Nullable
    private final String overrideName; // there may be other uses, so we store it

    private final MemoizedSupplier<ItemStack> iconSupplier;

    public DimensionMarker(ResourceKey<Level> dim, int tier, ResourceLocation itemKey, @Nullable String overrideName) {
        if (tier < 0 || tier >= MAX_TIER) {
            throw new IllegalArgumentException("Tier must be between 0 and " + (MAX_TIER - 1));
        }
        this.dimension = dim;
        this.tier = tier;
        this.overrideName = overrideName;
        this.iconSupplier = GTMemoizer.memoize(() -> ForgeRegistries.ITEMS.getDelegate(itemKey)
                .map(Holder::get)
                .map(this::getStack)
                .orElse(ItemStack.EMPTY));
    }

    public DimensionMarker(ResourceKey<Level> dim, int tier, Supplier<? extends ItemLike> supplier, @Nullable String overrideName) {
        if (tier < 0 || tier >= MAX_TIER) {
            throw new IllegalArgumentException("Tier must be between 0 and " + (MAX_TIER - 1));
        }
        this.dimension = dim;
        this.tier = tier;
        this.overrideName = overrideName;
        this.iconSupplier = GTMemoizer.memoize(() -> getStack(supplier.get().asItem()));
    }

    public ItemStack getIcon() {
        return iconSupplier.get();
    }

    private ItemStack getStack(Item item) {
        ItemStack stack = new ItemStack(item);
        if (overrideName != null) {
            stack.setHoverName(Component.translatable(overrideName));
        }
        return stack;
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    public static class Builder extends BuilderBase<DimensionMarker> {

        private Supplier<Item> iconSupplier;
        private int tier = 0;
        @Nullable
        private String overrideName;

        public Builder(ResourceLocation dimKey) {
            super(dimKey);
        }

        @HideFromJS
        public DimensionMarker buildAndRegister() {
            Validator.validate(
                    id,
                    Validator.errorIfNull(iconSupplier, "icon"),
                    Validator.errorIfOutOfRange(tier, "tier", 0, MAX_TIER - 1));
            return new DimensionMarker(ResourceKey.create(Registries.DIMENSION, id), tier, iconSupplier, overrideName);
        }

        @Override
        public DimensionMarker register() {
            return value = buildAndRegister();
        }
    }
}
