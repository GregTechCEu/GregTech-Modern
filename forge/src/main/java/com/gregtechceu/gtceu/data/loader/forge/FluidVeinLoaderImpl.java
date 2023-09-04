package com.gregtechceu.gtceu.data.loader.forge;

import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;
import net.minecraft.core.RegistryAccess;

public class FluidVeinLoaderImpl extends FluidVeinLoader {

    public FluidVeinLoaderImpl(RegistryAccess registryAccess) {
        super(registryAccess);
        FluidVeinLoader.INSTANCE = this;
    }
}
