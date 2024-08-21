package com.gregtechceu.gtceu.api.graphnet.worldnet;

import com.gregtechceu.gtceu.api.graphnet.NetNode;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

public class WorldNetNode extends NetNode {

    private BlockPos pos;

    public WorldNetNode(WorldNet net) {
        super(net);
    }

    @Override
    public @NotNull WorldNet getNet() {
        return (WorldNet) super.getNet();
    }

    public WorldNetNode setPos(BlockPos pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putLong("Pos", pos.asLong());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.setPos(BlockPos.of(nbt.getLong("Pos")));
    }

    @Override
    public BlockPos getEquivalencyData() {
        return pos;
    }
}
