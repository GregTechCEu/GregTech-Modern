package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IValue;

import lombok.Getter;
import lombok.Setter;

/**
 * @deprecated use {@link ObjectValue} instead
 */
@Deprecated
public class ConstValue<T> implements IValue<T> {

    @Getter
    @Setter
    protected T value;

    public ConstValue(T value) {
        this.value = value;
    }

    @Override
    public Class<T> getValueType() {
        return (Class<T>) value.getClass();
    }
}
