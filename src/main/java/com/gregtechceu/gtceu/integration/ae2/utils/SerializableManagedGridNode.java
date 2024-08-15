package com.gregtechceu.gtceu.integration.ae2.utils;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.IGridNodeListener;
import appeng.me.ManagedGridNode;

public class SerializableManagedGridNode extends ManagedGridNode implements ITagSerializable<CompoundTag> {

    public <T> SerializableManagedGridNode(T nodeOwner, IGridNodeListener<? super T> listener) {
        super(nodeOwner, listener);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        super.saveToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.loadFromNBT(tag);
    }
}
