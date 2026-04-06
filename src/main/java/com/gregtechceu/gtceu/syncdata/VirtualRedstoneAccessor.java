package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualRedstone;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public class VirtualRedstoneAccessor extends CustomObjectAccessor<VirtualRedstone> {

    public static final VirtualRedstoneAccessor INSTANCE = new VirtualRedstoneAccessor();

    protected VirtualRedstoneAccessor() {
        super(VirtualRedstone.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, VirtualRedstone value, Provider provider) {
        return NbtTagPayload.of(value.serializeNBT(provider));
    }

    @Override
    public VirtualRedstone deserialize(AccessorOp op, ITypedPayload<?> payload, Provider provider) {
        var tank = new VirtualRedstone();
        tank.deserializeNBT(provider, (CompoundTag) payload.getPayload());
        return tank;
    }
}
