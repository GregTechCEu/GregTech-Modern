package com.gregtechceu.gtceu.api.item.module;

public interface ITieredItemModule {

    int getTier();

    default void setOtherTierModules(ItemModule[] otherTierModules) {}
}
