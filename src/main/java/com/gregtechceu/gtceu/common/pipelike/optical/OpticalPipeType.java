package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum OpticalPipeType implements IPipeType<OpticalPipeProperties>, StringRepresentable {

    NORMAL;

    public static final Identifier TYPE = GTCEu.id("optical");

    @Override
    public float getThickness() {
        return 0.375F;
    }

    @Override
    public OpticalPipeProperties modifyProperties(OpticalPipeProperties baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public Identifier type() {
        return TYPE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
