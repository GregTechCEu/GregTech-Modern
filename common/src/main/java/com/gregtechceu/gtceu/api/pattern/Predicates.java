package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateBlocks;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateFluids;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateStates;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Predicates {

    public static TraceabilityPredicate controller(TraceabilityPredicate predicate) {
        return predicate.setController();
    }

    public static TraceabilityPredicate states(BlockState... allowedStates) {
        var candidates = new ArrayList<BlockState>();
        for (BlockState state : allowedStates) {
            candidates.add(state);
            if (state.getBlock() instanceof ActiveBlock block) {
                candidates.add(block.changeActive(state, !block.isActive(state)));
            }
        }
        return new TraceabilityPredicate(new PredicateStates(candidates.toArray(BlockState[]::new)));
    }

    public static TraceabilityPredicate blocks(Block... blocks) {
        return new TraceabilityPredicate(new PredicateBlocks(blocks));
    }

    public static TraceabilityPredicate fluids(Fluid... fluids) {
        return new TraceabilityPredicate(new PredicateFluids(fluids));
    }

    public static TraceabilityPredicate custom(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        return new TraceabilityPredicate(predicate, candidates);
    }
    public static TraceabilityPredicate any() {
        return new TraceabilityPredicate(SimplePredicate.ANY);
    }

    public static TraceabilityPredicate air() {
        return new TraceabilityPredicate(SimplePredicate.AIR);
    }

    public static TraceabilityPredicate abilities(PartAbility... abilities) {
        return blocks(Arrays.stream(abilities).map(PartAbility::getAllBlocks).flatMap(Collection::stream).toArray(Block[]::new));
    }

    public static TraceabilityPredicate autoAbilities(GTRecipeType recipeType) {
        return autoAbilities(recipeType, true, true, true, true, true, true);
    }

    public static TraceabilityPredicate autoAbilities(GTRecipeType recipeType,
                                                      boolean checkEnergyIn,
                                                      boolean checkEnergOut,
                                                      boolean checkItemIn,
                                                      boolean checkItemOut,
                                                      boolean checkFluidIn,
                                                      boolean checkFluidOut) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();
        if (checkEnergyIn) {
            if (recipeType.getMaxInputs(EURecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(3).setPreviewCount(1));
            }
        }
        if (checkEnergOut) {
            if (recipeType.getMaxOutputs(EURecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3).setPreviewCount(1));
            }
        }
        if (checkItemIn) {
            if (recipeType.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeType.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkFluidIn) {
            if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1));
            }
        }
        if (checkFluidOut) {
            if (recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1));
            }
        }
        return predicate;
    }

    public static TraceabilityPredicate heatingCoils() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (Map.Entry<CoilBlock.CoilType, BlockEntry<CoilBlock>> entry : GTBlocks.ALL_COILS.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    var stats = entry.getKey();
                    Object currentCoil = blockWorldState.getMatchContext().getOrPut("CoilType", stats);
                    if (!currentCoil.equals(stats)) {
                        blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.coils"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> Arrays.stream(CoilBlock.CoilType.values())
                // sort to make autogenerated jei previews not pick random coils each game load
                .sorted(Comparator.comparingInt(CoilBlock.CoilType::getTier))
                .map(coil -> BlockInfo.fromBlockState(GTBlocks.ALL_COILS.get(coil).getDefaultState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
    }
}
