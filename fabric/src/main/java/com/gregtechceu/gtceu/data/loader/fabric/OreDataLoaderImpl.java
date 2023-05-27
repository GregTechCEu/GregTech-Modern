package com.gregtechceu.gtceu.data.loader.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.loader.OreDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class OreDataLoaderImpl extends OreDataLoader implements IdentifiableResourceReloadListener {
    public static final ResourceLocation ID = GTCEu.id("ore_veins");

    public OreDataLoaderImpl() {
        super();
        OreDataLoader.INSTANCE = this;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
