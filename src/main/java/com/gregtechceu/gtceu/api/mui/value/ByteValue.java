package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IByteValue;

public class ByteValue implements IByteValue<Byte> {

    public static Dynamic wrap(IByteValue<Byte> val) {
        return new Dynamic(val::getByteValue, val::setByteValue);
    }

    protected byte value;

    @Override
    public void setByteValue(byte b) {
        value = b;
    }

    @Override
    public byte getByteValue() {
        return value;
    }

    @Override
    public Byte getValue() {
        return getByteValue();
    }

    @Override
    public void setValue(Byte value) {
        setByteValue(value);
    }

    public static class Dynamic extends ByteValue {

        private final Supplier getter;
        private final Consumer setter;

        public Dynamic(Supplier getter, Consumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public void setByteValue(byte b) {
            this.setter.setByte(b);
        }

        @Override
        public byte getByteValue() {
            return this.getter.getByte();
        }
    }

    public interface Supplier {

        byte getByte();
    }

    public interface Consumer {

        void setByte(byte b);
    }
}
