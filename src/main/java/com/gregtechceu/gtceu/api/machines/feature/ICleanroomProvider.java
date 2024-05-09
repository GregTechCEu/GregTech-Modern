package com.gregtechceu.gtceu.api.machines.feature;

import com.gregtechceu.gtceu.api.machines.multiblock.CleanroomType;

import java.util.Set;

/**
 * Implement this interface in order to make a Machine into a block that provides a Cleanroom to other blocks
 */
public interface ICleanroomProvider extends IMachineFeature {

    /**
     * @return a {@link Set} of {@link CleanroomType} which the cleanroom provides
     */
    Set<CleanroomType> getTypes();

    /**
     * @return whether the cleanroom is currently clean
     */
    boolean isClean();

}
