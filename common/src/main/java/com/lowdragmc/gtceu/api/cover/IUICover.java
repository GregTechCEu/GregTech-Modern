package com.lowdragmc.gtceu.api.cover;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote IUICover
 */
public interface IUICover extends IUIHolder {

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    @Override
    default boolean isInvalid() {
        return self().coverHolder.isInValid() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    @Override
    default boolean isRemote() {
        return self().coverHolder.isRemote();
    }

    @Override
    default void markAsDirty() {
        self().coverHolder.markDirty();
    }
}
