package com.gregtechceu.gtceu.core.mixins.registrate;

import net.minecraftforge.common.data.LanguageProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = LanguageProvider.class, remap = false)
public interface LanguageProviderAccessor {

    @Accessor("data")
    Map<String, String> gtceu$getData();
}
