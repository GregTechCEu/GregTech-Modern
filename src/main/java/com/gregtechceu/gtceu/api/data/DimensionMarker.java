package com.gregtechceu.gtceu.api.data;

import com.gregtechceu.gtceu.utils.SupplierMemoizer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DimensionMarker {

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

    private ItemStack getStack(Item item) {
        ItemStack stack = new ItemStack(item);
        if (overrideName != null) {
            stack.setHoverName(Component.translatable(overrideName));
        }
        return stack;
    }
}
