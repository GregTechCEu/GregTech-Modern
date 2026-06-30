package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IMappedRegistryAccess;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.neoforged.neoforge.registries.BaseMappedRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnstableApiUsage")
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> extends BaseMappedRegistry<T>
                                         implements WritableRegistry<T>, IMappedRegistryAccess {

    @Shadow
    private boolean frozen;

    @Override
    public boolean gtceu$isFrozen() {
        return this.frozen;
    }
}
