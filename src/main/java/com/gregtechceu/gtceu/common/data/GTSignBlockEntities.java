package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.blockentity.GTHangingSignBlockEntity;

import net.minecraft.world.level.block.entity.SignBlockEntity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

// This has to be seperate from GTBlockEntities because of the order in which forge events are fired, as the main block
// entities class can't be loaded before the registries are populated.
public class GTSignBlockEntities {

    public static void init() {}

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
}
