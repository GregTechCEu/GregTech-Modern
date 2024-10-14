package com.gregtechceu.gtceu.utils.virtualregistry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.virtualregistry.entries.VirtualTank;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;

public class EntryTypes<T extends VirtualEntry> {

    private static final Map<ResourceLocation, EntryTypes<?>> TYPES_MAP = new Object2ObjectOpenHashMap<>();
    public static final EntryTypes<VirtualTank> ENDER_FLUID = addEntryType(GTCEu.id("ender_fluid"), VirtualTank::new);

    private final ResourceLocation location;
    private final Supplier<T> factory;

    private EntryTypes(ResourceLocation location, Supplier<T> supplier) {
        this.location = location;
        this.factory = supplier;
    }

    public T createInstace(CompoundTag tag) {
        var entry = createInstance();
        entry.deserializeNBT(tag);
        return entry;
    }

    public T createInstance() {
        return factory.get();
    }

    @Override
    public String toString() {
        return this.location.toString();
    }

    public static EntryTypes<? extends VirtualEntry> fromString(String name) {
        return TYPES_MAP.getOrDefault(GTCEu.id(name), null);
    }

    public static EntryTypes<? extends VirtualEntry> fromLocation(String location) {
        return TYPES_MAP.getOrDefault(new ResourceLocation(location), null);
    }

    public static <E extends VirtualEntry> EntryTypes<E> addEntryType(ResourceLocation location, Supplier<E> supplier) {
        var type = new EntryTypes<>(location, supplier);
        if(!TYPES_MAP.containsKey(location)) {
            TYPES_MAP.put(location, type);
        }
        else {
            GTCEu.LOGGER.warn("Entry {} is already registered", location);
        }
        return type;
    }
}
