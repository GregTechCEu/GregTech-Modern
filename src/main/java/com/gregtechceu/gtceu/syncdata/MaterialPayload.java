package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class MaterialPayload extends ObjectTypedPayload<Material> {

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        buf.writeUtf(payload.getResourceLocation().toString());
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        payload = GTCEuAPI.materialManager.getMaterial(buf.readUtf());
    }

    @Nullable
    @Override
    public Tag serializeNBT() {
        return StringTag.valueOf(payload.getResourceLocation().toString());
    }

    @Override
    public void deserializeNBT(Tag tag) {
        payload = GTCEuAPI.materialManager.getMaterial(tag.getAsString());
    }
}
