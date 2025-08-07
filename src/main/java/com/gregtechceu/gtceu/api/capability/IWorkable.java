package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * For machines which have progress and can work
 */
public interface IWorkable extends IControllable {

    BooleanProperty ACTIVE_PROPERTY = GTMachineModelProperties.IS_ACTIVE;

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
