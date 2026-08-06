package com.gregtechceu.gtceu.common.data.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTDevBlocks {

    public static final BlockEntry<Block> CTM_TEST = REGISTRATE.block("ctm_test", Block::new)
            .lang("Connected Texture Test Block")
            .properties(p -> p.noLootTable().sound(SoundType.SCULK))
            .simpleItem()
            .register();

    public static void init() {}
}
