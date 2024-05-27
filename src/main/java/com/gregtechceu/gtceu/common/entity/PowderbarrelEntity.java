package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowderbarrelEntity extends GTExplosiveEntity {

    public PowderbarrelEntity(Level world, double x, double y, double z, @Nullable LivingEntity owner) {
        super(GTEntityTypes.POWDERBARREL.get(), world, x, y, z, owner);
    }

    public PowderbarrelEntity(EntityType<? extends PowderbarrelEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected float getStrength() {
        return 3.5F;
    }

    @Override
    public boolean dropsAllBlocks() {
        return true;
    }

    @Override
    @NotNull
    public BlockState getExplosiveState() {
        return GTBlocks.POWDERBARREL.getDefaultState();
    }
}
