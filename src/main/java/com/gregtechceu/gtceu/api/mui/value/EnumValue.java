package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IEnumValue;
import com.gregtechceu.gtceu.api.mui.base.value.IIntValue;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumValue<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {

    public static <T extends Enum<T>> Dynamic<T> wrap(IEnumValue<T> val) {
        return new Dynamic<>(val.getEnumClass(), val::getValue, val::setValue);
    }

    @Getter
    protected final Class<T> enumClass;
    @Getter
    @Setter
    protected T value;

    public EnumValue(Class<T> enumClass, T value) {
        this.enumClass = enumClass;
        this.value = value;
    }

    @Override
    public int getIntValue() {
        return this.value.ordinal();
    }

    @Override
    public void setIntValue(int val) {
        setValue(this.enumClass.getEnumConstants()[val]);
    }

    public static class Dynamic<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {

        @Getter
        protected final Class<T> enumClass;
        protected final Supplier<T> getter;
        protected final Consumer<T> setter;

        public Dynamic(Class<T> enumClass, Supplier<T> getter, Consumer<T> setter) {
            this.enumClass = enumClass;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int getIntValue() {
            return getValue().ordinal();
        }

        @Override
        public void setIntValue(int val) {
            setValue(this.enumClass.getEnumConstants()[val]);
        }

        @Override
        public T getValue() {
            return this.getter.get();
        }

        @Override
        public void setValue(T value) {
            this.setter.accept(value);
        }
    }
}
