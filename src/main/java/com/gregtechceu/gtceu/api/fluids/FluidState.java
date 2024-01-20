package com.gregtechceu.gtceu.api.fluids;

import lombok.Getter;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum FluidState implements StringRepresentable {

    LIQUID("gtceu.fluid.state_liquid"),
    GAS("gtceu.fluid.state_gas"),
    PLASMA("gtceu.fluid.state_plasma"),
    ;

    @Getter
    private final String translationKey;

    FluidState(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
