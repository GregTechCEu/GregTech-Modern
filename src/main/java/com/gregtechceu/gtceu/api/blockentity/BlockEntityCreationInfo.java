package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public record BlockEntityCreationInfo(BlockEntityType<?> type, BlockPos pos, BlockState state) {}
