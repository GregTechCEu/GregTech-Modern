package com.gregtechceu.gtceu.syncdata.data_transformers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class MachineRenderStateTransformer implements IValueTransformer<MachineRenderState> {

    @Override
    public Tag serializeNBT(MachineRenderState value, boolean isSync, boolean isFullSync) {
        return MachineRenderState.CODEC.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public MachineRenderState deserializeNBT(Tag tag, @Nullable MachineRenderState currentVal, boolean isSync) {
        return MachineRenderState.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
