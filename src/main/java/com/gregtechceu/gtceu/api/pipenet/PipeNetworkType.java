package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public record PipeNetworkType(ResourceLocation id,
                              Supplier<BlockEntityType<? extends PipeBlockEntity<?>>> blockEntityType) {

}
