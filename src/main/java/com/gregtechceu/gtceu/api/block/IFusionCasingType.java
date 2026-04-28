package com.gregtechceu.gtceu.api.block;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public interface IFusionCasingType extends StringRepresentable {

    /**
     * @return the {@link Identifier} defining the base texture of the coil
     */
    Identifier getTexture();

    /**
     * @return the Harvest level of this casing as an integer
     */
    int getHarvestLevel();
}
