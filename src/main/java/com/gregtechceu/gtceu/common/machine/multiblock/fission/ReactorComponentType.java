package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import java.util.Locale;

public enum ReactorComponentType {

    FUEL_ROD,
    COOLANT_CHANNEL,
    HEAT_EXCHANGER,
    NEUTRON_REFLECTOR,
    MODERATOR,
    CONTROL_ROD,
    CASING,
    CONTROLLER,
    VESSEL;

    private final String langKey;

    ReactorComponentType() {
        this.langKey = "gtceu.multiblock.fission.component." + name().toLowerCase(Locale.ROOT);
    }

    public String getLangKey() {
        return langKey;
    }
}
