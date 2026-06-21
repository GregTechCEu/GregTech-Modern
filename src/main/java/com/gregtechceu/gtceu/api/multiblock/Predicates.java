package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.PlaceholderError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class Predicates {

    /**
     * Return this for your pattern errors if you want them to be a default error with the pos of the BlockWorldState
     * and candidates of the simple predicate's error.
     */
    public static final PlaceholderError PLACEHOLDER = new PlaceholderError(BlockPos.ZERO, Collections.emptyList());

    public static PatternPredicate controller(MultiblockMachineDefinition def) {
        return controller(blocks(def.getBlock()));
    }

    public static PatternPredicate controller(PatternPredicate predicate) {
        return predicate.setController();
    }

    public static PatternPredicate states(BlockState... allowedStates) {
        var candidates = new ArrayList<BlockState>();
        for (BlockState state : allowedStates) {
            candidates.add(state);
            if (state.getBlock() instanceof ActiveBlock block) {
                candidates.add(block.changeActive(state, !block.isActive(state)));
            }
        }
        return new PatternPredicate(new PredicateStates(candidates.toArray(BlockState[]::new)));
    }

    public static PatternPredicate blocks(String debugName, Block... blocks) {
        return new PatternPredicate(new PredicateBlocks(debugName, blocks));
    }

    public static PatternPredicate blocks(Block... blocks) {
        return new PatternPredicate(new PredicateBlocks(blocks));
    }

    /*
     * public static PatternPredicate blocks(IMachineBlock... blocks) {
     * return new PatternPredicate(
     * new PredicateBlocks(Arrays.stream(blocks).map(IMachineBlock::self).toArray(Block[]::new)));
     * }
     */

    public static PatternPredicate machines(MachineDefinition... definitions) {
        Validate.noNullElements(definitions, "MachineDefinition array has null element at index %s");
        return blocks(Arrays.stream(definitions).map(MachineDefinition::get).toArray(MetaMachineBlock[]::new));
    }

    public static PatternPredicate blockTag(TagKey<Block> tag) {
        return new PatternPredicate(new PredicateBlockTag(tag));
    }

    public static PatternPredicate fluids(Fluid... fluids) {
        return new PatternPredicate(new PredicateFluids(fluids));
    }

    public static PatternPredicate fluidTag(TagKey<Fluid> tag) {
        return new PatternPredicate(new PredicateFluidTag(tag));
    }

    public static PatternPredicate custom(Function<CurrentBlockInfo, @Nullable PatternError> predicate,
                                          @Nullable List<BlockInfo> candidates) {
        return new PatternPredicate(predicate, candidates);
    }

    public static PatternPredicate any() {
        return PatternPredicate.ANY;
    }

    public static PatternPredicate air() {
        return PatternPredicate.AIR;
    }

    public static PatternPredicate abilities(PartAbility... abilities) {
        StringJoiner sb = new StringJoiner("-");
        for (PartAbility ability : abilities) {
            sb.add(ability.getName());
        }
        String debugName = sb.toString();

        PatternPredicate predicate = new PatternPredicate();
        for (var ability : abilities) {
            predicate.subPredicates.add(new PredicatePartAbility(debugName, ability));
        }
        return predicate;
    }

    public static PatternPredicate ability(PartAbility ability, int... tiers) {
        StringJoiner sb = new StringJoiner("-");
        for (int tier : tiers) {
            sb.add(GTValues.VN[tier]);
        }
        String debugName = ability.getName() + sb;

        return new PatternPredicate(new PredicatePartAbility(debugName, ability, tiers));
    }

    public static PatternPredicate autoAbilities(GTRecipeType... recipeType) {
        return autoAbilities(recipeType, true, true, true, true, true, true);
    }

    public static PatternPredicate autoAbilities(GTRecipeType[] recipeType,
                                                 boolean checkEnergyIn, boolean checkEnergyOut,
                                                 boolean checkItemIn, boolean checkItemOut,
                                                 boolean checkFluidIn, boolean checkFluidOut) {
        PatternPredicate predicate = new PatternPredicate();

        if (checkEnergyIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(EURecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                            .setMaxGlobalLimited(2).setPreviewCount(1)
                            .setPriority(1));
                    break;
                }
            }
        }
        if (checkEnergyOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(EURecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.OUTPUT_ENERGY).setMinGlobalLimited(1)
                            .setMaxGlobalLimited(2).setPreviewCount(1)
                            .setPriority(1));
                    break;
                }
            }
        }
        if (checkItemIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1)
                            .setPriority(2));
                    break;
                }
            }
        }
        if (checkItemOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1)
                            .setPriority(2));
                    break;
                }
            }
        }
        if (checkFluidIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1)
                            .setPriority(3));
                    break;
                }
            }
        }
        if (checkFluidOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)
                            .setPriority(3));
                    break;
                }
            }
        }
        return predicate;
    }

    public static PatternPredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler,
                                                 boolean checkParallel) {
        PatternPredicate predicate = new PatternPredicate();
        if (checkMaintenance) {
            predicate = predicate.or(abilities(PartAbility.MAINTENANCE)
                    .setMinGlobalLimited(ConfigHolder.INSTANCE.machines.enableMaintenance ? 1 : 0)
                    .setMaxGlobalLimited(1)
                    .setPriority(1));
        }
        if (checkMuffler) {
            predicate = predicate.or(abilities(PartAbility.MUFFLER)
                    .setExactLimit(1)
                    .setPriority(2));
        }
        if (checkParallel) {
            predicate = predicate.or(abilities(PartAbility.PARALLEL_HATCH)
                    .setMaxGlobalLimited(1)
                    .setPreviewCount(1)
                    .setPriority(3));
        }
        return predicate;
    }

    public static PatternPredicate heatingCoils() {
        return new PatternPredicate("Heating Coils", worldState -> {
            var blockState = worldState.getBlockState();
            for (var entry : GTCEuAPI.HEATING_COILS.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    return null;
                }
            }
            return new BlockMatchingError(worldState.getBlockPos(), GTCEuAPI.HEATING_COILS.values().stream()
                    .map(coilBlockSupplier -> (Block) coilBlockSupplier.get()).toList());
        }, GTCEuAPI.HEATING_COILS.entrySet().stream()
                // sort to make autogenerated jei previews not pick random coils each game load
                .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                .map(e -> new BlockInfo(e.getValue().get()))
                .toList())
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"))
                .setPriority(0);
    }

    public static PatternPredicate cleanroomFilters() {
        return new PatternPredicate("Cleanroom Filters", worldState -> {
            var blockState = worldState.getBlockState();
            for (var entry : GTCEuAPI.CLEANROOM_FILTERS.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    return null;
                }
            }
            return new BlockMatchingError(worldState.getBlockPos(),
                    GTCEuAPI.CLEANROOM_FILTERS.entrySet().stream().map(e -> e.getValue().get()).toList());
        }, GTCEuAPI.CLEANROOM_FILTERS.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getCleanroomType().getTier()))
                .map(e -> new BlockInfo(e.getValue().get()))
                .toList())
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.filters"));
    }

    public static PatternPredicate powerSubstationBatteries() {
        return new PatternPredicate("PSS Batteries", worldState -> {
            var state = worldState.getBlockState();
            for (var entry : GTCEuAPI.PSS_BATTERIES.entrySet()) {
                if (state.is(entry.getValue().get())) {
                    return null;
                }
            }
            return Predicates.PLACEHOLDER;
        }, GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                .map(e -> new BlockInfo(e.getValue().get().defaultBlockState()))
                .toList())
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));

        /*
         * return new TraceabilityPredicate(blockWorldState -> {
         * BlockState state = blockWorldState.getBlockState();
         * for (Map.Entry<IBatteryData, Supplier<BatteryBlock>> entry : GTCEuAPI.PSS_BATTERIES.entrySet()) {
         * if (state.is(entry.getValue().get())) {
         * IBatteryData battery = entry.getKey();
         * // Allow unfilled batteries in the structure, but do not add them to match context.
         * // This lets you use empty batteries as "filler slots" for convenience if desired.
         * if (battery.getTier() != -1 && battery.getCapacity() > 0) {
         * String key = PMC_BATTERY_HEADER + battery.getBatteryName();
         * PowerSubstationMachine.BatteryMatchWrapper wrapper = blockWorldState.getMatchContext().get(key);
         * if (wrapper == null) wrapper = new PowerSubstationMachine.BatteryMatchWrapper(battery);
         * blockWorldState.getMatchContext().set(key, wrapper.increment());
         * }
         * return true;
         * }
         * }
         * return false;
         * }, () -> GTCEuAPI.PSS_BATTERIES.entrySet().stream()
         * .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
         * .map(entry -> new BlockInfo(entry.getValue().get().defaultBlockState(), null))
         * .toArray(BlockInfo[]::new))
         * .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
         */
    }

    public static @Nullable PatternPredicate dataHatchPredicate() {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            // TODO xor predicate matching :)
            return new PatternPredicate(state -> {
                Block block = state.retrieveCurrentBlockState().getBlock();
                if (PartAbility.DATA_ACCESS.isApplicable(block) ||
                        PartAbility.OPTICAL_DATA_RECEPTION.isApplicable(block)) {
                    return null;
                }
                List<Block> blocks = new ArrayList<>(
                        List.of(PartAbility.DATA_ACCESS.getAllBlocks().toArray(new Block[0])));
                blocks.addAll(PartAbility.OPTICAL_DATA_RECEPTION.getAllBlocks());
                return new BlockMatchingError(state.getBlockPos(), blocks);
            }, Stream
                    .concat(PartAbility.DATA_ACCESS.getAllBlocks().stream(),
                            PartAbility.OPTICAL_DATA_RECEPTION.getAllBlocks().stream())
                    .map(BlockInfo::fromBlock).toList()).setExactLimit(1);
        }
        return null;
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static PatternPredicate frames(Material... frameMaterials) {
        var frameBlocks = Arrays.stream(frameMaterials)
                .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                .filter(Objects::nonNull)
                .filter(RegistryEntry::isPresent)
                .map(RegistryEntry::get)
                .toArray(Block[]::new);
        return blocks(frameBlocks)
                .or(new PatternPredicate(blockWorldState -> {
                    BlockEntity tileEntity = blockWorldState.getBlockEntity();
                    if (!(tileEntity instanceof IPipeNode<?, ?> pipeNode)) {
                        return Predicates.PLACEHOLDER;
                    }
                    return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial()) ? null :
                            Predicates.PLACEHOLDER;
                }, Arrays.stream(frameBlocks).map(BlockInfo::fromBlock).toList()));
    }
}
