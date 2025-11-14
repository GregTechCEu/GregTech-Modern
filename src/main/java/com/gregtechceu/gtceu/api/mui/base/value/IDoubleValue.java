package com.gregtechceu.gtceu.api.mui.base.value;

public interface IDoubleValue<T> extends IValue<T> {

    double getDoubleValue();

    void setDoubleValue(double val);

    default float getFloatValue() {
        return (float) getDoubleValue();
    }

    default void setFloatValue(float val) {
        setDoubleValue(val);
    }
}
