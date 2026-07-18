package com.gregtechceu.gtceu.core.mixins.registrate;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryEntry.class)
public interface RegistryEntryAccessor {

    @Accessor("owner")
    AbstractRegistrate<?> gtceu$getOwner();
}
