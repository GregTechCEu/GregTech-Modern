package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LevelItemPipeNet extends LevelPipeNet<ItemPipeProperties, ItemPipeNet> {

    private static final String DATA_ID = "gtceu_item_pipe_net";
    private static final SavedDataType<LevelItemPipeNet> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_ID),
            LevelItemPipeNet::new,
            serverLevel -> CompoundTag.CODEC.xmap(
                    tag -> new LevelItemPipeNet(serverLevel, tag, serverLevel.registryAccess()),
                    data -> data.save(new CompoundTag(), serverLevel.registryAccess())));

    public static LevelItemPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public LevelItemPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelItemPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected ItemPipeNet createNetInstance() {
        return new ItemPipeNet(this);
    }
}
