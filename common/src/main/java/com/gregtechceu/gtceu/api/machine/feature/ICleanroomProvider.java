package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import java.util.Set;

/**
 * Implement this interface in order to make a TileEntity into a block that provides a Cleanroom to other blocks
 */
public interface ICleanroomProvider {

    /**
     * @return a {@link Set} of {@link CleanroomType} which the cleanroom provides
     */
    Set<CleanroomType> getTypes();

    /**
     * Sets the cleanroom's clean amount
     *
     * @param amount the amount of cleanliness
     */
    void setCleanAmount(int amount);

    /**
     * Adjust the cleanroom's clean amount
     *
     * @param amount the amount of cleanliness to increase/decrease by
     */
    void adjustCleanAmount(int amount);

    /**
     * @return whether the cleanroom is currently clean
     */
    boolean isClean();

    /**
     * Consumes energy from the cleanroom
     *
     * @param simulate whether to actually apply change values or not
     * @return whether the draining succeeded
     */
    boolean drainEnergy(boolean simulate);

    /**
     * @return the amount of energy input per second
     */
    long getEnergyInputPerSecond();

    /**
     * @return the tier {@link GTValues#V} of energy the cleanroom uses at minimum
     */
    int getEnergyTier();
}
