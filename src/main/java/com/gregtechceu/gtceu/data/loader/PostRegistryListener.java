package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOreVeins;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public class PostRegistryListener extends ContextAwareReloadListener implements ResourceManagerReloadListener {

    public static final PostRegistryListener INSTANCE = new PostRegistryListener();

    private PostRegistryListener() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        var lookup = getRegistryLookup().lookupOrThrow(GTRegistries.Keys.ORE_VEIN);
        buildVeinGenerators(lookup);
        GTOreVeins.updateLargestVeinSize(lookup);
        ServerCache.instance.oreVeinDefinitionsChanged(lookup);
        WorldGeneratorUtils.invalidateOreVeinCache();
    }

    public static void buildVeinGenerators(HolderLookup.RegistryLookup<GTOreDefinition> lookup) {
        var iterator = lookup.listElements().iterator();
        while (iterator.hasNext()) {
            var definition = iterator.next();
            var veinGen = definition.value().veinGenerator();
            if (veinGen != null && definition.value().canGenerate()) {
                veinGen.build();
            }
        }
    }
}
