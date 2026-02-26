package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.BedrockFluidBuilder;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
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
public class GTBedrockFluidVeinKubeEvent implements KubeEvent {

    private final WritableRegistry<BedrockFluidDefinition> registry;

    public GTBedrockFluidVeinKubeEvent(WritableRegistry<BedrockFluidDefinition> registry) {
        this.registry = registry;
    }

    public void add(Context cx, ResourceLocation id, Consumer<BedrockFluidBuilder> consumer) {
        BedrockFluidBuilder builder = new BedrockFluidBuilder(id);
        consumer.accept(builder);
        register(id, builder.createTransformedObject());
    }

    private void register(ResourceLocation id, BedrockFluidDefinition def) {
        registry.register(createKey(id), def, RegistrationInfo.BUILT_IN);
    }

    public void modify(Context cx, ResourceLocation id, Consumer<BedrockFluidBuilder> consumer) {
        var vein = registry.get(id);
        if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
        var builder = BedrockFluidBuilder.from(vein, id);
        consumer.accept(builder);
        register(id, builder.createTransformedObject());
    }

    public void modifyAll(Context cx, BiConsumer<ResourceLocation, BedrockFluidBuilder> consumer) {
        Set<ResourceLocation> keys = Set.copyOf(registry.keySet());
        keys.forEach(id -> {
            var vein = registry.get(id);
            if (vein == null) throw new IllegalArgumentException("Fluid vein doesn't exist: " + id);
            var builder = BedrockFluidBuilder.from(vein, id);
            consumer.accept(id, builder);
            register(id, builder.createTransformedObject());
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
