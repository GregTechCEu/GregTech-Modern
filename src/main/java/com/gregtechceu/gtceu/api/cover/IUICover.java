package com.gregtechceu.gtceu.api.cover;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public interface IUICover {

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    default boolean isInvalid() {
        return self().coverHolder.isRemoved() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    default boolean isRemote() {
        return self().coverHolder.isRemote();
    }

    Widget createUIWidget();

    default void onUIClosed() {}
}
