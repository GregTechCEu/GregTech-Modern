package com.gregtechceu.gtceu.syncdata;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerPayload extends ObjectTypedPayload<AtomicInteger> {

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        buf.writeVarInt(this.payload.get());
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        this.payload = new AtomicInteger(buf.readVarInt());
    }

    @Nullable
    @Override
    public Tag serializeNBT() {
        return IntTag.valueOf(this.payload.get());
    }

    @Override
    public void deserializeNBT(Tag tag) {
        this.payload = new AtomicInteger(((IntTag)tag).getAsInt());
    }
}
