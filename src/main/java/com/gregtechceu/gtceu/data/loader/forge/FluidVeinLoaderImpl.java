package com.gregtechceu.gtceu.data.loader.forge;

import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;

public class FluidVeinLoaderImpl extends FluidVeinLoader {

    public FluidVeinLoaderImpl() {
        super();
        FluidVeinLoader.INSTANCE = this;
    }
}
