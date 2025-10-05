package com.gregtechceu.gtceu.api.mui.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class PosGuiData extends GuiData {

    @Getter
    private final BlockPos blockPos;

    public PosGuiData(Player player, BlockPos blockPos) {
        super(player);
        this.blockPos = blockPos;
    }

    public double getSquaredDistance(double x, double y, double z) {
        return this.blockPos.distToCenterSqr(x, y, z);
    }

    public double getSquaredDistance(Position pos) {
        return getSquaredDistance(pos.x(), pos.y(), pos.z());
    }

    public double getDistance(double x, double y, double z) {
        return Math.sqrt(getSquaredDistance(x, y, z));
    }

    public double getDistance(Position pos) {
        return Math.sqrt(getSquaredDistance(pos));
    }

    public double getSquaredDistance(Entity entity) {
        return getSquaredDistance(entity.position());
    }

    public double getDistance(Entity entity) {
        return Math.sqrt(getSquaredDistance(entity));
    }

    public BlockEntity getBlockEntity() {
        return getLevel().getBlockEntity(this.blockPos);
    }
}
