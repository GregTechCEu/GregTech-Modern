package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IValue;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @deprecated use {@link ObjectValue.Dynamic} instead
 */
@Deprecated
public class DynamicValue<T> implements IValue<T> {

    private final Supplier<T> getter;
    @Nullable
    private final Consumer<T> setter;

    public DynamicValue(Supplier<T> getter, @Nullable Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T getValue() {
        return this.getter.get();
    }

    @Override
    public void setValue(T value) {
        if (this.setter != null) {
            this.setter.accept(value);
        }
    }
}
