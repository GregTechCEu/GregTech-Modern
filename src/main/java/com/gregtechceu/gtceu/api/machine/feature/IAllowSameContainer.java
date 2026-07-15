package com.gregtechceu.gtceu.api.machine.feature;

/** Allows a recipe handler to restrict each item or fluid type to one slot. */
public interface IAllowSameContainer {

    boolean isAllowSame();

    void setAllowSame(boolean allowSame);
}
