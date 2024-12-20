package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class MachineOwnerPayload extends ObjectTypedPayload<IMachineOwner> {

    @Override
    public @Nullable Tag serializeNBT() {
        return payload.write();
    }

    @Override
    public void deserializeNBT(Tag tag) {
        payload = IMachineOwner.create((CompoundTag) tag);
    }
}
