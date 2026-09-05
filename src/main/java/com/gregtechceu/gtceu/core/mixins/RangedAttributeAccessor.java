package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RangedAttribute.class)
public interface RangedAttributeAccessor {

    @Accessor("maxValue")
    @Mutable
    void gtceu$setMaxValue(double maxValue);
}
