package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;

public interface IItemUIHolder extends IUIHolder<PlayerInventoryGuiData<?>> {

    default boolean shouldOpenUI() {
        return true;
    }
}
