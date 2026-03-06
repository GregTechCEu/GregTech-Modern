package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public class GTRecipePayload extends ObjectTypedPayload<GTRecipe> {

    @Nullable
    @Override
    public Tag serializeNBT(HolderLookup.Provider provider) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);

        CompoundTag tag = new CompoundTag();
        tag.putString("id", payload.id.toString());
        tag.put("recipe",
                GTRecipeSerializer.CODEC.codec().encodeStart(ops, payload).result().orElse(new CompoundTag()));
        tag.putInt("parallels", payload.parallels);
        tag.putInt("ocLevel", payload.ocLevel);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider provider) {
        if (tag instanceof CompoundTag compoundTag) {
            var ops = provider.createSerializationContext(NbtOps.INSTANCE);

            payload = GTRecipeSerializer.CODEC.codec().parse(ops, compoundTag.get("recipe")).result().orElse(null);
            if (payload != null) {
                payload.id = ResourceLocation.parse(compoundTag.getString("id"));
                payload.parallels = compoundTag.contains("parallels") ? compoundTag.getInt("parallels") : 1;
                payload.ocLevel = compoundTag.getInt("ocLevel");
            }
        }
    }

    @Override
    public void writePayload(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(this.payload.id);
        GTRecipeSerializer.toNetwork(buf, this.payload);
        buf.writeInt(this.payload.parallels);
        buf.writeInt(this.payload.ocLevel);
    }

    @Override
    public void readPayload(RegistryFriendlyByteBuf buf) {
        this.payload = GTRecipeSerializer.fromNetwork(buf);
        this.payload.parallels = buf.readInt();
        this.payload.ocLevel = buf.readInt();
    }
}
