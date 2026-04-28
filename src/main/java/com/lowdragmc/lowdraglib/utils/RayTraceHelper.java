package com.lowdragmc.lowdraglib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class RayTraceHelper {

    public static BlockHitResult rayTraceRange(Level level, Player player, double range) {
        return com.lowdragmc.lowdraglib2.utils.RayTraceHelper.rayTraceRange(level, player, range);
    }

    public static Vec3 getTraceTarget(Player player, double range, Vec3 origin) {
        return com.lowdragmc.lowdraglib2.utils.RayTraceHelper.getTraceTarget(player, range, origin);
    }

    public static Vec3 getTraceOrigin(Player player) {
        return com.lowdragmc.lowdraglib2.utils.RayTraceHelper.getTraceOrigin(player);
    }

    public static Object rayTraceUntil(Player player, double range, Predicate<BlockPos> predicate) {
        return com.lowdragmc.lowdraglib2.utils.RayTraceHelper.rayTraceUntil(player, range, predicate);
    }
}
