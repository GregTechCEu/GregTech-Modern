package com.gregtechceu.gtceu.core;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;

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
}
