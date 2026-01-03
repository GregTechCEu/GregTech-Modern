package com.gregtechceu.gtceu.api.misc.virtualregistry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualRedstone;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public final class EntryTypes<T extends VirtualEntry> {

    private static final Map<ResourceLocation, EntryTypes<?>> TYPES_MAP = new Object2ObjectOpenHashMap<>();
    public static final EntryTypes<VirtualTank> ENDER_FLUID = addEntryType(GTCEu.id("ender_fluid"), VirtualTank::new);
    public static final EntryTypes<VirtualItemStorage> ENDER_ITEM = addEntryType(GTCEu.id("ender_item"),
            VirtualItemStorage::new);
    public static final EntryTypes<VirtualRedstone> ENDER_REDSTONE = addEntryType(GTCEu.id("ender_redstone"),
            VirtualRedstone::new);
    // ENDER_ENERGY("ender_energy", null),
    private final ResourceLocation location;
    private final Supplier<T> factory;

    private EntryTypes(ResourceLocation location, Supplier<T> supplier) {
        this.location = location;
        this.factory = supplier;
    }

    @Nullable
    public static EntryTypes<? extends VirtualEntry> fromString(String name) {
        return TYPES_MAP.getOrDefault(GTCEu.id(name), null);
    }

    @Nullable
    public static EntryTypes<? extends VirtualEntry> fromLocation(ResourceLocation location) {
        return TYPES_MAP.getOrDefault(location, null);
    }

    public static <E extends VirtualEntry> EntryTypes<E> addEntryType(ResourceLocation location, Supplier<E> supplier) {
        var type = new EntryTypes<>(location, supplier);
        if (!TYPES_MAP.containsKey(location)) {
            TYPES_MAP.put(location, type);
        } else {
            GTCEu.LOGGER.warn("Entry \"{}\" is already registered!", location);
        }
        return type;
    }

    public T createInstance(CompoundTag nbt) {
        var entry = createInstance();
        entry.deserializeNBT(nbt);
        return entry;
    }

    public T createInstance() {
        return factory.get();
    }

    @Override
    public String toString() {
        return this.location.toString();
    }
}
