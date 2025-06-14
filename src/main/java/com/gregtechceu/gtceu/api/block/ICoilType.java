package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface ICoilType {

    /**
     * @return The Unique Name of the Heating Coil
     */
    @NotNull
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
    int getEnergyDiscount();

    /**
     * This is used for the energy discount in the cracking unit and pyrolyse oven
     *
     * @return the tier of the coil
     */
    int getTier();

    /**
     * @return the {@link Material} of the Heating Coil if it has one, otherwise {@code GTMaterials.NULL}
     */
    Material getMaterial();

    /**
     * @return the {@link ResourceLocation} defining the base texture of the coil
     */
    ResourceLocation getTexture();

    Lazy<ICoilType[]> ALL_COILS_TEMPERATURE_SORTED = Lazy.of(() -> GTCEuAPI.HEATING_COILS.keySet().stream()
            .sorted(Comparator.comparingInt(ICoilType::getCoilTemperature))
            .toArray(ICoilType[]::new));

    @Nullable
    static ICoilType getMinRequiredType(int requiredTemperature) {
        return Arrays.stream(ALL_COILS_TEMPERATURE_SORTED.get())
                .filter(coil -> coil.getCoilTemperature() >= requiredTemperature)
                .findFirst().orElse(null);
    }
}
