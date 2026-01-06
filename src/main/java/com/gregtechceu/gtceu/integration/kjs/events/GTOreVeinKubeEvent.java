package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.worldgen.OreVeinDefinition;
import com.gregtechceu.gtceu.api.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.OreVeinDefinitionBuilder;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GTOreVeinKubeEvent implements KubeEvent {

    private final WritableRegistry<OreVeinDefinition> registry;
    private final RegistryAccess registries;

    public GTOreVeinKubeEvent(WritableRegistry<OreVeinDefinition> registry,
                              RegistryAccess registries) {
        this.registry = registry;
        this.registries = registries;
    }

    public void add(Context cx, ResourceLocation id, Consumer<OreVeinDefinitionBuilder> consumer) {
        OreVeinDefinitionBuilder builder = new OreVeinDefinitionBuilder(id);
        consumer.accept(builder);
        register(id, builder.createTransformedObject());
    }

    private void register(ResourceLocation id, OreVeinDefinition def) {
        this.registry.register(createKey(id), def, RegistrationInfo.BUILT_IN);
    }

    public void modify(Context cx, ResourceLocation id, Consumer<OreVeinDefinitionBuilder> consumer) {
        OreVeinDefinition vein = this.registry.get(id);
        if (vein == null) throw new IllegalArgumentException("Ore vein doesn't exist: " + id);

        OreVeinDefinitionBuilder builder = OreVeinDefinitionBuilder.from(vein, id);
        consumer.accept(builder);
        register(id, builder.createTransformedObject());
    }

    public void modifyAll(Context cx, BiConsumer<ResourceLocation, OreVeinDefinition> consumer) {
        Set<ResourceLocation> keys = this.registry.keySet();
        keys.forEach(id -> {
            OreVeinDefinition vein = this.registry.get(id);
            if (vein == null) throw new IllegalArgumentException("Ore vein doesn't exist: " + id);
            consumer.accept(id, vein);
        });
    }

    private void remove(Context cx, ResourceLocation id) {
        if (!this.registry.containsKey(id)) {
            ConsoleJS.SERVER.error("", new KubeRuntimeException("Trying to remove nonexistent bedrock ore vein " + id)
                    .source(SourceLine.of(cx)));
            return;
        }
        // blank out the vein info because we can't remove from the registry
        OreVeinDefinition vein = this.registry.getOrThrow(createKey(id));
        vein.veinGenerator(NoopVeinGenerator.INSTANCE);
        vein.biomeWeightModifier(BiomeWeightModifier.EMPTY);
        vein.weight(0);
    }

    public void removeAll(Context cx) {
        Set<ResourceLocation> keys = Set.copyOf(this.registry.keySet());
        keys.forEach(key -> remove(cx, key));
    }

    public void removeAll(Context cx, BiPredicate<ResourceLocation, OreVeinDefinition> predicate) {
        Set<ResourceLocation> keys = Set.copyOf(this.registry.keySet());
        keys.stream()
                .filter(key -> predicate.test(key, registry.get(key)))
                .forEach(key -> remove(cx, key));
    }

    public static ResourceKey<OreVeinDefinition> createKey(ResourceLocation id) {
        return ResourceKey.create(GTRegistries.ORE_VEIN_REGISTRY, id);
    }
}
