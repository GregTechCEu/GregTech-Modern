package com.gregtechceu.gtceu.syncdata;

import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import static com.lowdragmc.lowdraglib.side.fluid.FluidStack.loadFromTag;

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
        try {
            payload = FluidStack.loadFluidStackFromNBT((CompoundTag) tag);
        } catch (ClassCastException exception) {
            // LDLib FluidStack stores amount as Long tag, which will throw an error
            // Loads from tag using LDLib FluidStack, then converts it to a Forge FluidStack
            var stack = loadFromTag((CompoundTag) tag);
            payload = FluidHelperImpl.toFluidStack(stack);
        }
    }

    @Override
    public Object copyForManaged(Object value) {
        if (value instanceof FluidStack) {
            return ((FluidStack) value).copy();
        }
        return value;
    }
}
