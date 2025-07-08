package com.gregtechceu.gtceu.core.mixins.registrate;

import com.google.common.collect.ListMultimap;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractRegistrate.class, remap = false)
public interface AbstractRegistrateAccessor {

    @Accessor
    ListMultimap<ProviderType<?>, @NonnullType NonNullConsumer<? extends RegistrateProvider>> getDatagens();

    @Accessor
    NonNullSupplier<Boolean> getDoDatagen();
}
