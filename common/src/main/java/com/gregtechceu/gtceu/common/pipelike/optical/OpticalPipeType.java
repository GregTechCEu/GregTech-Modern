package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public enum OpticalPipeType implements IPipeType<OpticalPipeData> {
    NORMAL;

    public static final ResourceLocation TYPE_ID = GTCEu.id("optical");

    @Override
    public float getThickness() {
        return 0.375F;
    }

    @Override
    public OpticalPipeData modifyProperties(OpticalPipeData baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }
}
