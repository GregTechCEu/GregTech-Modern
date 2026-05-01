package com.lowdragmc.lowdraglib.syncdata.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;

public abstract class ObjectTypedPayload<T> implements ITypedPayload<T> {

    protected T payload;

    public ITypedPayload<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public byte getType() {
        return 0;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public void writePayload(RegistryFriendlyByteBuf buffer) {}

    @Override
    public void readPayload(RegistryFriendlyByteBuf buffer) {}
}
