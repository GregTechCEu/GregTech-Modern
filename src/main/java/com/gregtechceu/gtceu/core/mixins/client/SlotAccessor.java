package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor interface for accessing protected methods from {@link Slot}.
 */
@Mixin(Slot.class)
public interface SlotAccessor {

    @Accessor
    @Mutable
    void setX(int x);

    @Accessor
    @Mutable
    void setY(int y);
}
