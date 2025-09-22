package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntegerProperty.class)
public interface IntegerPropertyAccessor {

    @Accessor("min")
    int gtceu$getMin();

    @Accessor("max")
    int gtceu$getMax();
}
