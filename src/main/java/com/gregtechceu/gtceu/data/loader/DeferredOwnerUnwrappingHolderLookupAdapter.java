package com.gregtechceu.gtceu.data.loader;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DeferredOwnerUnwrappingHolderLookupAdapter implements RegistryOps.RegistryInfoLookup {

    public final HolderLookup.Provider lookupProvider;
    private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new ConcurrentHashMap<>();

    public DeferredOwnerUnwrappingHolderLookupAdapter(HolderLookup.Provider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return (Optional<RegistryOps.RegistryInfo<E>>) this.lookups.computeIfAbsent(registryKey, this::createLookup);
    }

    // the special sauce
    private Optional<RegistryOps.RegistryInfo<Object>> createLookup(ResourceKey<? extends Registry<?>> registryKey) {
        return this.lookupProvider.lookup(registryKey).map(registryLookup -> {
            // unwrap the real holder *owner* from whatever delegates it might be buried in so using
            // RegistryFileCodec#encode works with this RegistryOps
            HolderOwner<Object> owner = registryLookup;
            while (owner instanceof HolderLookup.RegistryLookup.Delegate<Object> delegate) {
                owner = delegate.parent();
            }
            // still hand vanilla the original holder *getter* so adding entries works if this is a tag adding lookup
            return new RegistryOps.RegistryInfo<>(owner, registryLookup, registryLookup.registryLifecycle());
        });
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof DeferredOwnerUnwrappingHolderLookupAdapter holderLookupAdapter &&
                this.lookupProvider.equals(holderLookupAdapter.lookupProvider);
    }

    @Override
    public int hashCode() {
        return this.lookupProvider.hashCode();
    }
}
