package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class PatternStringError extends PatternError {

    public final Component component;

    public PatternStringError(Component component) {
        super(null, Collections.emptyList());
        this.component = component;
    }

    @Override
    public List<Component> getErrorInfo() {
        return Collections.singletonList(component);
    }
}
