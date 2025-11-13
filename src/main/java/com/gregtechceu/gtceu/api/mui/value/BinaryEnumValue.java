package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IBoolValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Accepts enums which have exactly two elements. Can act as {@link IBoolValue}.
 */
public class BinaryEnumValue<T extends Enum<T>> extends EnumValue<T> implements IBoolValue<T> {

    public static <T extends Enum<T>, V extends EnumValue<T> & IBoolValue<T>> Dynamic<T> wrap(V val) {
        return new Dynamic<>(val.getEnumClass(), val::getValue, val::setValue);
    }

    public BinaryEnumValue(Class<T> enumClass, T value) {
        super(enumClass, value);
        if (enumClass.getEnumConstants().length != 2) {
            throw new IllegalArgumentException("Enum class must have exactly two elements");
        }
    }

    @Override
    public boolean getBoolValue() {
        return this.value.ordinal() == 1;
    }

    @Override
    public void setBoolValue(boolean val) {
        setValue(this.enumClass.getEnumConstants()[val ? 1 : 0]);
    }

    public static class Dynamic<T extends Enum<T>> extends EnumValue.Dynamic<T> implements IBoolValue<T> {

        public Dynamic(Class<T> enumClass, Supplier<T> getter, Consumer<T> setter) {
            super(enumClass, getter, setter);
        }

        @Override
        public boolean getBoolValue() {
            return getValue().ordinal() == 1;
        }

        @Override
        public void setBoolValue(boolean val) {
            setValue(this.enumClass.getEnumConstants()[val ? 1 : 0]);
        }
    }
}
