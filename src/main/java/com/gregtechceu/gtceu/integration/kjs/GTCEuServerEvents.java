package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockFluidVeinKubeEvent;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinKubeEvent;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinKubeEvent;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GTCEuServerEvents {

    EventGroup GROUP = EventGroup.of("GTCEuServerEvents");

    EventHandler ORE_VEIN_MODIFICATION = GROUP.server("oreVeins", () -> GTOreVeinKubeEvent.class);
    EventHandler FLUID_VEIN_MODIFICATION = GROUP.server("fluidVeins", () -> GTBedrockFluidVeinKubeEvent.class);
    EventHandler BEDROCK_ORE_VEIN_MODIFICATION = GROUP.server("bedrockOreVeins", () -> GTBedrockOreVeinKubeEvent.class);
}
