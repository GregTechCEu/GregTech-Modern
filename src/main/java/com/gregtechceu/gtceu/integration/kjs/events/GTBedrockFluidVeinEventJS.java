package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.BedrockFluidDefinitionBuilderJS;

import com.mojang.serialization.Lifecycle;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.rhino.Context;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GTBedrockFluidVeinEventJS extends EventJS {

    private final WritableRegistry<BedrockFluidDefinition> registry;

    public GTBedrockFluidVeinEventJS(WritableRegistry<BedrockFluidDefinition> registry) {
        this.registry = registry;
    }

    public void add(Context cx, ResourceLocation id, Consumer<BedrockFluidDefinitionBuilderJS> consumer) {
        BedrockFluidDefinitionBuilderJS builder = new BedrockFluidDefinitionBuilderJS(id);
        consumer.accept(builder);
        register(id, builder.createObject());
    }

    private void register(ResourceLocation id, BedrockFluidDefinition def) {
        registry.register(createKey(id), def, Lifecycle.stable());
    }

    public void modify(Context cx, ResourceLocation id, Consumer<BedrockFluidDefinitionBuilderJS> consumer) {
        var vein = registry.get(id);
        if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
        var builder = BedrockFluidDefinitionBuilderJS.from(vein, id);
        consumer.accept(builder);
        register(id, builder.createObject());
    }

    public void modifyAll(Context cx, BiConsumer<ResourceLocation, BedrockFluidDefinitionBuilderJS> consumer) {
        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(id -> {
            var vein = registry.get(id);
            if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
            var builder = BedrockFluidDefinitionBuilderJS.from(vein, id);
            consumer.accept(id, builder);
            register(id, builder.createObject());
        });
    }

    public void removeAll(Context cx) {
        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(key -> remove(cx, key));
    }

    public void removeAll(Context cx, BiPredicate<ResourceLocation, BedrockFluidDefinition> predicate) {
        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.stream()
                .filter(key -> predicate.test(key, registry.get(key)))
                .forEach(key -> remove(cx, key));
    }

    public void remove(Context cx, ResourceLocation id) {
        if (!registry.containsKey(id)) {
            ConsoleJS.SERVER.error("", new RuntimeException("Trying to remove nonexistent bedrock ore vein " + id));
            return;
        }
        // blank out the vein info because we can't remove from the registry
        var holder = registry.getHolderOrThrow(createKey(id));
        holder.value().setBiomeWeightModifier(BiomeWeightModifier.EMPTY);
        holder.value().setWeight(0);
    }

    public static ResourceKey<BedrockFluidDefinition> createKey(ResourceLocation id) {
        return ResourceKey.create(GTRegistries.Keys.BEDROCK_FLUID, id);
    }
}
