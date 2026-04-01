package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CombinedInvWrapper.class, remap = false)
public interface CombinedInvWrapperAccessor {

    @Accessor
    IItemHandlerModifiable[] getItemHandler();
}