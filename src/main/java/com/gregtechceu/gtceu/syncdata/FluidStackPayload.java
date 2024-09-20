package com.gregtechceu.gtceu.syncdata;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public class FluidStackPayload extends ObjectTypedPayload<FluidStack> {

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        payload.writeToPacket(buf);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        payload = FluidStack.readFromPacket(buf);
    }

    @Nullable
    @Override
    public Tag serializeNBT() {
        return payload.writeToNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(Tag tag) {
        payload = FluidStack.loadFluidStackFromNBT((CompoundTag) tag);
    }
}
