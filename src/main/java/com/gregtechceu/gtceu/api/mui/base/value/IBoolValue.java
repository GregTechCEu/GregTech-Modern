package com.gregtechceu.gtceu.api.mui.base.value;

public interface IBoolValue<T> extends IValue<T>, IIntValue<T> {

    boolean getBoolValue();

    void setBoolValue(boolean val);

    @Override
    default int getIntValue() {
        return getBoolValue() ? 1 : 0;
    }

    @Override
    default void setIntValue(int val) {
        setBoolValue(val == 1);
    }
}
