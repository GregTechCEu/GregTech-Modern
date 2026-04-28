package com.gregtechceu.gtceu.api.gui.editor;

import java.util.function.Supplier;

public final class LazyEditableMachineUI implements Supplier<Object> {

    private final Supplier<Object> supplier;
    private boolean resolved;
    private Object value;

    public LazyEditableMachineUI(Supplier<Object> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Object get() {
        if (!resolved) {
            value = supplier.get();
            resolved = true;
        }
        return value;
    }

    public static Object resolve(Object value) {
        return value instanceof LazyEditableMachineUI lazy ? lazy.get() : value;
    }
}
