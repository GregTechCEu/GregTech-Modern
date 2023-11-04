package com.gregtechceu.gtceu.common.pipelike.item;

import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelItemPipeNet extends LevelPipeNet<ItemPipeData, ItemPipeNet> {

    public static LevelItemPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelItemPipeNet(serverLevel, tag), () -> new LevelItemPipeNet(serverLevel), "gtceu_item_pipe_net");
    }

    public LevelItemPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelItemPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected ItemPipeNet createNetInstance() {
        return new ItemPipeNet(this);
    }
}
