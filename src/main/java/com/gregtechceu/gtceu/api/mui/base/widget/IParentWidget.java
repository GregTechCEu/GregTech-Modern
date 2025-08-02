package com.gregtechceu.gtceu.api.mui.base.widget;

import java.util.function.BooleanSupplier;
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

    default W childIf(boolean condition, I child) {
        if (condition) return child(child);
        return getThis();
    }

    default W childIf(BooleanSupplier condition, I child) {
        if (condition.getAsBoolean()) return child(child);
        return getThis();
    }

    default W childIf(boolean condition, Supplier<I> child) {
        if (condition) return child(child.get());
        return getThis();
    }

    default W childIf(BooleanSupplier condition, Supplier<I> child) {
        if (condition.getAsBoolean()) return child(child.get());
        return getThis();
    }
}
