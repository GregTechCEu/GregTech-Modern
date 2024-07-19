package com.gregtechceu.gtceu.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class GTTeleporter implements ITeleporter {

    private final ServerLevel worldServerInstance;
    private final double x, y, z;

    public GTTeleporter(ServerLevel level, double x, double y, double z) {
        this.worldServerInstance = level;
        this.x = x;
        this.y = y;
        this.z = z;
    }


}
