package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.nbt.CompoundTag;

public class VirtualItemStorageAccessor extends CustomObjectAccessor<VirtualItemStorage> {

    public static final VirtualItemStorageAccessor INSTANCE = new VirtualItemStorageAccessor();

    protected VirtualItemStorageAccessor() {
        super(VirtualItemStorage.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, VirtualItemStorage value) {
        return NbtTagPayload.of(value.serializeNBT());
    }

    @Override
    public VirtualItemStorage deserialize(AccessorOp op, ITypedPayload<?> payload) {
        var tank = new VirtualItemStorage();
        tank.deserializeNBT((CompoundTag) payload.getPayload());
        return tank;
    }
}
