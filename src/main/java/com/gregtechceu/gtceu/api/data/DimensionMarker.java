package com.gregtechceu.gtceu.api.data;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.Validator;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
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
    public final int tier; // Not only used to represent dimension tier, but also for sorting

    @Nullable
    private final String overrideName;

    private final SupplierMemoizer.MemoizedSupplier<ItemStack> markerSupplier;

    public DimensionMarker(int tier, ResourceLocation resourceLocation, @Nullable String overrideName) {
        this.tier = tier;
        this.markerSupplier = SupplierMemoizer.memoize(() -> ForgeRegistries.ITEMS.getDelegate(resourceLocation)
                .map(Holder::get)
                .map(this::getStack)
                .orElse(ItemStack.EMPTY));
        this.overrideName = overrideName;
    }

    public DimensionMarker(int tier, Supplier<? extends ItemLike> supplier, @Nullable String overrideName) {
        this.tier = tier;
        this.markerSupplier = SupplierMemoizer.memoize(() -> getStack(supplier.get().asItem()));
        this.overrideName = overrideName;
    }

    public ItemStack getMarker() {
        return markerSupplier.get();
    }

    public void register(ResourceLocation id) {
        if (tier < 0 || tier >= MAX_TIER) {
            throw new IllegalArgumentException("Tier must be between 0 and " + (MAX_TIER - 1));
        }
        GTRegistries.DIMENSION_MARKERS.register(id, this);
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

        public Builder(ResourceLocation id) {
            super(id);
        }

        public Builder(ResourceLocation id, Object... args) {
            this(id);
        }

        @HideFromJS
        public DimensionMarker buildAndRegister() {
            Validator.validate(
                    id,
                    Validator.errorIfNull(iconSupplier, "icon"),
                    Validator.errorIfOutOfRange(tier, "tier", 0, MAX_TIER - 1));
            DimensionMarker marker = new DimensionMarker(tier, iconSupplier, overrideName);
            marker.register(id);
            return marker;
        }

        @Override
        public DimensionMarker register() {
            return value = buildAndRegister();
        }
    }
}
