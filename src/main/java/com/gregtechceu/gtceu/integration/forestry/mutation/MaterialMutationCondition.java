package com.gregtechceu.gtceu.integration.forestry.mutation;

import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MaterialMutationCondition implements IMutationCondition {
    private final Supplier<Block> blockSupplier;

    public MaterialMutationCondition(Supplier<Block> blockSupplier) {
        this.blockSupplier = blockSupplier;
    }

    private Set<BlockState> getAcceptedStates() {
        Block block = blockSupplier.get();
        if (block == null) return Set.of();
        return new HashSet<>(block.getStateDefinition().getPossibleStates());
    }

    @Override
    public float modifyChance(Level level, BlockPos pos, IMutation<?> mutation, IGenome genome0, IGenome genome1, IClimateProvider climate, float currentChance) {
        Set<BlockState> accepted = getAcceptedStates();
        if (accepted.isEmpty()) return currentChance;

        BlockPos checkPos = pos;
        while (level.getBlockEntity(checkPos) != null) {
            checkPos = checkPos.below();
        }

        return accepted.contains(level.getBlockState(checkPos)) ? currentChance : 0.0F;
    }

    @Override
    public Component getDescription() {
        Block block = blockSupplier.get();
        if (block == null) return Component.empty();
        return Component.translatable("for.mutation.condition.resource", block.getName());
    }
}
