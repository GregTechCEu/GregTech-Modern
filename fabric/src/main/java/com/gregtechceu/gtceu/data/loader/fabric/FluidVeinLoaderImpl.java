package com.gregtechceu.gtceu.data.loader.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public class FluidVeinLoaderImpl extends FluidVeinLoader implements IdentifiableResourceReloadListener {

    public static final ResourceLocation ID = GTCEu.id("fluid_veins");

    public FluidVeinLoaderImpl() {
        super();
        FluidVeinLoader.INSTANCE = this;
    }
    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return List.of(ResourceReloadListenerKeys.TAGS);
    }
}
