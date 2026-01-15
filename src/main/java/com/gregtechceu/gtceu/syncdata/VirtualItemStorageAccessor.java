package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public class VirtualItemStorageAccessor extends CustomObjectAccessor<VirtualItemStorage> {

    public static final VirtualItemStorageAccessor INSTANCE = new VirtualItemStorageAccessor();

    protected VirtualItemStorageAccessor() {
        super(VirtualItemStorage.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, VirtualItemStorage value, Provider provider) {
        return NbtTagPayload.of(value.serializeNBT(provider));
    }

    @Override
    public VirtualItemStorage deserialize(AccessorOp op, ITypedPayload<?> payload, Provider provider) {
        var tank = new VirtualItemStorage();
        tank.deserializeNBT(provider, (CompoundTag) payload.getPayload());
        return tank;
    }
}
