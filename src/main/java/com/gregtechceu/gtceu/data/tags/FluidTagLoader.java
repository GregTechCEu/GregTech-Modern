package com.gregtechceu.gtceu.data.tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.Fluid;

public class FluidTagLoader {

    @ExpectPlatform
    public static void addPlatformSpecificFluidTags(RegistrateTagsProvider<Fluid> provider) {
        throw new AssertionError();
    }
}
