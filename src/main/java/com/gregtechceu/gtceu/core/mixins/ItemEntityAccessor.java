package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.entity.item.ItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {

    @Accessor
    int getPickupDelay();
}
