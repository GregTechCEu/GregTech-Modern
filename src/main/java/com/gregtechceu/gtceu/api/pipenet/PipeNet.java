package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.*;

public class PipeNet {

    @Getter
    protected final LevelPipeNet worldData;

    private final Map<BlockPos, List<IRoutePath<?>>> routePaths = new Object2ObjectOpenHashMap<>();

    @Getter
    private final PipeNetworkType networkType;

    @Getter
    private long lastUpdate;
    @Getter
    boolean isValid = false;

    public PipeNet(LevelPipeNet levelPipeNet, PipeNetworkType netType) {
        this.worldData = levelPipeNet;
        this.networkType = netType;
    }

    public ServerLevel getLevel() {
        return worldData.getWorld();
    }

    public void onNeighbourUpdate(BlockPos fromPos) {
        routePaths.clear();
    }
}
