package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.api.material.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LevelEnergyNet extends LevelPipeNet<WireProperties, EnergyNet> {

    public static LevelEnergyNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> new LevelEnergyNet(serverLevel), (tag, provider) -> new LevelEnergyNet(serverLevel, tag, provider)), "gtceu_energy_net");
    }

    public LevelEnergyNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelEnergyNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected EnergyNet createNetInstance() {
        return new EnergyNet(this);
    }
}
