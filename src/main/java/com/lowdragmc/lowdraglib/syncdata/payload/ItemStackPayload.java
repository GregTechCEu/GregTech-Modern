package com.lowdragmc.lowdraglib.syncdata.payload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.DynamicOps;

public class ItemStackPayload extends ObjectTypedPayload<ItemStack> {

    public ItemStackPayload() {
        this.payload = ItemStack.EMPTY;
    }

    @Override
    public void writePayload(RegistryFriendlyByteBuf buffer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload == null ? ItemStack.EMPTY : payload);
    }

    @Override
    public void readPayload(RegistryFriendlyByteBuf buffer) {
        payload = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    }

    @Override
    public Tag serializeNBT(HolderLookup.Provider provider) {
        DynamicOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.encodeStart(ops, payload == null ? ItemStack.EMPTY : payload).getOrThrow();
    }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider provider) {
        DynamicOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        payload = ItemStack.OPTIONAL_CODEC.parse(ops, tag).result().orElse(ItemStack.EMPTY);
    }

    @Override
    public Object copyForManaged(Object value) {
        return value instanceof ItemStack stack ? stack.copy() : ItemStack.EMPTY;
    }
}
