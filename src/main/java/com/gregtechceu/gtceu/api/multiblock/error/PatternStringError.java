package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;

import java.util.Collections;

public class PatternStringError extends PatternError {

    public final Component component;

    public PatternStringError(Component component) {
        super(null, Collections.emptyList());
        this.component = component;
    }

    @Override
    public PatternErrorUI applyErrorInformation() {
        return (parent) -> {
            parent.child(Text.of(component).asWidget());
        };
    }
}
