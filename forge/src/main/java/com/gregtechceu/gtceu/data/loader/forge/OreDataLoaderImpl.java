package com.gregtechceu.gtceu.data.loader.forge;

import com.gregtechceu.gtceu.data.loader.OreDataLoader;
import net.minecraft.core.RegistryAccess;

public class OreDataLoaderImpl extends OreDataLoader {
    public OreDataLoaderImpl(RegistryAccess registryAccess) {
        super(registryAccess);
        OreDataLoader.INSTANCE = this;
    }
}
