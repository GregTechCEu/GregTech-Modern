package com.gregtechceu.gtceu.api.blockentity;

/**
 * @author screret
 * @date 6/28/2023
 * @implNote Implement on paintable block entities
 */
public interface IPaintable {

    /**
     * Get painting color.
     * It's not the real color of this block.
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
