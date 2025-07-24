package com.gregtechceu.gtceu.api.capability;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * For machines which have progress and can work
 */
public interface IWorkable extends IControllable {

    BooleanProperty ACTIVE_PROPERTY = BooleanProperty.create("active");

    /**
     * @return current progress of machine
     */
    int getProgress();

    /**
     * @return progress machine need to complete it's stuff
     */
    int getMaxProgress();

    /**
     * @return true is machine is active
     */
    boolean isActive();
}
