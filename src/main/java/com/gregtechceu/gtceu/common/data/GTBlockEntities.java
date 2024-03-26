package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.blockentity.*;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlockEntities
 */
public class GTBlockEntities {
    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<CableBlockEntity> CABLE = REGISTRATE
            .blockEntity("cable", CableBlockEntity::create)
            .onRegister(CableBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.CABLE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<FluidPipeBlockEntity> FLUID_PIPE = REGISTRATE
            .blockEntity("fluid_pipe", FluidPipeBlockEntity::new)
            .onRegister(FluidPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.FLUID_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<ItemPipeBlockEntity> ITEM_PIPE = REGISTRATE
            .blockEntity("item_pipe", ItemPipeBlockEntity::create)
            .onRegister(ItemPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.ITEM_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    public static final BlockEntityEntry<LaserPipeBlockEntity> LASER_PIPE = REGISTRATE
            .blockEntity("laser_pipe", LaserPipeBlockEntity::create)
            .onRegister(LaserPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.LASER_PIPES)
            .register();

    public static final BlockEntityEntry<OpticalPipeBlockEntity> OPTICAL_PIPE = REGISTRATE
        .blockEntity("optical_pipe", OpticalPipeBlockEntity::new)
        .validBlocks(GTBlocks.OPTICAL_PIPES)
        .register();

    public static void init() {

    }
}
