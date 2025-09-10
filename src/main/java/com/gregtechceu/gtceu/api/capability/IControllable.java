package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IControllable {

    BooleanProperty WORKING_ENABLED_PROPERTY = GTMachineModelProperties.IS_WORKING_ENABLED;

    /**
     * @return true if the controllable is allowed to work
     */
    boolean isWorkingEnabled();

    /**
     * Set if the controllable can work or not
     *
     * @param isWorkingAllowed true if the workable can work, otherwise false
     */
    void setWorkingEnabled(boolean isWorkingAllowed);

    default void setSuspendAfterFinish(boolean suspendAfterFinish) {}

    default boolean isSuspendAfterFinish() {
        return false;
    }
}
