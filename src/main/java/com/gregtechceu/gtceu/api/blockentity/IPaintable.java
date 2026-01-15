package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IPaintable {

    BooleanProperty IS_PAINTED_PROPERTY = GTMachineModelProperties.IS_PAINTED;
    int UNPAINTED_COLOR = 0xffffffff;

    /**
     * Get painting color.
     * It's not the real color of this block.
     * 
     * @return -1 - non painted.
     */
    int getPaintingColor();

    void setPaintingColor(int color);

    /**
     * Default color.
     */
    int getDefaultPaintingColor();

    /**
     * If the block is painted.
     */
    default boolean isPainted() {
        return getPaintingColor() != UNPAINTED_COLOR && getPaintingColor() != getDefaultPaintingColor();
    }

    /**
     * Get the real color of this block.
     */
    default int getRealColor() {
        return isPainted() ? getPaintingColor() : getDefaultPaintingColor();
    }
}
