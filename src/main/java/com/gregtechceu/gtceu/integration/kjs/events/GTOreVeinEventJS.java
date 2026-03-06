package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOreVeins;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.rhino.Context;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GTOreVeinEventJS implements KubeEvent {

    private final WritableRegistry<GTOreDefinition> registry;

    public GTOreVeinEventJS(WritableRegistry<GTOreDefinition> registry) {
        this.registry = registry;
    }

    public void add(Context cx, ResourceLocation id, Consumer<GTOreDefinition> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var biomes = registries.access().lookupOrThrow(Registries.BIOME);

        GTOreDefinition vein = GTOreVeins.blankOreDefinition(biomes);
        consumer.accept(vein);
        register(id, vein);
    }

    private void register(ResourceLocation id, GTOreDefinition def) {
        registry.register(createKey(id), def, RegistrationInfo.BUILT_IN);
    }

    public void modify(Context cx, ResourceLocation id, Consumer<GTOreDefinition> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        var biomes = registries.access().lookupOrThrow(Registries.BIOME);

        var vein = registry.get(id);
        if (vein == null) throw new IllegalArgumentException("Ore vein doesn't exist: " + id);

        vein.biomeLookup(biomes);
        consumer.accept(vein);
    }

    public void modifyAll(Context cx, BiConsumer<ResourceLocation, GTOreDefinition> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        var biomes = registries.access().lookupOrThrow(Registries.BIOME);

        Set<ResourceLocation> keys = registry.keySet();
        keys.forEach(id -> {
            var vein = registry.get(id);
            if (vein == null) throw new IllegalArgumentException("Ore vein doesn't exist: " + id);
            vein.biomeLookup(biomes);
            consumer.accept(id, vein);
        });
    }

    public void remove(Context cx, ResourceLocation id) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        remove(cx, registry, id);
    }

    public void removeAll(Context cx) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(key -> remove(cx, registry, key));
    }

    public void removeAll(Context cx, BiPredicate<ResourceLocation, GTOreDefinition> predicate) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.stream()
                .filter(key -> predicate.test(key, registry.get(key)))
                .forEach(key -> remove(cx, registry, key));
    }

    private void remove(Context cx, Registry<GTOreDefinition> registry, ResourceLocation id) {
        if (!registry.containsKey(id)) {
            ConsoleJS.SERVER.error("", new KubeRuntimeException("Trying to remove nonexistent bedrock ore vein " + id)
                    .source(SourceLine.of(cx)));
            return;
        }
        // blank out the vein info because we can't remove from the registry
        var holder = registry.getHolderOrThrow(GTOreVeins.create(id));
        holder.value().veinGenerator(NoopVeinGenerator.INSTANCE);
        holder.value().biomeWeightModifier(BiomeWeightModifier.EMPTY);
        holder.value().weight(0);
    }

    public static ResourceKey<GTOreDefinition> createKey(ResourceLocation id) {
        return ResourceKey.create(GTRegistries.ORE_VEIN_REGISTRY, id);
    }
}
