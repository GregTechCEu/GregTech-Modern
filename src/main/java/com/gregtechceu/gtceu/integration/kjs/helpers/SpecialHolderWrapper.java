package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Original logic from
 * <a href=
 * "https://https://github.com/KubeJS-Mods/KubeJS/blob/main/src/main/java/dev/latvian/mods/kubejs/holder/HolderWrapper.java">KubeJS</a>
 */
public interface SpecialHolderWrapper {

    static boolean canWrapHolderSet(@Nullable Object from, TypeInfo target) {
        // only allow using this type wrapper when KubeJS *DOESN'T* have full registry information
        return lookupRegistry(RegistryType.lookup(target.param(0))) == null;
    }

    static @Nullable HolderSet<?> wrapHolderSet(Context cx, @Nullable Object from, TypeInfo target) {
        if (from == null) {
            throw Context.reportRuntimeError("Can't interpret 'null' as a Holder", cx);
        }

        TypeInfo type = target.param(0);
        RegistryType<?> registryType = lookupRegistryType(cx, type, from);

        Registry<?> reg = lookupRegistry(registryType);
        if (cx instanceof KubeJSContext kcx && reg != null) {
            // defer to the default type wrapper if KJS has full registries
            return HolderWrapper.wrapSet(kcx, from, type);
        } else {
            if (reg == null) {
                reg = GTRegistries.builtinRegistry().registry(registryType.key()).orElse(null);
            }
            if (reg != null) {
                return wrapHolderSet(cx, from, type, reg);
            }
        }
        return null;
    }

    static <T> HolderSet<T> wrapHolderSet(Context cx, @Nullable Object from, TypeInfo param, Registry<T> registry) {
        var simpleHolders = HolderWrapper.wrapSimpleSet(registry, from);

        if (simpleHolders != null) {
            return simpleHolders;
        }

        if (from instanceof Iterable<?> itr) {
            Stream.Builder<HolderSet<T>> allDirects = Stream.builder();
            List<HolderSet<T>> complex = new ArrayList<>();

            for (var elem : itr) {
                var wrapped = wrapHolderSet(cx, elem, param, registry);

                if (wrapped instanceof HolderSet.Direct<T> direct) {
                    allDirects.accept(direct);
                } else {
                    complex.add(wrapped);
                }
            }

            List<? extends Holder<T>> compressedDirects = allDirects.build()
                    .flatMap(HolderSet::stream)
                    .distinct().toList();

            if (compressedDirects.isEmpty()) {
                return switch (complex.size()) {
                    case 0 -> HolderSet.empty();
                    case 1 -> complex.getFirst();
                    default -> new OrHolderSet<>(complex);
                };
            } else {
                if (complex.isEmpty()) {
                    return HolderSet.direct(compressedDirects);
                } else {
                    complex.add(HolderSet.direct(compressedDirects));
                    return new OrHolderSet<>(complex);
                }
            }
        } else {
            Holder<T> holder = wrapHolder(cx, from, param, registry);
            return HolderSet.direct(holder);
        }
    }

    static <T> Holder<T> wrapHolder(Context cx, @Nullable Object from, TypeInfo param, Registry<T> registry) {
        if (from instanceof Holder<?> holder) {
            // noinspection unchecked
            return (Holder<T>) holder;
        } else if (from == null) {
            throw Context.reportRuntimeError("Can't interpret 'null' as a Holder", cx);
        }

        if (!ID.isKey(from)) {
            Holder<T> holder = registry.wrapAsHolder(Cast.to(from));

            if (holder instanceof Holder.Direct<T>) {
                var baseClass = lookupRegistryType(cx, param, from).baseClass();

                if (!baseClass.isInstance(from)) {
                    throw Context
                            .reportRuntimeError("Can't interpret '" + from + "' as Holder: can't cast object to '" +
                                    baseClass.getName() + "' of " + registry.key().location(), cx);
                }
            }
            return holder;
        }

        ResourceLocation id = ID.mc(from);
        Optional<Holder.Reference<T>> holder = registry.getHolder(id);
        return holder.isEmpty() ? DeferredHolder.create(registry.key(), id) : holder.get();
    }

    static RegistryType<?> lookupRegistryType(Context cx, TypeInfo type, Object from) {
        RegistryType<?> registryType = RegistryType.lookup(type);
        if (registryType == null) {
            throw Context.reportRuntimeError(
                    "Can't interpret '" + from + "': no registries for type '" + type + "' found", cx);
        }
        return registryType;
    }

    // copied from KubeJSContext, but without the requirement for a context (and no errors on nonexistent registries)
    private static @Nullable Registry<?> lookupRegistry(@Nullable RegistryType<?> type) {
        if (type == null) {
            return null;
        }
        // noinspection UnstableApiUsage
        return RegistryAccessContainer.current.access().registry(type.key()).orElse(null);
    }
}
