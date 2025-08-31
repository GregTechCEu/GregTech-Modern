package com.gregtechceu.gtceu.api.mui.drawable.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
import java.util.function.Supplier;

public class DynamicKey extends BaseKey {

    private final Supplier<MutableComponent> supplier;

    public DynamicKey(Supplier<Component> supplier) {
        Objects.requireNonNull(supplier.get(), "IKey returns a null string!");
        this.supplier = () -> {
            Component c = supplier.get();
            if (c instanceof MutableComponent m) {
                return m;
            } else {
                return c.copy();
            }
        };
    }

    @Override
    public MutableComponent get() {
        return this.supplier.get();
    }
}
