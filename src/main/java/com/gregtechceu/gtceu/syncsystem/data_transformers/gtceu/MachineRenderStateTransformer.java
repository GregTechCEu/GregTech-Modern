package com.gregtechceu.gtceu.syncsystem.data_transformers.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class MachineRenderStateTransformer extends ValueTransformer<MachineRenderState> {

    @Override
    public Tag serializeNBT(MachineRenderState value, ISyncManaged holder) {
        return MachineRenderState.CODEC.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public MachineRenderState deserializeNBT(Tag tag, ISyncManaged holder, @Nullable MachineRenderState currentVal) {
        return MachineRenderState.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
