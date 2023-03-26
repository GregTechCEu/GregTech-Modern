package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.integration.kjs.events.ElementEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GTCEuStartupEvents {
	EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");
	EventHandler ELEMENT = GROUP.startup("element", () -> ElementEventJS.class);
	EventHandler MATERIAL = GROUP.startup("material", () -> MaterialEventJS.class);

}
