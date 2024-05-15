package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PrimedTnt.class)
public interface PrimedTntAccessor {

    @Accessor
    void setOwner(LivingEntity owner);
}
