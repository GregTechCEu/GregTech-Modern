package com.gregtechceu.gtceu.common.data.loader;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.api.worldgen.OreVeinDefinition;
import com.gregtechceu.gtceu.api.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.data.worldgen.GTOreVeins;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NotNullByDefault
public class PostRegistryListener extends ContextAwareReloadListener implements PreparableReloadListener {

    public static final PostRegistryListener INSTANCE = new PostRegistryListener();

    private PostRegistryListener() {}

    protected void apply() {
        var registry = GTRegistries.builtinRegistry().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);

        buildVeinGenerators(registry);
        GTOreVeins.updateLargestVeinSize(registry);
        ServerCache.instance.oreVeinDefinitionsChanged(registry);
        WorldGeneratorUtils.invalidateOreVeinCache();
    }

    public static void buildVeinGenerators(Registry<OreVeinDefinition> registry) {
        var iterator = registry.holders().iterator();
        Set<Holder.Reference<OreVeinDefinition>> toRemove = new HashSet<>();
        while (iterator.hasNext()) {
            var definition = iterator.next();
            var veinGen = definition.value().veinGenerator();
            if (veinGen != null && !(veinGen instanceof NoopVeinGenerator)) {
                veinGen.build();
            } else {
                toRemove.add(definition);
            }
        }
        if (registry instanceof GTRegistry<OreVeinDefinition> registry1) {
            for (var def : toRemove) {
                registry1.remove(def);
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
