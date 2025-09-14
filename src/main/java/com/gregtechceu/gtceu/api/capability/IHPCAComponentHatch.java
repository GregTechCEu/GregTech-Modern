package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IHPCAComponentHatch {

    BooleanProperty HPCA_PART_DAMAGED_PROPERTY = GTMachineModelProperties.IS_HPCA_PART_DAMAGED;

    /**
     * How much EU/t this component needs for the multi to just be idle.
     * Used in 2 ways:
     * - "Non-computational" units like HPCA Bridge, Active Cooler
     * - "Computational base cost" for units like HPCA Computation, High Computation
     */
    int getUpkeepEUt();

    /**
     * How much EU/t this component can use, if it is being utilized fully.
     * Used to scale cost for "computational" units. Power draw is a range
     * created by actual computation used vs maximum potential computation.
     */
    default int getMaxEUt() {
        return getUpkeepEUt();
    }

    /**
     * If this component can be damaged by HPCA overheat.
     */
    boolean canBeDamaged();

    /**
     * If this component is currently damaged by HPCA overheat.
     */
    default boolean isDamaged() {
        return false;
    }

    /**
     * Set this component as damaged (or undamaged).
     */
    default void setDamaged(boolean damaged) {}

    /**
     * If this component allows for bridging HPCAs to Network Switches.
     */
    boolean isBridge();

    /**
     * The icon for this component in the HPCA's UI. Should be a 13x13 px sprite.
     */
    ResourceTexture getComponentIcon();

    /**
     * Sets the component to be active for the sake of model overlays.
     */
    void setActive(boolean active);
}
