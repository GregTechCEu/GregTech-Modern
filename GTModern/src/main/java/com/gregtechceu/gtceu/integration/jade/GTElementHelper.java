package com.gregtechceu.gtceu.integration.jade;

import net.minecraft.world.phys.Vec2;

import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public class GTElementHelper {

    public static final Vec2 SMALL_FLUID_SIZE = new Vec2(10.0F, 10.0F);
    public static final Vec2 SMALL_FLUID_OFFSET = new Vec2(0.0F, -1.0F);

    public static IElement smallFluid(JadeFluidObject fluid) {
        return helper().fluid(fluid).size(SMALL_FLUID_SIZE).translate(SMALL_FLUID_OFFSET).message(null);
    }

    public static IElementHelper helper() {
        return IElementHelper.get();
    }
}
