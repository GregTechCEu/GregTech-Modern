package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UniformGenerator.class)
public interface UniformGeneratorAccessor {

    @Invoker("<init>")
    static UniformGenerator callInit(NumberProvider min, NumberProvider max) {
        throw new AssertionError("Mixin didn't apply");
    }
}
