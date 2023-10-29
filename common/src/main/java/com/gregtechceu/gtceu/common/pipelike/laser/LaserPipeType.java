package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import net.minecraft.resources.ResourceLocation;

public enum LaserPipeType implements IPipeType<LaserPipeNet.LaserData> {
    NORMAL;

    public static final ResourceLocation TYPE_ID = GTCEu.id("laser");

    @Override
    public float getThickness() {
        return 0.375f;
    }

    @Override
    public LaserPipeNet.LaserData modifyProperties(LaserPipeNet.LaserData baseProperties) {
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
