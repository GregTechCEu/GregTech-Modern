package com.gregtechceu.gtceu.data.loader.forge;

import com.gregtechceu.gtceu.data.loader.OreDataLoader;
import net.minecraft.core.RegistryAccess;

public class OreDataLoaderImpl extends OreDataLoader {
    public OreDataLoaderImpl() {
        super();
        OreDataLoader.INSTANCE = this;
    }
}
