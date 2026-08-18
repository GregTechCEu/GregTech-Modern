package com.gregtechceu.gtceu.integration.map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IWaypointHandler {

    void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, BlockPos pos);

    void removeWaypoint(String key);
}
