package com.gregtechceu.gtceu.integration.ae2.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import appeng.api.networking.IGridNodeListener;
import appeng.me.ManagedGridNode;
import org.jetbrains.annotations.NotNull;

public class SerializableManagedGridNode extends ManagedGridNode implements INBTSerializable<CompoundTag> {

    public <T> SerializableManagedGridNode(T nodeOwner, IGridNodeListener<? super T> listener) {
        super(nodeOwner, listener);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        super.saveToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        super.loadFromNBT(tag);
    }
}
