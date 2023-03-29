package com.gregtechceu.gtceu.api.capability.recipe;

/**
 * The capability can be input or output or both
 */
public enum IO {
    IN,
    OUT,
    BOTH,
    NONE;

    public boolean support(IO io) {
        if (io == this) return true;
        if (io == NONE) return false;
        return this == BOTH;
    }

}
