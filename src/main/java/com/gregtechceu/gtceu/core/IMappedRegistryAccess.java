package com.gregtechceu.gtceu.core;

public interface IMappedRegistryAccess {

    default boolean gtceu$isFrozen() {
        throw new AssertionError();
    }
}
