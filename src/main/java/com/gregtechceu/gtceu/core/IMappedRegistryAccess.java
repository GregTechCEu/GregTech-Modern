package com.gregtechceu.gtceu.core;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;

public interface IMappedRegistryAccess<T> {

    default boolean gtceu$isFrozen() {
        throw new AssertionError();
    }

    default ObjectList<Holder.Reference<T>> gtceu$getById() {
        throw new AssertionError();
    }

    default Reference2IntMap<T> gtceu$getToId() {
        throw new AssertionError();
    }

    default Map<ResourceLocation, Holder.Reference<T>> gtceu$getByLocation() {
        throw new AssertionError();
    }

    default Map<ResourceKey<T>, Holder.Reference<T>> gtceu$getByKey() {
        throw new AssertionError();
    }

    default Map<T, Holder.Reference<T>> gtceu$getByValue() {
        throw new AssertionError();
    }

    default Map<ResourceKey<T>, RegistrationInfo> gtceu$getRegistrationInfos() {
        throw new AssertionError();
    }

    /// FOR TESTING ONLY; THIS WILL FUCK UP THINGS IF THINGS ARE IN USE!
    @TestOnly
    @VisibleForTesting
    default void gtceu$remove(ResourceKey<T> key) {
        if (this.gtceu$isFrozen()) {
            throw new IllegalStateException("Cannot remove entry from a frozen registry: " + key);
        }

        Holder.Reference<T> ref = this.gtceu$getByKey().remove(key);
        if (ref == null) {
            return; // not present, nothing to remove
        }

        this.gtceu$getByLocation().remove(key.location());

        T value = ref.value();
        this.gtceu$getByValue().remove(value);

        int id = this.gtceu$getToId().removeInt(value);
        ObjectList<Holder.Reference<T>> byId = this.gtceu$getById();

        if (id >= 0 && id < byId.size()) {
            byId.set(id, null);
        }

        this.gtceu$getRegistrationInfos().remove(key);
    }
}
