package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IPaintable {

    BooleanProperty IS_PAINTED_PROPERTY = BooleanProperty.create("is_painted");

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
        return getPaintingColor() != -1 && getPaintingColor() != getDefaultPaintingColor();
    }

    /**
     * Get the real color of this block.
     */
    default int getRealColor() {
        return isPainted() ? getPaintingColor() : getDefaultPaintingColor();
    }
}
