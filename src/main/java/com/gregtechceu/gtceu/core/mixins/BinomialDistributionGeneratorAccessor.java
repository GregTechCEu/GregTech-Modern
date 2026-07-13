package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BinomialDistributionGenerator.class)
public interface BinomialDistributionGeneratorAccessor {

    @Invoker("<init>")
    static BinomialDistributionGenerator callInit(NumberProvider min, NumberProvider max) {
        throw new AssertionError("Mixin didn't apply");
    }
}
