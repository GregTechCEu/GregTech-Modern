package com.gregtechceu.gtceu.api.misc.virtualregistry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualRedstone;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public final class EntryTypes<T extends VirtualEntry> {

    private static final Map<ResourceLocation, EntryTypes<?>> TYPES = new Object2ObjectOpenHashMap<>();

    public static final EntryTypes<VirtualTank> ENDER_FLUID = addEntryType(GTCEu.id("ender_fluid"), VirtualTank::new);
    public static final EntryTypes<VirtualItemStorage> ENDER_ITEM = addEntryType(GTCEu.id("ender_item"),
            VirtualItemStorage::new);
    public static final EntryTypes<VirtualRedstone> ENDER_REDSTONE = addEntryType(GTCEu.id("ender_redstone"),
            VirtualRedstone::new);
    // ENDER_ENERGY("ender_energy", null),
    // ENDER_REDSTONE("ender_redstone", null);

    @Getter
    private final ResourceLocation id;
    private final Supplier<T> factory;

    private EntryTypes(ResourceLocation id, Supplier<T> supplier) {
        this.id = id;
        this.factory = supplier;
    }

    @Nullable
    public static EntryTypes<? extends VirtualEntry> fromString(String name) {
        return TYPES.get(GTCEu.id(name));
    }

    public static <E extends VirtualEntry> EntryTypes<E> addEntryType(ResourceLocation location, Supplier<E> supplier) {
        var type = new EntryTypes<>(location, supplier);
        if (!TYPES.containsKey(location)) {
            TYPES.put(location, type);
        } else {
            GTCEu.LOGGER.warn("Entry \"{}\" is already registered!", location);
        }
        return type;
    }

    public T createInstance(HolderLookup.@NotNull Provider registries, CompoundTag nbt) {
        var entry = createInstance();
        entry.deserializeNBT(registries, nbt);
        return entry;
    }

    public T createInstance() {
        return factory.get();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
