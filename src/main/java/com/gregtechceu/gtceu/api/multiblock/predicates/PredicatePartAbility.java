package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.error.PartAbilityError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

public class PredicatePartAbility extends BasePredicate {

    public final @Unmodifiable List<Block> blocks;
    public final PartAbility partAbility;

    public PredicatePartAbility(@Nullable String debugName, PartAbility partAbility) {
        this.debugName = Objects.requireNonNullElse(debugName, "Unknown Part Ability");
        this.partAbility = partAbility;
        if (partAbility.getAllBlocks().isEmpty()) {
            this.blocks = List.of(Blocks.BARRIER);
        } else {
            this.blocks = partAbility.getAllBlocks().stream().toList();
        }
        Validate.noNullElements(this.blocks, "Blocks array has null element at index %s");

        errorPredicate = state -> {
            BlockPos pos = state.getPos();

            return this.blocks.contains(state.retrieveCurrentBlockState().getBlock()) ? null :
                    new PartAbilityError(pos, partAbility);
        };

        candidates = this.blocks.stream()
                .map(BlockInfo::fromBlock)
                .toList();
    }

    public PredicatePartAbility(@Nullable String debugName, PartAbility partAbility, int... tiers) {
        this.debugName = Objects.requireNonNullElse(debugName, "Unknown Part Ability");
        this.partAbility = partAbility;
        if (partAbility.getAllBlocks().isEmpty()) {
            this.blocks = List.of(Blocks.BARRIER);
        } else {
            this.blocks = partAbility.getBlocks(tiers).stream().toList();
        }
        Validate.noNullElements(this.blocks, "Blocks array has null element at index %s");
        errorPredicate = state -> {
            BlockPos pos = state.getPos();

            return this.blocks.contains(state.getBlockState().getBlock()) ? null :
                    new PartAbilityError(pos, partAbility);
        };

        candidates = this.blocks.stream()
                .map(BlockInfo::fromBlock)
                .toList();
    }
}
