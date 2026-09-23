package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.INBTSerializable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CapabilityDispatcher.class, remap = false)
public interface CapabilityDispatcherAccessor {

    @Accessor
    INBTSerializable<Tag>[] getWriters();

    @Accessor
    String[] getNames();
}
