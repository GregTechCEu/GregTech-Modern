package com.gregtechceu.gtceu.api.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

public class BrittleCharcoalBlock extends Block {

    public BrittleCharcoalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return super.getDrops(state, params);
    }
}
