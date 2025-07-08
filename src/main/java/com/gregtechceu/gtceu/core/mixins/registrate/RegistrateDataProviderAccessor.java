package com.gregtechceu.gtceu.core.mixins.registrate;

import com.google.common.collect.BiMap;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RegistrateDataProvider.class, remap = false)
public interface RegistrateDataProviderAccessor {

    @Accessor("TYPES")
    static BiMap<String, ProviderType<?>> gtceu$getTypes() {
        throw new AssertionError();
    }
}
