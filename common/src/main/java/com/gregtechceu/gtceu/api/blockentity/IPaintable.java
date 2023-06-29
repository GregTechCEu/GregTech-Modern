package com.gregtechceu.gtceu.api.blockentity;

/**
 * @author screret
 * @date 6/28/2023
 * @implNote Implement on paintable block entities
 */
public interface IPaintable {

    int getPaintingColor();

    void setPaintingColor(int color);

    boolean isPainted();

    int getDefaultPaintingColor();
}
