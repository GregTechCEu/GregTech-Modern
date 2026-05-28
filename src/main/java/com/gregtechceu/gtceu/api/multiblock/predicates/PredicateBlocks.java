package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class PredicateBlocks extends BasePredicate {

    public Block[] blocks;

    public PredicateBlocks(Block... blocks) {
        this(null, blocks);
    }

    public PredicateBlocks(String debugName, Block... blocks) {
        this.blocks = blocks;

        blocks = Arrays.stream(blocks).filter(Objects::nonNull).toArray(Block[]::new);
        if (blocks.length == 0) blocks = new Block[] { Blocks.BARRIER };
        Block[] finalBlocks = blocks;
        errorPredicate = state -> {
            BlockPos pos = state.getPos();

            return ArrayUtils.contains(finalBlocks, state.getBlockState().getBlock()) ? null : new BlockMatchingError(pos, finalBlocks);
        };

        candidates = Arrays.stream(finalBlocks)
                .map(BlockInfo::fromBlock)
                .toList();

        if (debugName == null) {
            StringJoiner sb = new StringJoiner("-");
            for (Block b : blocks) {
                sb.add(BuiltInRegistries.BLOCK.getKey(b).getPath());
            }
            this.debugName = sb.toString();
        } else {
            this.debugName = debugName;
        }
    }
}
