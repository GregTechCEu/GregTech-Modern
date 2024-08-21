package com.gregtechceu.gtceu.api.graphnet.servernet;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ServerNetNode extends NetNode {

    private GlobalPos pos;

    public ServerNetNode(IGraphNet net) {
        super(net);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putLong("Pos", pos.pos().asLong());
        tag.putString("Dim", pos.dimension().location().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        BlockPos pos = BlockPos.of(nbt.getLong("Pos"));
        this.pos = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("Dim"))), pos);
    }

    @Override
    public GlobalPos getEquivalencyData() {
        return pos;
    }
}
