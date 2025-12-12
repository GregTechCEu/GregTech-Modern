package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class MachineRenderStatePayload extends ObjectTypedPayload<MachineRenderState> {

    @Override
    public void writePayload(RegistryFriendlyByteBuf buf) {
        // Ensure we're using the interned state for registry lookup
        if (payload != null) {
            payload = payload.intern();
        }
        buf.writeById(MachineDefinition.RENDER_STATE_REGISTRY::getId, payload);
    }

    @Override
    public void readPayload(RegistryFriendlyByteBuf buf) {
        payload = buf.readById(MachineDefinition.RENDER_STATE_REGISTRY::byId);
    }

    @Nullable
    @Override
    public Tag serializeNBT(HolderLookup.Provider registries) {
        return MachineRenderState.CODEC.encodeStart(NbtOps.INSTANCE, payload).getOrThrow();
    }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider registries) {
        payload = MachineRenderState.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }
}
