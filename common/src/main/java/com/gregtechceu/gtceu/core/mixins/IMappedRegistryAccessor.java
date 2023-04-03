package com.gregtechceu.gtceu.core.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface IMappedRegistryAccessor<T> {

    @Accessor
    int getNextId();

    @Accessor
    void setNextId(int nextId);

    @Accessor
    ObjectList<Holder.Reference<T>> getById();

    @Accessor
    Object2IntMap<T> getToId();

    @Accessor
    Map<ResourceLocation, Holder.Reference<T>> getByLocation();

    @Accessor
    Map<ResourceKey<T>, Holder.Reference<T>> getByKey();

    @Accessor
    Map<T, Holder.Reference<T>> getByValue();


}
