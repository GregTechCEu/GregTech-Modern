package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.*;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties.ACTIVE;
import static com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine.PMC_BATTERY_HEADER;

public class Predicates {

    public static TraceabilityPredicate controller(TraceabilityPredicate predicate) {
        return predicate.setController();
    }

    public static TraceabilityPredicate states(BlockState... allowedStates) {
        var candidates = new ArrayList<BlockState>();
        for (BlockState state : allowedStates) {
            candidates.add(state);
            if (state.hasProperty(ACTIVE)) {
                candidates.add(state.setValue(ACTIVE, !state.getValue(ACTIVE)));
            }
        }
        return new TraceabilityPredicate(new PredicateStates(candidates.toArray(BlockState[]::new)));
    }

    public static TraceabilityPredicate blocks(Block... blocks) {
        return new TraceabilityPredicate(new PredicateBlocks(blocks));
    }

    public static TraceabilityPredicate blocks(IMachineBlock... blocks) {
        return new TraceabilityPredicate(
                new PredicateBlocks(Arrays.stream(blocks).map(IMachineBlock::self).toArray(Block[]::new)));
    }

    public static TraceabilityPredicate machines(MachineDefinition... definitions) {
        ArrayList<IMachineBlock> machineBlocks = new ArrayList<>(definitions.length);
        for (var definition : definitions) {
            if (definition != null) {
                machineBlocks.add(definition.get());
            }
        }
        return blocks(machineBlocks.toArray(IMachineBlock[]::new));
    }

    public static TraceabilityPredicate blockTag(TagKey<Block> tag) {
        return new TraceabilityPredicate(new PredicateBlockTag(tag));
    }

    public static TraceabilityPredicate fluids(Fluid... fluids) {
        return new TraceabilityPredicate(new PredicateFluids(fluids));
    }

    public static TraceabilityPredicate fluidTag(TagKey<Fluid> tag) {
        return new TraceabilityPredicate(new PredicateFluidTag(tag));
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
        return blocks(Arrays.stream(abilities).map(PartAbility::getAllBlocks).flatMap(Collection::stream)
                .toArray(Block[]::new));
    }

    public static TraceabilityPredicate ability(PartAbility ability, int... tiers) {
        return blocks((tiers.length == 0 ? ability.getAllBlocks() : ability.getBlocks(tiers)).toArray(Block[]::new));
    }

    public static TraceabilityPredicate autoAbilities(GTRecipeType... recipeType) {
        return autoAbilities(recipeType, true, true, true, true, true, true);
    }

    public static TraceabilityPredicate autoAbilities(GTRecipeType[] recipeType,
                                                      boolean checkEnergyIn,
                                                      boolean checkEnergyOut,
                                                      boolean checkItemIn,
                                                      boolean checkItemOut,
                                                      boolean checkFluidIn,
                                                      boolean checkFluidOut) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();

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

    public static TraceabilityPredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler,
                                                      boolean checkParallel) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();
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

    public static TraceabilityPredicate heatingCoils() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (Map.Entry<ICoilType, Supplier<CoilBlock>> entry : GTCEuAPI.HEATING_COILS.entrySet()) {
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
        }, () -> GTCEuAPI.HEATING_COILS.entrySet().stream()
                // sort to make autogenerated jei previews not pick random coils each game load
                .sorted(Comparator.comparingInt(value -> value.getKey().getTier()))
                .map(coil -> BlockInfo.fromBlockState(coil.getValue().get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
    }

    public static TraceabilityPredicate cleanroomFilters() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (var entry : GTCEuAPI.CLEANROOM_FILTERS.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    var stats = entry.getKey();
                    Object currentCoil = blockWorldState.getMatchContext().getOrPut("FilterType", stats);
                    if (!currentCoil.equals(stats)) {
                        blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.filters"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> GTCEuAPI.CLEANROOM_FILTERS.values().stream()
                .map(blockSupplier -> BlockInfo.fromBlockState(blockSupplier.get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.filters"));
    }

    public static TraceabilityPredicate powerSubstationBatteries() {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState state = blockWorldState.getBlockState();
            for (Map.Entry<IBatteryData, Supplier<BatteryBlock>> entry : GTCEuAPI.PSS_BATTERIES.entrySet()) {
                if (state.is(entry.getValue().get())) {
                    IBatteryData battery = entry.getKey();
                    // Allow unfilled batteries in the structure, but do not add them to match context.
                    // This lets you use empty batteries as "filler slots" for convenience if desired.
                    if (battery.getTier() != -1 && battery.getCapacity() > 0) {
                        String key = PMC_BATTERY_HEADER + battery.getBatteryName();
                        PowerSubstationMachine.BatteryMatchWrapper wrapper = blockWorldState.getMatchContext().get(key);
                        if (wrapper == null) wrapper = new PowerSubstationMachine.BatteryMatchWrapper(battery);
                        blockWorldState.getMatchContext().set(key, wrapper.increment());
                    }
                    return true;
                }
            }
            return false;
        }, () -> GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                .map(entry -> new BlockInfo(entry.getValue().get().defaultBlockState(), null))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
    }

    public static TraceabilityPredicate dataHatchPredicate(TraceabilityPredicate def) {
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
    public static TraceabilityPredicate frames(Material... frameMaterials) {
        var frameBlocks = Arrays.stream(frameMaterials)
                .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                .filter(Objects::nonNull)
                .filter(RegistryEntry::isPresent)
                .map(RegistryEntry::get)
                .toArray(Block[]::new);
        return blocks(frameBlocks)
                .or(new TraceabilityPredicate(blockWorldState -> {
                    BlockEntity tileEntity = blockWorldState.getTileEntity();
                    if (!(tileEntity instanceof IPipeNode<?, ?> pipeNode)) {
                        return false;
                    }
                    return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial());
                }, () -> Arrays.stream(frameMaterials)
                        .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                        .filter(Objects::nonNull).filter(RegistryEntry::isPresent).map(RegistryEntry::get)
                        .map(BlockInfo::fromBlock).toArray(BlockInfo[]::new)));
    }
}
