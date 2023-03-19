package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.api.block.VariantActiveBlock;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateBlocks;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateFluids;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateStates;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.block.variant.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
            if (state.getBlock() instanceof VariantActiveBlock<?> block) {
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
        return autoAbilities(recipeType, true, true, true, true, true);
    }

    public static TraceabilityPredicate autoAbilities(GTRecipeType recipeType,
                                                      boolean checkEnergyIn,
                                                      boolean checkItemIn,
                                                      boolean checkItemOut,
                                                      boolean checkFluidIn,
                                                      boolean checkFluidOut) {
//        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
        TraceabilityPredicate predicate = new TraceabilityPredicate()
                .or(checkEnergyIn ? abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        if (checkItemIn) {
            if (recipeType.getMinInputs(ItemRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
            } else if (recipeType.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeType.getMinOutputs(ItemRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
            } else if (recipeType.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkFluidIn) {
            if (recipeType.getMinInputs(FluidRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeType.getMinInputs(ItemRecipeCapability.CAP)));
            } else if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1));
            }
        }
        if (checkFluidOut) {
            if (recipeType.getMinOutputs(FluidRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeType.getMinOutputs(ItemRecipeCapability.CAP)));
            } else if (recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                predicate = predicate.or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1));
            }
        }
        return predicate;
    }

    public static TraceabilityPredicate heatingCoils() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            if (blockState.is(GTBlocks.WIRE_COIL.get())) {
                var stats = GTBlocks.WIRE_COIL.get().getVariant(blockState);
                Object currentCoil = blockWorldState.getMatchContext().getOrPut("CoilType", stats);
                if (!currentCoil.equals(stats)) {
                    blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.coils"));
                    return false;
                }
                return true;
            }
            return false;
        }, () -> Arrays.stream(CoilBlock.CoilType.values())
                // sort to make autogenerated jei previews not pick random coils each game load
                .sorted(Comparator.comparingInt(CoilBlock.CoilType::getTier))
                .map(coil -> BlockInfo.fromBlockState(GTBlocks.WIRE_COIL.get().getState(coil)))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
    }
}
