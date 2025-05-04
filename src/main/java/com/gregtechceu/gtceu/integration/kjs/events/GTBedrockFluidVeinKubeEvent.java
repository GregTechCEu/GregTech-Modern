package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.BedrockFluidBuilder;

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
public class GTBedrockFluidVeinKubeEvent implements KubeEvent {

    public GTBedrockFluidVeinKubeEvent() {}

    public void add(Context cx, ResourceLocation id, Consumer<BedrockFluidBuilder> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);
        var biomes = registries.access().lookupOrThrow(Registries.BIOME);

        BedrockFluidBuilder builder = new BedrockFluidBuilder(id);
        consumer.accept(builder);
        register(registry, id, builder.createTransformedObject());
    }

    private void register(Registry<BedrockFluidDefinition> registry, ResourceLocation id, BedrockFluidDefinition def) {
        if (registry instanceof WritableRegistry<BedrockFluidDefinition> writable) {
            writable.register(createKey(id), def, RegistrationInfo.BUILT_IN);
        }
    }

    public void modify(Context cx, ResourceLocation id, Consumer<BedrockFluidBuilder> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);
        var biomes = registries.access().lookupOrThrow(Registries.BIOME);

        var vein = registry.get(id);
        if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
        var builder = BedrockFluidBuilder.from(vein, id);
        consumer.accept(builder);
        register(registry, id, builder.createTransformedObject());
    }

    public void modifyAll(Context cx, BiConsumer<ResourceLocation, BedrockFluidBuilder> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);
        var biomes = registries.access().lookupOrThrow(Registries.BIOME);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(id -> {
            var vein = registry.get(id);
            if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
            var builder = BedrockFluidBuilder.from(vein, id);
            consumer.accept(id, builder);
            register(registry, id, builder.createTransformedObject());
        });
    }

    public void remove(Context cx, ResourceLocation id) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);
        remove(cx, registry, id);
    }

    public void removeAll(Context cx) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(key -> remove(cx, registry, key));
    }

    public void removeAll(Context cx, BiPredicate<ResourceLocation, BedrockFluidDefinition> predicate) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.stream()
                .filter(key -> predicate.test(key, registry.get(key)))
                .forEach(key -> remove(cx, registry, key));
    }

    private void remove(Context cx, Registry<BedrockFluidDefinition> registry, ResourceLocation id) {
        if (!registry.containsKey(id)) {
            ConsoleJS.SERVER.error("", new KubeRuntimeException("Trying to remove nonexistent bedrock ore vein " + id)
                    .source(SourceLine.of(cx)));
            return;
        }
        // blank out the vein info because we can't remove from the registry
        var holder = registry.getHolderOrThrow(createKey(id));
        holder.value().setBiomeWeightModifier(BiomeWeightModifier.EMPTY);
        holder.value().setWeight(0);
    }

    public static ResourceKey<BedrockFluidDefinition> createKey(ResourceLocation id) {
        return ResourceKey.create(GTRegistries.BEDROCK_FLUID_REGISTRY, id);
    }
}
