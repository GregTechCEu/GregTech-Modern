package com.gregtechceu.gtceu.api.cover;

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

    Object createUIWidget();

    default void onUIClosed() {}
}
