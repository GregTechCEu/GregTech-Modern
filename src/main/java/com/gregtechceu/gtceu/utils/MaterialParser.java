package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialEntry;
import com.gregtechceu.gtceu.core.mixins.registrate.RegistryEntryAccessor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.IntConsumer;

@UtilityClass
public class MaterialParser {

    /**
     * Parse a material entry. This method will never return null but makes no other guarantees about the validity of
     * the return value or if it'll ever even be bound. Use with care.
     *
     * @param chars a string
     * @return a material registry entry.
     */
    public static MaterialEntry materialEntryFromString(CharSequence chars) {
        return findMaterialEntry(GTCEu.id(chars.toString().trim()));
    }

    /**
     * Parse a material entry. This method will never return null but makes no other guarantees about the validity of
     * the return value or if it'll ever even be bound. Use with care.
     *
     * @param id the material's ID
     * @return a material registry entry.
     */
    @SuppressWarnings("unchecked")
    public static MaterialEntry findMaterialEntry(ResourceLocation id) {
        MaterialEntry materialEntry = null;

        GTRegistrate registrate = GTRegistrate.create(id.getNamespace(), false);
        var registryEntryOpt = registrate.getOptional(id.getPath(), GTRegistries.Keys.MATERIAL);
        if (registryEntryOpt.isPresent()) {
            RegistryEntry<?, ?> registryEntry = registryEntryOpt.get();
            if (registryEntry instanceof MaterialEntry entry) {
                materialEntry = entry;
            }
            else if (registryEntry.getKey().isFor(GTRegistries.Keys.MATERIAL)) {
                materialEntry = new MaterialEntry(registrate, (DeferredHolder<Material, Material>) registryEntry);
            }
        }
        if (materialEntry == null) {
            materialEntry = new MaterialEntry(registrate, DeferredHolder.create(GTRegistries.Keys.MATERIAL, id));
        }
        return materialEntry;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static @Nullable MaterialEntry getEntryFrom(@UnknownNullability Holder<?> holder) {
        if (holder == null) return null;
        ResourceKey<?> key = holder.getKey();
        if (key == null || !key.isFor(GTRegistries.Keys.MATERIAL)) {
            return null;
        }

        return switch (holder) {
            // simple case
            case MaterialEntry materialEntry -> materialEntry;
            // try to reduce allocations when we have something that can have a GTRegistrate parent
            case RegistryEntry<?, ?> entry -> {
                AbstractRegistrate<?> entryOwner = ((RegistryEntryAccessor) entry).gtceu$getOwner();
                GTRegistrate registrate;
                if (entryOwner instanceof GTRegistrate gtReg) {
                    registrate = gtReg;
                } else {
                    registrate = GTRegistrate.create(entryOwner.getModid(), false);
                }
                yield new MaterialEntry(registrate, (DeferredHolder) entry);
            }
            // IDK if this'll actually be reacted. but it's good to have nonetheless.
            case DeferredHolder<?, ?> deferred -> {
                GTRegistrate registrate = GTRegistrate.create(deferred.getId().getNamespace(), false);
                yield new MaterialEntry(registrate, (DeferredHolder) deferred);
            }
            // how'd we get here?
            case Holder.Reference<?> reference -> {
                GTRegistrate registrate = GTRegistrate.create(key.location().getNamespace(), false);
                yield new MaterialEntry(registrate, (DeferredHolder) DeferredHolder.create(key));
            }
            // direct holder (or something very odd), not even gonna try.
            default -> null;
        };
    }

    public static @UnknownNullability MaterialEntry @NotNull [] getAsEntries(@Nullable IntConsumer nullIndexConsumer,
                                                                             @UnknownNullability Holder<?> @NotNull ... holders) {
        MaterialEntry[] entries = new MaterialEntry[holders.length];
        for (int i = 0; i < holders.length; i++) {
            entries[i] = getEntryFrom(holders[i]);
            if (entries[i] == null && nullIndexConsumer != null) {
                nullIndexConsumer.accept(i);
            }
        }
        return entries;
    }
}
