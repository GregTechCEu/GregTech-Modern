package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(MultiVariantGenerator.class)
public interface MultiVariantGeneratorAccessor {

    @Accessor("baseVariants")
    List<Variant> gtceu$getBaseVariants();
}
