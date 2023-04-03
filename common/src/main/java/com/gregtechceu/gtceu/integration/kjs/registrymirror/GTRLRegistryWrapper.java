package com.gregtechceu.gtceu.integration.kjs.registrymirror;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.core.mixins.IHolder$ReferenceAccessor;
import com.gregtechceu.gtceu.core.mixins.IMappedRegistryAccessor;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GTRLRegistryWrapper<T> extends MappedRegistry<T> {
    private final GTRegistry.RL<T> registry;
    private final ResourceKey<? extends Registry<T>> key;

    public GTRLRegistryWrapper(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, GTRegistry.RL<T> registry) {
        super(key, lifecycle, null);
        this.registry = registry;
        this.key = key;
        IMappedRegistryAccessor<T> accessor = (IMappedRegistryAccessor<T>) this;
        accessor.setNextId(registry.keys().size());

        var byId = registry.entries().stream().map(val -> Holder.Reference.createStandAlone(this, ResourceKey.create(key, val.getKey()))).toList();
        accessor.getById().addAll(byId);

        var byKey = registry.entries().stream().map(val -> {
            var id = ResourceKey.create(key, val.getKey());
            var value = Holder.Reference.createStandAlone(GTRLRegistryWrapper.this, id);
            return Map.entry(id, value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        accessor.getByKey().putAll(byKey);

        var byLocation = registry.entries().stream().map(val -> {
            var id = ResourceKey.create(key, val.getKey());
            var value = Holder.Reference.createStandAlone(GTRLRegistryWrapper.this, id);
            return Map.entry(id.location(), value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        accessor.getByLocation().putAll(byLocation);

        AtomicInteger index = new AtomicInteger(0);
        var toId = registry.values().stream().map(val -> Map.entry(val, index.getAndIncrement())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        accessor.getToId().putAll(toId);

        var byValue = registry.entries().stream().map(val -> {
            var id = ResourceKey.create(key, val.getKey());
            var value = Holder.Reference.createStandAlone(GTRLRegistryWrapper.this, id);
            return Map.entry(val.getValue(), value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        accessor.getByValue().putAll(byValue);
    }

    public Holder<T> register(ResourceKey<T> key, T value, Lifecycle lifecycle) {
        return this.registerMapping(((IMappedRegistryAccessor<T>)this).getNextId(), key, value, lifecycle);
    }

    @Override
    public Holder<T> registerMapping(int id, ResourceKey<T> key, T value, Lifecycle lifecycle) {
        //registry.register(key.location(), value);
        return super.registerMapping(id, key, value, lifecycle);
    }

    @Nullable
    @Override
    public ResourceLocation getKey(T value) {
        return registry.getKey(value);
    }

    @Override
    public Optional<Holder<T>> getHolder(ResourceKey<T> key) {
        ResourceKey<T> keyReal = ResourceKey.create(this.key, key.location());
        var ref = Holder.Reference.createStandAlone(this, keyReal);
        ((IHolder$ReferenceAccessor<T>)ref).invokeBind(keyReal, registry.get(key.location()));
        return Optional.of(ref);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> key) {
        return this.registry.get(key.location());
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T value) {
        return Optional.of(ResourceKey.create(this.key.location(), this.registry.getKey(value)));
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation name) {
        return Optional.ofNullable(this.registry.get(name));
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceKey<T> registryKey) {
        return Optional.ofNullable(this.registry.get(registryKey.location()));
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation name) {
        return this.registry.get(name);
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.registry.keys());
    }

    @Override
    public boolean containsKey(ResourceLocation name) {
        return registry.containKey(name);
    }

    @Override
    public Registry<T> freeze() {
        return super.freeze();
    }

    @Override
    public Iterator<T> iterator() {
        return this.registry.iterator();
    }

}
