package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.blockentity.*;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTBlockEntities {

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<CableBlockEntity> CABLE = REGISTRATE
            .blockEntity("cable", CableBlockEntity::new)
            .validBlocks(GTMaterialBlocks.CABLE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<FluidPipeBlockEntity> FLUID_PIPE = REGISTRATE
            .blockEntity("fluid_pipe", FluidPipeBlockEntity::new)
            .validBlocks(GTMaterialBlocks.FLUID_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<ItemPipeBlockEntity> ITEM_PIPE = REGISTRATE
            .blockEntity("item_pipe", ItemPipeBlockEntity::new)
            .validBlocks(GTMaterialBlocks.ITEM_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    public static final BlockEntityEntry<LaserPipeBlockEntity> LASER_PIPE = REGISTRATE
            .blockEntity("laser_pipe", LaserPipeBlockEntity::new)
            .validBlocks(GTBlocks.LASER_PIPES)
            .register();

    public static final BlockEntityEntry<OpticalPipeBlockEntity> OPTICAL_PIPE = REGISTRATE
            .blockEntity("optical_pipe", OpticalPipeBlockEntity::new)
            .validBlocks(GTBlocks.OPTICAL_PIPES)
            .register();

    public static final BlockEntityEntry<DuctPipeBlockEntity> DUCT_PIPE = REGISTRATE
            .blockEntity("duct_pipe", DuctPipeBlockEntity::new)
            .validBlocks(GTBlocks.DUCT_PIPES)
            .register();

    public static void init() {}
}
