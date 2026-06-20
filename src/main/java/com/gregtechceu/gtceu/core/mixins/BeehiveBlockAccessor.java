package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeehiveBlock.class)
public interface BeehiveBlockAccessor {

    @Invoker("hiveContainsBees")
    boolean gtceu$hiveContainsBees(Level level, BlockPos pos);

    @Invoker("angerNearbyBees")
    void gtceu$angerNearbyBees(Level level, BlockPos pos);
}
