package com.gregtechceu.gtceu.api.mui.value;

import com.gregtechceu.gtceu.api.mui.base.value.IValue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectValue<T> implements IValue<T> {

    public static <T> Dynamic<T> wrap(IValue<T> val) {
        return new Dynamic<>(val::getValue, val::setValue);
    }

    public static <T> Dynamic<T> wrapAtomic(AtomicReference<T> val) {
        return new Dynamic<>(val::get, val::set);
    }

    private T value;

    public ObjectValue(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    public static class Dynamic<T> implements IValue<T> {

        private final Supplier<T> getter;
        private final Consumer<T> setter;

        public Dynamic(Supplier<T> getter, Consumer<T> setter) {
            this.getter = getter;
            this.setter = setter;
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
