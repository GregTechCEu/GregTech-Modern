package com.gregtechceu.gtceu.api.graphnet.pipenet.logic;

import gregtech.api.network.IPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;

public class TemperatureLossFunction implements INBTSerializable<CompoundTag>, IPacket {

    private static final Float2ObjectArrayMap<TemperatureLossFunction> CABLE_LOSS_CACHE = new Float2ObjectArrayMap<>();
    private static final Float2ObjectArrayMap<TemperatureLossFunction> PIPE_LOSS_CACHE = new Float2ObjectArrayMap<>();

    private EnumLossFunction function;
    private float factorX;
    private float factorY;

    public TemperatureLossFunction(EnumLossFunction function, float factorX) {
        this.function = function;
        this.factorX = factorX;
    }

    public TemperatureLossFunction(EnumLossFunction function, float factorX, float factorY) {
        this.function = function;
        this.factorX = factorX;
        this.factorY = factorY;
    }

    public TemperatureLossFunction() {}

    public TemperatureLossFunction(CompoundTag tag) {
        deserializeNBT(tag);
    }

    public float restoreTemperature(float energy, int timePassed) {
        return function.applyLoss(energy, factorX, factorY, timePassed);
    }

    public static TemperatureLossFunction getOrCreateCable(float factor) {
        TemperatureLossFunction function = CABLE_LOSS_CACHE.get(factor);
        if (function == null) {
            function = new TemperatureLossFunction(EnumLossFunction.WEAK_SCALING, factor, 0.35f);
            CABLE_LOSS_CACHE.put(factor, function);
        }
        return function;
    }

    public static TemperatureLossFunction getOrCreatePipe(float factor) {
        TemperatureLossFunction function = PIPE_LOSS_CACHE.get(factor);
        if (function == null) {
            // since pipes are hollow the exponent is larger
            function = new TemperatureLossFunction(EnumLossFunction.WEAK_SCALING, factor, 0.45f);
            PIPE_LOSS_CACHE.put(factor, function);
        }
        return function;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.setInteger("Ordinal", function.ordinal());
        tag.setFloat("X", factorX);
        if (factorY != 0) tag.setFloat("Y", factorY);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        function = EnumLossFunction.values()[nbt.getInteger("Ordinal")];
        factorX = nbt.getFloat("X");
        factorY = nbt.getFloat("Y");
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(function.ordinal());
        buf.writeFloat(factorX);
        buf.writeFloat(factorY);
    }

    @Override
    public void decode(PacketBuffer buf) {
        function = EnumLossFunction.values()[buf.readVarInt()];
        factorX = buf.readFloat();
        factorY = buf.readFloat();
    }
}
