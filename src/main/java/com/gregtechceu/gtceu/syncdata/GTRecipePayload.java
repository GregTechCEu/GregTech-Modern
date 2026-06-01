package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Nullable;

public class GTRecipePayload extends ObjectTypedPayload<GTRecipe> {

    @Nullable
    @Override
    public Tag serializeNBT() {
        if (payload == null) return null;
        return payload.toNBT();
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            payload = GTRecipe.fromNBT(compoundTag);
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        payload.toNetwork(buf);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        payload = GTRecipe.fromNetwork(buf);
    }
}
