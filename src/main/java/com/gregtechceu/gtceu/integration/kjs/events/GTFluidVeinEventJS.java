package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.FluidVeinBuilderJS;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventJS;

import java.util.function.Consumer;

public class GTFluidVeinEventJS extends EventJS {

    public GTFluidVeinEventJS() {}

    public void add(ResourceLocation id, Consumer<FluidVeinBuilderJS> consumer) {
        FluidVeinBuilderJS builder = new FluidVeinBuilderJS(id);
        consumer.accept(builder);
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.register(id, builder.build());
    }

    public void remove(ResourceLocation id) {
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.remove(id);
    }

    public void modify(ResourceLocation id, Consumer<BedrockFluidDefinition> consumer) {
        consumer.accept(GTRegistries.BEDROCK_FLUID_DEFINITIONS.get(id));
    }
}
