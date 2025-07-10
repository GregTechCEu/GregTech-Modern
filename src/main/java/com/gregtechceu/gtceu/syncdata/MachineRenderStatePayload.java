package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class MachineRenderStatePayload extends ObjectTypedPayload<MachineRenderState> {

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        buf.writeId(MachineDefinition.RENDER_STATE_REGISTRY, payload);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        payload = buf.readById(MachineDefinition.RENDER_STATE_REGISTRY);
    }

    @Nullable
    @Override
    public Tag serializeNBT() {
        return MachineRenderState.CODEC.encodeStart(NbtOps.INSTANCE, payload)
                .getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public void deserializeNBT(Tag tag) {
        payload = MachineRenderState.CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, GTCEu.LOGGER::error);
    }
}
