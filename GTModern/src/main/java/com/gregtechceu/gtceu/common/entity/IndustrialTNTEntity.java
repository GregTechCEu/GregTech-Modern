package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IndustrialTNTEntity extends GTExplosiveEntity {

    public IndustrialTNTEntity(Level world, double x, double y, double z, @Nullable LivingEntity owner) {
        super(GTEntityTypes.INDUSTRIAL_TNT.get(), world, x, y, z, owner);
    }

    @SuppressWarnings("unused")
    public IndustrialTNTEntity(EntityType<? extends IndustrialTNTEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected float getStrength() {
        return 5.0F;
    }

    @Override
    public boolean dropsAllBlocks() {
        return true;
    }

    @Override
    protected int getRange() {
        return 3;
    }

    @Override
    public @NotNull BlockState getExplosiveState() {
        return GTBlocks.INDUSTRIAL_TNT.getDefaultState();
    }
}
