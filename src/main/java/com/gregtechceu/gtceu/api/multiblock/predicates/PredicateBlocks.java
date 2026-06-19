package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.StringJoiner;

public class PredicateBlocks extends BasePredicate {

    public final @Unmodifiable List<Block> blocks;

    public PredicateBlocks(Block... blocks) {
        this(null, blocks);
    }

    public PredicateBlocks(@Nullable String debugName, Block... blocks) {
        this(null, List.of(blocks), null);
    }

    public PredicateBlocks(@Nullable String debugName, List<Block> blocks, @Nullable PatternError error) {
        if (blocks.isEmpty()) {
            this.blocks = List.of(Blocks.BARRIER);
        } else {
            this.blocks = blocks;
        }
        Validate.noNullElements(this.blocks, "Blocks array has null element at index %s");

        errorPredicate = state -> {
            BlockPos pos = state.getPos();

            return this.blocks.contains(state.retrieveCurrentBlockState().getBlock()) ? null :
                    (error == null ? new BlockMatchingError(pos, this.blocks) : error);
        };

        candidates = this.blocks.stream()
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
