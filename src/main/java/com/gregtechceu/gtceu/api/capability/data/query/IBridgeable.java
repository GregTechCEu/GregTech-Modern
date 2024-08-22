package com.gregtechceu.gtceu.api.capability.data.query;

public interface IBridgeable {

    /**
     * Called when a query has traversed a multiblock with more than one reception point and continued onward.
     */
    void setBridged();
}
