package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.material.material.Material;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class MaterialPayload extends ObjectTypedPayload<Material> {

    @Override
    public void writePayload(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(payload.getResourceLocation().toString());
    }

    @Override
    public void readPayload(RegistryFriendlyByteBuf buf) {
        payload = GTCEuAPI.materialManager.getMaterial(buf.readUtf());
    }

    @Nullable
    @Override
    public Tag serializeNBT(HolderLookup.Provider provider) {
        return StringTag.valueOf(payload.getResourceLocation().toString());
    }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider provider) {
        payload = GTCEuAPI.materialManager.getMaterial(tag.getAsString());
    }
}
