package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTFluidVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GTCEuServerEvents {
    EventGroup GROUP = EventGroup.of("GTCEuServerEvents");

    EventHandler ORE_VEIN_MODIFICATION = GROUP.server("oreVeins", () -> GTOreVeinEventJS.class);
    EventHandler BEDROCK_ORE_VEIN_MODIFICATION = GROUP.server("bedrockOreVeins", () -> GTBedrockOreVeinEventJS.class);
    EventHandler FLUID_VEIN_MODIFICATION = GROUP.server("fluidVeins", () -> GTFluidVeinEventJS.class);
}
