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
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class Predicates {

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
        ArrayList<MetaMachineBlock> machineBlocks = new ArrayList<>(definitions.length);
        for (var definition : definitions) {
            if (definition != null) {
                machineBlocks.add(definition.get());
            }
        }
        return blocks(machineBlocks.toArray(MetaMachineBlock[]::new));
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

    public static PatternPredicate custom(Function<CurrentBlockInfo, PatternError> predicate,
                                          @NotNull List<BlockInfo> candidates) {
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

        return blocks(debugName, Arrays.stream(abilities).map(PartAbility::getAllBlocks).flatMap(Collection::stream)
                .toArray(Block[]::new));
    }

    public static PatternPredicate ability(PartAbility ability, int... tiers) {
        StringJoiner sb = new StringJoiner("-");
        for (int tier : tiers) {
            sb.add(GTValues.VN[tier]);
        }
        String debugName = ability.getName() + sb;

        return blocks(debugName,
                (tiers.length == 0 ? ability.getAllBlocks() : ability.getBlocks(tiers)).toArray(Block[]::new));
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
                            .setMaxGlobalLimited(2).setPreviewCount(1));
                    break;
                }
            }
        }
        if (checkEnergyOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(EURecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.OUTPUT_ENERGY).setMinGlobalLimited(1)
                            .setMaxGlobalLimited(2).setPreviewCount(1));
                    break;
                }
            }
        }
        if (checkItemIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1));
                    break;
                }
            }
        }
        if (checkItemOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1));
                    break;
                }
            }
        }
        if (checkFluidIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1));
                    break;
                }
            }
        }
        if (checkFluidOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1));
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
                    .setMaxGlobalLimited(1));
        }
        if (checkMuffler) {
            predicate = predicate.or(abilities(PartAbility.MUFFLER).setMinGlobalLimited(1).setMaxGlobalLimited(1));
        }
        if (checkParallel) {
            predicate = predicate.or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1));
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
            return PatternError.PLACEHOLDER;
        }, GTCEuAPI.HEATING_COILS.entrySet().stream()
                /*
                 * .filter(e -> !map.containsKey("coilTier") ||
                 * e.getKey().getTier() == GTStringUtils.parseInt(map.get("coilTier"), 1))
                 */
                // sort to make autogenerated jei previews not pick random coils each game load
                .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                .map(e -> new BlockInfo(e.getValue().get()))
                .toList())
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
    }

    public static PatternPredicate cleanroomFilters() {
        return new PatternPredicate("Cleanroom Filters", worldState -> {
            var blockState = worldState.getBlockState();
            for (var entry : GTCEuAPI.CLEANROOM_FILTERS.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    return null;
                }
            }
            return PatternError.PLACEHOLDER;
        }, GTCEuAPI.CLEANROOM_FILTERS.values().stream()
                .map(e -> BlockInfo
                        .fromBlockState(e.get().defaultBlockState()))
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
            return PatternError.PLACEHOLDER;
        }, GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                .map(e -> new BlockInfo(e.getValue().get().defaultBlockState(), null))
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

    public static PatternPredicate dataHatchPredicate(PatternPredicate def) {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            return abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                    .setExactLimit(1)
                    .or(def);
        }
        return def;
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
                        return PatternError.PLACEHOLDER;
                    }
                    return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial()) ? null :
                            PatternError.PLACEHOLDER;
                }));
    }
}
