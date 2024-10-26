package com.gregtechceu.gtceu.api.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public interface IFusionCasingType extends StringRepresentable {

    /**
     * @return the {@link ResourceLocation} defining the base texture of the coil
     */
    ResourceLocation getTexture();

    /**
     * @return the Harvest level of this casing as an integer
     */
    int getHarvestLevel();
}
