package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.util.datafix.DataFixTypes;

import com.mojang.datafixers.DSL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataFixTypes.class)
public interface DataFixTypesAccessor {

    @Accessor
    DSL.TypeReference getType();
}
