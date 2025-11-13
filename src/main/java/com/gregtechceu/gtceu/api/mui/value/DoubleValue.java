package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IDoubleValue;
import com.gregtechceu.gtceu.api.mui.base.value.IFloatValue;
import com.gregtechceu.gtceu.api.mui.base.value.IStringValue;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleValue implements IDoubleValue<Double>, IFloatValue<Double>, IStringValue<Double> {

    public static Dynamic wrap(IDoubleValue<?> val) {
        return new Dynamic(val::getDoubleValue, val::setDoubleValue);
    }

    public static Dynamic wrapAtomic(AtomicDouble val) {
        return new Dynamic(val::get, val::set);
    }

    private double value;

    public DoubleValue(double value) {
        this.value = value;
    }

    @Override
    public Double getValue() {
        return getDoubleValue();
    }

    @Override
    public void setValue(Double value) {
        setDoubleValue(value);
    }

    @Override
    public double getDoubleValue() {
        return this.value;
    }

    @Override
    public void setDoubleValue(double val) {
        this.value = val;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public void setStringValue(String val) {
        setDoubleValue(Double.parseDouble(val));
    }

    @Override
    public float getFloatValue() {
        return (float) getDoubleValue();
    }

    @Override
    public void setFloatValue(float val) {
        setDoubleValue(val);
    }

    public static class Dynamic implements IDoubleValue<Double>, IStringValue<Double> {

        private final DoubleSupplier getter;
        private final DoubleConsumer setter;

        public Dynamic(DoubleSupplier getter, DoubleConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public double getDoubleValue() {
            return this.getter.getAsDouble();
        }

        @Override
        public void setDoubleValue(double val) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getDoubleValue());
        }

        @Override
        public void setStringValue(String val) {
            setDoubleValue(Double.parseDouble(val));
        }

        @Override
        public Double getValue() {
            return getDoubleValue();
        }

        @Override
        public void setValue(Double value) {
            setDoubleValue(value);
        }
    }
}
