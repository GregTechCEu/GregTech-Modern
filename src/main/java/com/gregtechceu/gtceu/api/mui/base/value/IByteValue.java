package com.gregtechceu.gtceu.api.mui.base.value;

public interface IByteValue<T> extends IIntValue<T>, IStringValue<T> {

    @Override
    default void setIntValue(int val) {
        setByteValue((byte) val);
    }

    @Override
    default void setStringValue(String val) {
        setByteValue(Byte.parseByte(val));
    }

    @Override
    default int getIntValue() {
        return getByteValue();
    }

    @Override
    default String getStringValue() {
        return String.valueOf(getByteValue());
    }

    void setByteValue(byte b);

    byte getByteValue();
}
