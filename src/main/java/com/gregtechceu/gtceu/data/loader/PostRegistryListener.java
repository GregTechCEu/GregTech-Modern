package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOreVeins;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NotNullByDefault
public class PostRegistryListener extends ContextAwareReloadListener implements PreparableReloadListener {

    public static final PostRegistryListener INSTANCE = new PostRegistryListener();

    private PostRegistryListener() {}

    protected void apply() {
        HolderLookup.RegistryLookup<GTOreDefinition> holderLookup = getRegistryLookup()
                .lookupOrThrow(GTRegistries.Keys.ORE_VEIN);

        buildVeinGenerators(holderLookup);
        GTOreVeins.updateLargestVeinSize(holderLookup);
        ServerCache.instance.oreVeinDefinitionsChanged(holderLookup);
        WorldGeneratorUtils.invalidateOreVeinCache();
    }

    public static void buildVeinGenerators(HolderLookup.RegistryLookup<GTOreDefinition> holderLookup) {
        var iterator = holderLookup.listElements().iterator();
        while (iterator.hasNext()) {
            var definition = iterator.next();
            var veinGen = definition.value().veinGenerator();
            if (veinGen != null && definition.value().canGenerate()) {
                veinGen.build();
            }
        }
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return stage.wait(null).thenRunAsync(this::apply);
    }
}
