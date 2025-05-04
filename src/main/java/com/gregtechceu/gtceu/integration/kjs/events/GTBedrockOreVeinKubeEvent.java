package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.BedrockOreBuilder;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
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

public class GTBedrockOreVeinKubeEvent implements KubeEvent {

    public GTBedrockOreVeinKubeEvent() {}

    public void add(Context cx, ResourceLocation id, Consumer<BedrockOreBuilder> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_ORE_REGISTRY);

        BedrockOreBuilder builder = new BedrockOreBuilder(id);
        consumer.accept(builder);
        register(registry, id, builder.createTransformedObject());
    }

    private void register(Registry<BedrockOreDefinition> registry, ResourceLocation id, BedrockOreDefinition def) {
        if (registry instanceof WritableRegistry<BedrockOreDefinition> writable) {
            writable.register(createKey(id), def, RegistrationInfo.BUILT_IN);
        }
    }

    public void modify(Context cx, ResourceLocation id, Consumer<BedrockOreBuilder> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_ORE_REGISTRY);

        var vein = registry.get(id);
        if (vein == null) throw new IllegalArgumentException("Bedrock ore vein doesn't exist: " + id);
        var builder = BedrockOreBuilder.from(vein, id);
        consumer.accept(builder);
        register(registry, id, builder.createTransformedObject());
    }

    public void modifyAll(Context cx, BiConsumer<ResourceLocation, BedrockOreBuilder> consumer) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_ORE_REGISTRY);

        Set<ResourceLocation> keys = registry.keySet();
        keys.forEach(id -> {
            var vein = registry.get(id);
            if (vein == null) throw new IllegalArgumentException("Bedrock ore vein doesn't exist: " + id);
            var builder = BedrockOreBuilder.from(vein, id);
            consumer.accept(id, builder);
            register(registry, id, builder.createTransformedObject());
        });
    }

    public void remove(Context cx, ResourceLocation id) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_ORE_REGISTRY);
        remove(cx, registry, id);
    }

    public void removeAll(Context cx) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_ORE_REGISTRY);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(key -> remove(cx, registry, key));
    }

    public void removeAll(Context cx, BiPredicate<ResourceLocation, BedrockOreDefinition> predicate) {
        RegistryAccessContainer registries = RegistryAccessContainer.of(cx);
        var registry = registries.access().registryOrThrow(GTRegistries.BEDROCK_ORE_REGISTRY);

        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.stream()
                .filter(key -> predicate.test(key, registry.get(key)))
                .forEach(key -> remove(cx, registry, key));
    }

    private void remove(Context cx, Registry<BedrockOreDefinition> registry, ResourceLocation id) {
        if (!registry.containsKey(id)) {
            ConsoleJS.SERVER.error("", new KubeRuntimeException("Trying to remove nonexistent bedrock ore vein " + id)
                    .source(SourceLine.of(cx)));
            return;
        }
        // blank out the vein info because we can't remove from the registry
        var holder = registry.getHolderOrThrow(createKey(id));
        holder.value().biomeWeightModifier(BiomeWeightModifier.EMPTY);
        holder.value().weight(0);
    }

    public static ResourceKey<BedrockOreDefinition> createKey(ResourceLocation id) {
        return ResourceKey.create(GTRegistries.BEDROCK_ORE_REGISTRY, id);
    }
}
