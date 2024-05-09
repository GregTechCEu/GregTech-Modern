package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class GTBedrockOreVeinEventJS extends EventJS {

    public GTBedrockOreVeinEventJS() {

    }

    public void add(ResourceLocation id, Consumer<BedrockOreDefinition.Builder> consumer) {
        BedrockOreDefinition.Builder builder = BedrockOreDefinition.builder(id);
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
