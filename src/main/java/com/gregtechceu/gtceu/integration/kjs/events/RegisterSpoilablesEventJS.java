package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.events.RegisterSpoilablesEvent;
import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;

import dev.latvian.mods.kubejs.event.EventJS;

public class RegisterSpoilablesEventJS extends EventJS {

    private final RegisterSpoilablesEvent event;

    public RegisterSpoilablesEventJS(RegisterSpoilablesEvent event) {
        this.event = event;
    }

    public SpoilableBehavior.Builder getBuilder() {
        return this.event.getBuilder();
    }
}
