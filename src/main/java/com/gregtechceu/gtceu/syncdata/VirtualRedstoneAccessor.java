package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualRedstone;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.nbt.CompoundTag;

public class VirtualRedstoneAccessor extends CustomObjectAccessor<VirtualRedstone> {

    public static final VirtualRedstoneAccessor INSTANCE = new VirtualRedstoneAccessor();

    protected VirtualRedstoneAccessor() {
        super(VirtualRedstone.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, VirtualRedstone value) {
        return NbtTagPayload.of(value.serializeNBT());
    }

    @Override
    public VirtualRedstone deserialize(AccessorOp op, ITypedPayload<?> payload) {
        var tank = new VirtualRedstone();
        tank.deserializeNBT((CompoundTag) payload.getPayload());
        return tank;
    }
}
