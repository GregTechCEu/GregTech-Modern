package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.error.PartAbilityError;
import com.gregtechceu.gtceu.api.multiblock.error.PlaceholderError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Predicates {

    /**
     * Return this for your pattern errors if you want them to be a default error with the pos of the BlockWorldState
     * and candidates of the simple predicate's error.
     */
    public static final PlaceholderError PLACEHOLDER = new PlaceholderError(BlockPos.ZERO, Collections.emptyList());

    public static MultiPredicate controller(MultiblockMachineDefinition def) {
        return blocks(def.getBlock()).setController(true);
    }

    /// @deprecated use {@link #controller(MultiblockMachineDefinition)} instead
    @Deprecated
    public static MultiPredicate controller(MultiPredicate predicate) {
        return predicate.setController(true);
    }

    public static MultiPredicate states(BlockState... allowedStates) {
        return states(null, allowedStates);
    }

    @RemapForJS("statesDebug")
    public static MultiPredicate states(@Nullable String debugName, BlockState... allowedStates) {
        List<BlockState> states = new ArrayList<>();
        BooleanProperty activeProp = GTBlockStateProperties.ACTIVE;
        for (BlockState state : allowedStates) {
            states.add(state);
            if (state.hasProperty(activeProp)) {
                states.add(state.setValue(activeProp, !state.getValue(activeProp)));
            }
        }
        return builder(debugName == null ? "States" : debugName)
                .predicate(ctx -> states.contains(ctx.state()))
                // .errorConsumer(ctx -> PLACEHOLDER)
                .candidates(states.stream().map(BlockInfo::fromBlockState))
                .contents(builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    states.forEach(state -> joiner.add(blockToString(state)));
                    builder.append(joiner);
                })
                .toMultiPredicate();
    }

    @HideFromJS
    public static MultiPredicate blocks(Block block) {
        return builder("Block")
                .predicate(ctx -> ctx.state().is(block))
                .errorFunction(ctx -> new BlockMatchingError(ctx.pos(), List.of(block)))
                .candidates(Stream.of(BlockInfo.fromBlock(block)))
                .contents(builder -> builder.append(blockToString(block)))
                .toMultiPredicate();
    }

    public static MultiPredicate blocks(Block... blocks) {
        return blocks(null, blocks);
    }

    @HideFromJS
    public static MultiPredicate blocks(Supplier<Block> block) {
        return blocks(block.get());
    }

    @SafeVarargs
    @HideFromJS
    public static MultiPredicate blocks(Supplier<Block>... blocks) {
        return blocks(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    @RemapForJS("blocksDebug")
    public static MultiPredicate blocks(@Nullable String debugName, Block... blocks) {
        return blocks(debugName, Arrays.stream(blocks));
    }

    @HideFromJS
    public static MultiPredicate blocks(@Nullable String debugName,
                                        Stream<Block> blocks) {
        List<Block> blockList = blocks.toList();
        return blocks(debugName, blockList, blockList.stream());
    }

    @HideFromJS
    public static MultiPredicate blocks(@Nullable String debugName,
                                        List<Block> blocks,
                                        Stream<Block> candidates) {
        return builder(debugName == null ? "Blocks" : debugName)
                .predicate(ctx -> {
                    for (var block : blocks) {
                        if (ctx.state().is(block)) return true;
                    }
                    return false;
                })
                .errorFunction(ctx -> new BlockMatchingError(ctx.pos(), blocks))
                .candidates(candidates.map(BlockInfo::fromBlock))
                .contents(builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    blocks.forEach(block -> joiner.add(blockToString(block)));
                    builder.append(joiner);
                })
                .toMultiPredicate();
    }

    // todo these two methods below should be moved into a util class
    private static String blockToString(BlockState blockState) {
        return blockToString(blockState.getBlock());
    }

    private static String blockToString(Block block) {
        return BuiltInRegistries.BLOCK.getHolder(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow())
                .map(r -> r.key().location().toString())
                .orElse("unknown block");
    }

    public static MultiPredicate machines(MachineEntry<MachineDefinition> definition) {
        return machines(definition.value());
    }

    @SafeVarargs
    public static MultiPredicate machines(MachineEntry<MachineDefinition>... definitions) {
        return machines(Arrays.stream(definitions).map(Holder::value).toArray(MachineDefinition[]::new));
    }

    public static MultiPredicate machines(@Nullable MachineDefinition... definitions) {
        List<Block> blocks = new ArrayList<>();
        for (MachineDefinition definition : definitions) {
            if (definition != null) {
                blocks.add(definition.getBlock());
            }
        }
        if (blocks.isEmpty()) {
            throw new IllegalStateException("All machine definitions are null!");
        }
        return blocks("MachineDefinitions", blocks, blocks.stream());
    }

    public static MultiPredicate blockTag(TagKey<Block> tag) {
        Objects.requireNonNull(tag, "Block tag cannot be null");
        return builder("BlockTag")
                .blockTag(tag)
                .predicate(ctx -> ctx.state().is(tag))
                .errorFunction(ctx -> new BlockMatchingError(ctx.pos(),
                        BuiltInRegistries.BLOCK.getTag(tag).orElseThrow()
                                .stream().map(Holder::value).toList()))
                .toMultiPredicate();
    }

    public static MultiPredicate fluids(Fluid... fluids) {
        return fluids(null, fluids);
    }

    @RemapForJS("fluidsDebug")
    public static MultiPredicate fluids(@Nullable String debugName, Fluid... fluids) {
        Validate.noNullElements(fluids, "Fluids array has null element at index %s");
        return builder(debugName == null ? "Fluids" : debugName)
                .predicate(ctx -> ArrayUtils.contains(fluids, ctx.fluid()))
                // .errorConsumer(ctx -> ctx.appendError(PLACEHOLDER))
                .candidates(Arrays.stream(fluids).map(BlockInfo::fromFluid))
                .contents(builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    for (Fluid fluid : fluids) {
                        joiner.add(BuiltInRegistries.FLUID
                                .getHolder(BuiltInRegistries.FLUID.getResourceKey(fluid).orElseThrow())
                                .map(r -> r.key().location().toString())
                                .orElse("unknown"));
                    }
                    builder.append(joiner);
                })
                .toMultiPredicate();
    }

    public static MultiPredicate fluidTag(TagKey<Fluid> tag) {
        Objects.requireNonNull(tag, "Fluid tag cannot be null");
        return builder("FluidTag")
                .predicate(ctx -> ctx.fluidState().is(tag))
                // .errorConsumer(ctx -> ctx.appendError(PLACEHOLDER))
                .fluidTag(tag)
                .toMultiPredicate();
    }

    public static MultiPredicate any() {
        return MultiPredicate.ANY;
    }

    public static MultiPredicate air() {
        return MultiPredicate.AIR;
    }

    public static MultiPredicate abilities(PartAbility ability) {
        return builder("Ability")
                .predicate(ctx -> ability.isApplicable(ctx.state().getBlock()))
                .errorFunction(ctx -> new PartAbilityError(ctx.pos(), ability))
                .candidates(ability.getAllBlocks().stream().map(BlockInfo::fromBlock))
                .contents(builder -> builder.append(ability.getName()))
                .toMultiPredicate();
    }

    public static MultiPredicate abilities(PartAbility... abilities) {
        List<BasePredicate> predicates = new ArrayList<>();
        for (PartAbility ability : abilities) {
            Validate.noNullElements(ability.getAllBlocks(), "Ability %s has null block at index %s", ability.getName());
            predicates.add(builder("Ability")
                    .predicate(ctx -> ability.isApplicable(ctx.state().getBlock()))
                    .errorFunction(ctx -> new PartAbilityError(ctx.pos(), ability))
                    .candidates(ability.getAllBlocks().stream().map(BlockInfo::fromBlock))
                    .contents(builder -> builder.append(ability.getName()))
                    .build());
        }
        return MultiPredicate.or(predicates);
    }

    public static MultiPredicate ability(PartAbility ability, int... tiers) {
        StringJoiner sb = new StringJoiner("-");
        for (int tier : tiers) {
            sb.add(GTValues.VN[tier]);
        }
        return builder("TieredAbility")
                .predicate(ctx -> ability.getBlocks(tiers).contains(ctx.state().getBlock()))
                .errorFunction(ctx -> new PartAbilityError(ctx.pos(), ability))
                .candidates(ability.getBlocks(tiers).stream().map(BlockInfo::fromBlock))
                .contents(builder -> builder.append(ability.getName())
                        .append("[").append(sb).append("]"))
                .toMultiPredicate();
    }

    public static MultiPredicate autoAbilities(GTRecipeType... recipeType) {
        return autoAbilities(recipeType, true, true, true, true, true, true);
    }

    public static MultiPredicate autoAbilities(GTRecipeType[] recipeType,
                                               boolean checkEnergyIn, boolean checkEnergyOut,
                                               boolean checkItemIn, boolean checkItemOut,
                                               boolean checkFluidIn, boolean checkFluidOut) {
        MultiPredicate predicate = MultiPredicate.empty();
        if (checkEnergyIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(EURecipeCapability.CAP) > 0) {
                    predicate = predicate.and(abilities(PartAbility.INPUT_ENERGY)
                            .setMinCount(1).setMaxCount(2)
                            .setPreviewCount(1).setPriority(1));
                    break;
                }
            }
        }
        if (checkEnergyOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(EURecipeCapability.CAP) > 0) {
                    predicate = predicate.and(abilities(PartAbility.OUTPUT_ENERGY)
                            .setMinCount(1).setMaxCount(2)
                            .setPreviewCount(1).setPriority(1));
                    break;
                }
            }
        }
        if (checkItemIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.and(abilities(PartAbility.IMPORT_ITEMS)
                            .setPreviewCount(1).setPriority(2));
                    break;
                }
            }
        }
        if (checkItemOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.and(abilities(PartAbility.EXPORT_ITEMS)
                            .setPreviewCount(1).setPriority(2));
                    break;
                }
            }
        }
        if (checkFluidIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.and(abilities(PartAbility.IMPORT_FLUIDS)
                            .setPreviewCount(1).setPriority(3));
                    break;
                }
            }
        }
        if (checkFluidOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.and(abilities(PartAbility.EXPORT_FLUIDS)
                            .setPreviewCount(1).setPriority(3));
                    break;
                }
            }
        }
        return predicate;
    }

    public static MultiPredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler,
                                               boolean checkParallel) {
        MultiPredicate predicate = MultiPredicate.empty();
        if (checkMaintenance) {
            predicate = predicate.and(abilities(PartAbility.MAINTENANCE)
                    .setMinCount(ConfigHolder.INSTANCE.machines.enableMaintenance ? 1 : 0)
                    .setMaxCount(1)
                    .setPriority(1));
        }
        if (checkMuffler) {
            predicate = predicate.and(abilities(PartAbility.MUFFLER)
                    .setExactLimit(1)
                    .setPriority(2));
        }
        if (checkParallel) {
            predicate = predicate.and(abilities(PartAbility.PARALLEL_HATCH)
                    .setMaxCount(1)
                    .setPreviewCount(1)
                    .setPriority(3));
        }
        return predicate;
    }

    public static MultiPredicate heatingCoils() {
        return blocks("HeatingCoils",
                GTCEuAPI.HEATING_COILS.values().stream()
                        .<Block>map(Supplier::get).toList(),
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> e.getValue().get()))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"))
                .setPriority(0);
    }

    public static MultiPredicate cleanroomFilters() {
        return blocks("CleanroomFilters",
                GTCEuAPI.CLEANROOM_FILTERS.values().stream().map(Supplier::get).toList(),
                GTCEuAPI.CLEANROOM_FILTERS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getCleanroomType().getTier()))
                        .map(entry -> entry.getValue().get()))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.filters"));
    }

    public static MultiPredicate powerSubstationBatteries() {
        return blocks("PSS-Batteries",
                GTCEuAPI.PSS_BATTERIES.values()
                        .stream().map(Supplier::get).map(Block.class::cast).toList(),
                GTCEuAPI.PSS_BATTERIES.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> e.getValue().get()))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
    }

    public static @Nullable MultiPredicate dataHatchPredicate() {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            return abilities(PartAbility.DATA_ACCESS)
                    .xor(abilities(PartAbility.OPTICAL_DATA_RECEPTION))
                    .setExactLimit(1)
                    .setPriority(1);
        }
        return null;
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static MultiPredicate frames(Material... frameMaterials) {
        var frameBlocks = Arrays.stream(frameMaterials)
                .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                .filter(obj -> Objects.nonNull(obj) && obj.isBound())
                .map(RegistryEntry::get)
                .toArray(Block[]::new);
        return blocks("Frames", frameBlocks)
                .or(framedPipes(frameMaterials, frameBlocks));
    }

    public static MultiPredicate framedPipes(Material[] frameMaterials, Block[] frameBlocks) {
        return builder("FramedPipes")
                .predicate(ctx -> {
                    BlockEntity tileEntity = ctx.blockEntity();
                    if (!(tileEntity instanceof IPipeNode<?, ?> pipeNode)) {
                        return false;
                    }
                    return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial());
                })
                // .errorConsumer(ctx -> PLACEHOLDER)
                .candidates(Arrays.stream(frameBlocks).map(BlockInfo::fromBlock))
                .contents(builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    Arrays.stream(frameBlocks).forEach(block -> joiner.add(blockToString(block)));
                    builder.append(joiner);
                })
                .toMultiPredicate();
    }

    public static PredicateBuilder builder(String debugName) {
        return new PredicateBuilder(debugName);
    }

    public static PredicateBuilder builder() {
        return builder("Predicate");
    }
}
