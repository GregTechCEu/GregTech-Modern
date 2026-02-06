package com.gregtechceu.gtceu.api.mui.base;

import java.util.List;

public interface ITreeNode<T extends ITreeNode<T>> {

    T getParent();

    default boolean hasParent() {
        return getParent() != null;
    }

    List<T> getChildren();

    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }
}
