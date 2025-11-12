package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IDoubleValue;
import com.gregtechceu.gtceu.api.mui.base.value.IStringValue;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleValue implements IDoubleValue<Double> {

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
