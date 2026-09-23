package com.gregtechceu.gtceu.api.events;

import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.function.Supplier;

public class RegisterSpoilablesEvent extends Event implements IModBusEvent {

    private final Supplier<SpoilableBehavior.Builder> builderSupplier;

    public RegisterSpoilablesEvent(Supplier<SpoilableBehavior.Builder> builderSupplier) {
        this.builderSupplier = builderSupplier;
    }

    public SpoilableBehavior.Builder getBuilder() {
        return builderSupplier.get();
    }
}
