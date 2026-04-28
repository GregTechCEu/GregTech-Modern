package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LevelEnergyNet extends LevelPipeNet<WireProperties, EnergyNet> {

    private static final String DATA_ID = "gtceu_energy_net";
    private static final SavedDataType<LevelEnergyNet> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_ID),
            LevelEnergyNet::new,
            serverLevel -> CompoundTag.CODEC.xmap(
                    tag -> new LevelEnergyNet(serverLevel, tag, serverLevel.registryAccess()),
                    data -> data.save(new CompoundTag(), serverLevel.registryAccess())));

    public static LevelEnergyNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
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
