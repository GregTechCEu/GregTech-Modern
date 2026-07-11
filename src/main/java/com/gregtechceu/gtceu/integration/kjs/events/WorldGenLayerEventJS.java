package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.integration.kjs.builders.WorldGenLayerBuilder;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.function.Consumer;

public class WorldGenLayerEventJS implements KubeEvent {

    @Info("Create a new material icon set with the default parent.")
    public SimpleWorldGenLayer create(ResourceLocation id, Consumer<WorldGenLayerBuilder> consumer) {
        WorldGenLayerBuilder builder = new WorldGenLayerBuilder(id);
        consumer.accept(builder);
        return builder.build();
    }
}
