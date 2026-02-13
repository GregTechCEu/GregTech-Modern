package com.gregtechceu.gtceu.api.mui.base.widget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public interface IParentWidget<I extends IWidget, W extends IParentWidget<I, W>> {

    W getThis();

    boolean addChild(I child, int index);

    default W child(int index, I child) {
        if (!addChild(child, index)) {
            throw new IllegalStateException("Failed to add child");
        }
        return getThis();
    }

    default W child(I child) {
        if (!addChild(child, -1)) {
            throw new IllegalStateException("Failed to add child");
        }
        return getThis();
    }

    /**
     * @deprecated use {@link #childIf(boolean, Supplier)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default W childIf(boolean condition, I child) {
        if (condition) return child(child);
        return getThis();
    }

    default W childIf(boolean condition, Supplier<I> child) {
        if (condition) return child(child.get());
        return getThis();
    }
}
