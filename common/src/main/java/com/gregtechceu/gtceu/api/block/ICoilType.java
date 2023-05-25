package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICoilType {

    /**
     * @return The Unique Name of the Heating Coil
     */
    @Nonnull
    String getName();

    /**
     * @return the temperature the Heating Coil provides
     */
    int getCoilTemperature();

    /**
     * This is used for the amount of parallel recipes in the multi smelter
     *
     * @return the level of the Heating Coil
     */
    int getLevel();

    /**
     * This is used for the energy discount in the multi smelter
     *
     * @return the energy discount of the Heating Coil
     */
    @SuppressWarnings("unused")
    int getEnergyDiscount();

    /**
     * This is used for the energy discount in the cracking unit and pyrolyse oven
     *
     * @return the tier of the coil
     */
    int getTier();

    /**
     * @return the {@link Material} of the Heating Coil if it has one, otherwise {@code null}
     */
    @Nullable
    Material getMaterial();

    /**
     * @return the {@link ResourceLocation} defining the base texture of the coil
     */
    ResourceLocation getTexture();
}
