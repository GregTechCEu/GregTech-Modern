package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlockEntities
 */
public class GTBlockEntities {

    // Cable Block Entity
    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<CableBlockEntity> CABLE = REGISTRATE
            .blockEntity("cable", CableBlockEntity::create)
            .onRegister(CableBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.CABLE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    // Fluid Pipe Block Entity
    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<FluidPipeBlockEntity> FLUID_PIPE = REGISTRATE
            .blockEntity("fluid_pipe", FluidPipeBlockEntity::create)
            .onRegister(FluidPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.FLUID_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    public static void init() {}
}
