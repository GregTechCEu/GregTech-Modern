package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.resources.Identifier;

public interface IPipeType<NodeDataType> {

    /**
     * the thickness of the pipe.
     */
    float getThickness();

    /**
     * modify the node data by the pipe type.
     */
    NodeDataType modifyProperties(NodeDataType baseProperties);

    /**
     * can the pipe be painted as other color.
     */
    boolean isPaintable();

    /**
     * indicate a unique type id.
     */
    Identifier type();
}
