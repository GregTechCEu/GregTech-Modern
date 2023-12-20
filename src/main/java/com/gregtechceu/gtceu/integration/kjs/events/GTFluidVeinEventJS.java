package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.builders.FluidVeinBuilderJS;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class GTFluidVeinEventJS extends EventJS {

    public GTFluidVeinEventJS() {

    }

    public void add(ResourceLocation id, Consumer<BedrockFluidDefinition.Builder> consumer) {
        BedrockFluidDefinition.Builder builder = BedrockFluidDefinition.builder(id);
        consumer.accept(builder);
        builder.register();
    }

    public void remove(ResourceLocation id) {
        GTRegistries.BEDROCK_ORE_DEFINITIONS.remove(id);
    }

    public void modify(ResourceLocation id, Consumer<BedrockOreDefinition> consumer) {
        consumer.accept(GTRegistries.BEDROCK_ORE_DEFINITIONS.get(id));
    }
}
