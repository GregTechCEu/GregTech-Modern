package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.core.Holder;

public interface ITieredItemModule {

    int getTier();

    default void setOtherTierModules(Holder<ItemModule>[] otherTierModules) {}
}
