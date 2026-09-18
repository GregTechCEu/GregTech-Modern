package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.Identifier;

import dev.latvian.mods.kubejs.event.EventJS;

import java.util.function.Consumer;

public class GTFluidVeinEventJS extends EventJS {

    public GTFluidVeinEventJS() {}

    public void add(Identifier id, Consumer<BedrockFluidDefinition.Builder> consumer) {
        BedrockFluidDefinition.Builder builder = BedrockFluidDefinition.builder(id);
        consumer.accept(builder);
        builder.register();
    }

    public void remove(Identifier id) {
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.remove(id);
    }

    public void modify(Identifier id, Consumer<BedrockFluidDefinition> consumer) {
        consumer.accept(GTRegistries.BEDROCK_FLUID_DEFINITIONS.get(id));
    }
}
