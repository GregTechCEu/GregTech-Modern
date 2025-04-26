package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IMappedRegistryAccess;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.BaseMappedRegistry;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> extends BaseMappedRegistry<T>
                                         implements WritableRegistry<T>, IMappedRegistryAccess<T> {

    @Shadow
    private boolean frozen;
    @Shadow
    @Final
    private ObjectList<Holder.Reference<T>> byId;
    @Shadow
    @Final
    private Reference2IntMap<T> toId;
    @Shadow
    @Final
    private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow
    @Final
    private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow
    @Final
    private Map<T, Holder.Reference<T>> byValue;
    @Shadow
    @Final
    private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;

    @Override
    public boolean gtceu$isFrozen() {
        return this.frozen;
    }

    @Override
    public ObjectList<Holder.Reference<T>> gtceu$getById() {
        return this.byId;
    }

    @Override
    public Reference2IntMap<T> gtceu$getToId() {
        return this.toId;
    }

    @Override
    public Map<ResourceLocation, Holder.Reference<T>> gtceu$getByLocation() {
        return this.byLocation;
    }

    @Override
    public Map<ResourceKey<T>, Holder.Reference<T>> gtceu$getByKey() {
        return this.byKey;
    }

    @Override
    public Map<T, Holder.Reference<T>> gtceu$getByValue() {
        return this.byValue;
    }

    @Override
    public Map<ResourceKey<T>, RegistrationInfo> gtceu$getRegistrationInfos() {
        return this.registrationInfos;
    }
}
