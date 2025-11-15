package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IDoubleValue;
import com.gregtechceu.gtceu.api.mui.base.value.IFloatValue;
import com.gregtechceu.gtceu.api.mui.base.value.IStringValue;
import com.gregtechceu.gtceu.utils.FloatConsumer;
import com.gregtechceu.gtceu.utils.FloatSupplier;

import com.google.common.util.concurrent.AtomicDouble;

public class FloatValue implements IFloatValue<Float>, IDoubleValue<Float>, IStringValue<Float> {

    public static Dynamic wrap(IFloatValue<?> val) {
        return new Dynamic(val::getFloatValue, val::setFloatValue);
    }

    public static Dynamic wrapAtomic(AtomicDouble val) {
        return new Dynamic(val::floatValue, val::set);
    }

    private float value;

    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public Float getValue() {
        return getFloatValue();
    }

    @Override
    public float getFloatValue() {
        return value;
    }

    @Override
    public void setFloatValue(float val) {
        this.value = val;
    }

    @Override
    public void setValue(Float value) {
        setDoubleValue(value);
    }

    @Override
    public double getDoubleValue() {
        return getFloatValue();
    }

    @Override
    public void setDoubleValue(double val) {
        setFloatValue((float) val);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(getFloatValue());
    }

    @Override
    public void setStringValue(String val) {
        setFloatValue(Float.parseFloat(val));
    }

    public static class Dynamic implements IFloatValue<Float>, IDoubleValue<Float>, IStringValue<Float> {

        private final FloatSupplier getter;
        private final FloatConsumer setter;

        public Dynamic(FloatSupplier getter, FloatConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public float getFloatValue() {
            return this.getter.getAsFloat();
        }

        @Override
        public void setFloatValue(float val) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getFloatValue());
        }

        @Override
        public void setStringValue(String val) {
            setDoubleValue(Double.parseDouble(val));
        }

        @Override
        public Float getValue() {
            return getFloatValue();
        }

        @Override
        public void setValue(Float value) {
            setFloatValue(value);
        }

        @Override
        public double getDoubleValue() {
            return getFloatValue();
        }

        @Override
        public void setDoubleValue(double val) {
            setFloatValue((float) val);
        }
    }
}
