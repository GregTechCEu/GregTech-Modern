package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.material.MapColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapColor.class)
public interface MapColorAccessor {

    @Accessor("MATERIAL_COLORS")
    static MapColor[] gtceu$getMaterialColors() {
        throw new AssertionError("Mixin didn't apply");
    }
}
