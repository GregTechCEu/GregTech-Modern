package com.gregtechceu.gtceu.common.pipelike.item;

/**
 * The set of {@link ItemRoutePath} connections to fetch from an {@link ItemPipeNet}: All, Non-Restrictive, or Only
 * Restrictive
 */
public enum ItemRoutePathSet {

    /**
     * the full set of item pipe net routes
     */
    FULL,
    /**
     * only the subset of item pipe net routes that include at least one Restrictive Pipe
     */
    RESTRICTED,
    /**
     * only the subset of item pipe net routes that do not include at least one Restrictive Pipe
     */
    NONRESTRICTED;

    ItemRoutePathSet() {}
}
