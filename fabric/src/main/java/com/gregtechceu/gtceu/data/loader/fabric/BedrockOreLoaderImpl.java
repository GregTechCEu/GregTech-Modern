package com.gregtechceu.gtceu.data.loader.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.loader.BedrockOreLoader;
import com.gregtechceu.gtceu.data.loader.FluidVeinLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public class BedrockOreLoaderImpl extends BedrockOreLoader implements IdentifiableResourceReloadListener {

    public static final ResourceLocation ID = GTCEu.id("bedrock_ore_veins");

    public BedrockOreLoaderImpl() {
        super();
        BedrockOreLoader.INSTANCE = this;
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
