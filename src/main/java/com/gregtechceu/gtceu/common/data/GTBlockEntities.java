package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.blockentity.*;

import net.minecraft.world.level.block.entity.SignBlockEntity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTBlockEntities {

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<CableBlockEntity> CABLE = REGISTRATE
            .blockEntity("cable", CableBlockEntity::create)
            .onRegister(CableBlockEntity::onBlockEntityRegister)
            .validBlocks(GTMaterialBlocks.CABLE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<FluidPipeBlockEntity> FLUID_PIPE = REGISTRATE
            .blockEntity("fluid_pipe", FluidPipeBlockEntity::new)
            .onRegister(FluidPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTMaterialBlocks.FLUID_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<ItemPipeBlockEntity> ITEM_PIPE = REGISTRATE
            .blockEntity("item_pipe", ItemPipeBlockEntity::create)
            .onRegister(ItemPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTMaterialBlocks.ITEM_PIPE_BLOCKS.values().toArray(BlockEntry[]::new))
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

    public static final BlockEntityEntry<DuctPipeBlockEntity> DUCT_PIPE = REGISTRATE
            .blockEntity("duct_pipe", DuctPipeBlockEntity::create)
            .onRegister(DuctPipeBlockEntity::onBlockEntityRegister)
            .validBlocks(GTBlocks.DUCT_PIPES)
            .register();

    public static final BlockEntityEntry<SignBlockEntity> GT_SIGN = REGISTRATE
            .<SignBlockEntity>blockEntity("sign", SignBlockEntity::new)
            .validBlocks(GTBlocks.RUBBER_SIGN,
                    GTBlocks.RUBBER_WALL_SIGN,
                    GTBlocks.TREATED_WOOD_SIGN,
                    GTBlocks.TREATED_WOOD_WALL_SIGN)
            .register();

    public static final BlockEntityEntry<GTHangingSignBlockEntity> GT_HANGING_SIGN = REGISTRATE
            .blockEntity("hanging_sign", GTHangingSignBlockEntity::new)
            .validBlocks(GTBlocks.RUBBER_HANGING_SIGN,
                    GTBlocks.RUBBER_WALL_HANGING_SIGN,
                    GTBlocks.TREATED_WOOD_HANGING_SIGN,
                    GTBlocks.TREATED_WOOD_WALL_HANGING_SIGN)
            .register();

    public static void init() {}
}
