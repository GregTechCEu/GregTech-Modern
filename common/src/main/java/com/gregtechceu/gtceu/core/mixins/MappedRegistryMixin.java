/*
package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IMappedRegistryMixin;
import com.gregtechceu.gtceu.core.mixins.IHolderReferenceAccessor;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> extends WritableRegistry<T> implements IMappedRegistryMixin<T> {

    @Shadow
    private void validateWrite(ResourceKey<T> key) {
        throw new NotImplementedException("Mixin failed to apply");
    }
    @Final
    @Shadow
    private ObjectList<Holder.Reference<T>> byId;
    @Final
    @Shadow
    private Object2IntMap<T> toId;
    @Final
    @Shadow
    private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Final
    @Shadow
    private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Final
    @Shadow
    private Map<T, Holder.Reference<T>> byValue;
    @Final
    @Shadow
    private Map<T, Lifecycle> lifecycles;
    @Shadow
    private Lifecycle elementsLifecycle;
    @Final
    @Shadow
    private Function<T, Holder.Reference<T>> customHolderProvider;
    @Shadow
    private List<Holder.Reference<T>> holdersInOrder;
    @Shadow
    private int nextId;

    @Override
    public Holder<T> registerMappingPushValues(int id, ResourceKey<T> key, T value, Lifecycle lifecycle, boolean logDuplicateKeys) {
        this.validateWrite(key);
        Validate.notNull(key);
        Validate.notNull(value);
        this.byId.size(Math.max(this.byId.size(), id + 1));
        this.toId.put(value, id);
        this.holdersInOrder = null;
        if (logDuplicateKeys && this.byKey.containsKey(key)) {
            Util.logAndPauseIfInIde("Adding duplicate key '" + key + "' to registry");
        }

        if (this.byValue.containsKey(value)) {
            Util.logAndPauseIfInIde("Adding duplicate value '" + value + "' to registry");
        }

        this.lifecycles.put(value, lifecycle);
        this.elementsLifecycle = this.elementsLifecycle.add(lifecycle);
        if (this.nextId <= id) {
            this.nextId = id + 1;
        }

        Holder.Reference<T> reference;
        if (this.customHolderProvider != null) {
            reference = this.customHolderProvider.apply(value);
            Holder.Reference<T> reference2 = this.byKey.put(key, reference);
            if (reference2 != null && reference2 != reference) {
                throw new IllegalStateException("Invalid holder present for key " + key);
            }
        } else {
            reference = this.byKey.computeIfAbsent(key, (registryKey) -> Holder.Reference.createStandAlone(this, registryKey));
        }

        this.byLocation.put(key.location(), reference);
        this.byValue.put(value, reference);
        ((IHolderReferenceAccessor<T>)reference).invokeBind(key, value);
        this.byId.add(id, reference);
        return reference;
    }

    public MappedRegistryMixin(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
        super(resourceKey, lifecycle);
    }
}
*/