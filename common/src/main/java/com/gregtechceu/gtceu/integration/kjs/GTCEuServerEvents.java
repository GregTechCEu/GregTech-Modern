package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.GTRecipeEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GTCEuServerEvents {
	EventGroup GROUP = EventGroup.of("GTCEuServerEvents");
	EventHandler RECIPE = GROUP.server("recipe", () -> GTRecipeEventJS.class);

}
