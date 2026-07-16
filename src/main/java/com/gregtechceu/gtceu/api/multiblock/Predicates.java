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
import com.gregtechceu.gtceu.api.multiblock.error.PartAbilityError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.PlaceholderError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

    @Deprecated
    public static MultiPredicate controller(MultiPredicate predicate) {
        return predicate.setController(true);
    }

    public static MultiPredicate states(BlockState... allowedStates) {
        return states("States", allowedStates);
    }

    public static MultiPredicate states(@Nullable String debugName, BlockState... allowedStates) {
        List<BlockState> states = new ArrayList<>();
        for (BlockState state : allowedStates) {
            states.add(state);
            if (state.getBlock() instanceof ActiveBlock block) {
                states.add(block.changeActive(state, !block.isActive(state)));
            }
        }
        return customPredicate(debugName,
                ctx -> states.contains(ctx.state()) || ctx.error(PLACEHOLDER),
                states.stream().map(BlockInfo::fromBlockState),
                builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    states.forEach(state -> joiner.add(blockToString(state)));
                    builder.append(joiner);
                });
    }

    public static MultiPredicate blocks(Block block) {
        return customPredicate("Block",
                ctx -> ctx.state().is(block) || ctx.error(new BlockMatchingError(ctx.pos(), List.of(block))),
                Stream.of(BlockInfo.fromBlock(block)),
                builder -> builder.append(blockToString(block)));
    }

    public static MultiPredicate blocks(Block... blocks) {
        return blocks("Blocks", blocks);
    }

    public static MultiPredicate blocks(@Nullable String debugName, Block... blocks) {
        return blocks(debugName, Arrays.stream(blocks));
    }

    public static MultiPredicate blocks(@Nullable String debugName,
                                        Stream<Block> blocks) {
        List<Block> blockList = blocks.toList();
        return blocks(debugName, blockList, blockList.stream());
    }

    public static MultiPredicate blocks(@Nullable String debugName,
                                        List<Block> blocks,
                                        Stream<Block> candidates) {
        return customPredicate(debugName, ctx -> {
            for (var block : blocks) {
                if (ctx.state().is(block)) return true;
            }
            return ctx.error(new BlockMatchingError(ctx.pos(), blocks));
        }, candidates.map(BlockInfo::fromBlock), builder -> {
            StringJoiner joiner = new StringJoiner(", ");
            blocks.forEach(block -> joiner.add(blockToString(block)));
            builder.append(joiner);
        });
    }

    private static String blockToString(BlockState blockState) {
        return blockToString(blockState.getBlock());
    }

    private static String blockToString(Block block) {
        return ForgeRegistries.BLOCKS.getDelegate(block)
                .map(r -> r.key().location().toString())
                .orElse("unknown block");
    }

    public static MultiPredicate machines(MachineDefinition... definitions) {
        Validate.noNullElements(definitions, "MachineDefinition array has null element at index %s");
        return blocks(Arrays.stream(definitions).map(MachineDefinition::get).toArray(MetaMachineBlock[]::new));
    }

    public static MultiPredicate blockTag(TagKey<Block> tag) {
        return customPredicate("BlockTag",
                ctx -> ctx.state().is(tag),
                Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                        .getTag(tag).stream()
                        .map(BlockInfo::fromBlock),
                builder -> builder.append(tag.location()));
    }

    public static MultiPredicate fluids(Fluid... fluids) {
        return customPredicate("Fluids",
                ctx -> ArrayUtils.contains(fluids, ctx.fluid()) || ctx.error(PLACEHOLDER),
                Arrays.stream(fluids).map(BlockInfo::fromFluid),
                builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    for (Fluid fluid : fluids) {
                        joiner.add(ForgeRegistries.FLUIDS.getDelegate(fluid)
                                .map(r -> r.key().location().toString())
                                .orElse("unknown"));
                    }
                    builder.append(joiner);
                });
    }

    public static MultiPredicate fluidTag(TagKey<Fluid> tag) {
        return customPredicate("FluidTag",
                ctx -> ctx.fluidState().is(tag),
                Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                        .getTag(tag).stream()
                        .map(BlockInfo::fromFluid),
                builder -> builder.append(tag.location()));
    }

    @Deprecated
    public static MultiPredicate customFunction(Function<CurrentBlockInfo, @Nullable PatternError> predicate,
                                                @Nullable List<BlockInfo> candidates) {
        return customPredicate(ctx -> {
            PatternError error = predicate.apply(ctx.getCurrentBlockInfo());
            return error == null || ctx.error(error);
        }, Objects.<List<BlockInfo>>requireNonNullElse(candidates, Collections.emptyList()).stream());
    }

    public static MultiPredicate customPredicate(Predicate<PredicateContext> predicate,
                                                 Stream<BlockInfo> candidates) {
        return customPredicate(null, predicate, candidates);
    }

    public static MultiPredicate customPredicate(@Nullable String debugName,
                                                 Predicate<PredicateContext> predicate,
                                                 Stream<BlockInfo> candidates) {
        return customPredicate(debugName, predicate, candidates, null);
    }

    public static MultiPredicate customPredicate(@Nullable String debugName,
                                                 Predicate<PredicateContext> predicate,
                                                 Stream<BlockInfo> candidates,
                                                 @Nullable Consumer<StringBuilder> contents) {
        return BasePredicate.create(debugName, predicate, candidates, contents);
    }

    public static MultiPredicate any() {
        return MultiPredicate.ANY;
    }

    public static MultiPredicate air() {
        return MultiPredicate.AIR;
    }

    public static MultiPredicate abilities(PartAbility ability) {
        return customPredicate("Ability",
                ctx -> ability.isApplicable(ctx.state().getBlock()) ||
                        ctx.error(new PartAbilityError(ctx.pos(), ability)),
                ability.getAllBlocks().stream().map(BlockInfo::fromBlock),
                builder -> builder.append(ability.getName()));
    }

    public static MultiPredicate abilities(PartAbility... abilities) {
        return customPredicate("Abilities",
                ctx -> {
                    for (PartAbility ability : abilities) {
                        if (ability.isApplicable(ctx.state().getBlock())) {
                            return true;
                        }
                        ctx.error(new PartAbilityError(ctx.pos(), ability));
                    }
                    return false;
                },
                Arrays.stream(abilities)
                        .flatMap(a -> a.getAllBlocks().stream())
                        .map(BlockInfo::fromBlock),
                builder -> {
                    StringJoiner sb = new StringJoiner(", ");
                    for (PartAbility ability : abilities) {
                        sb.add(ability.getName());
                    }
                    builder.append(sb);
                });
    }

    public static MultiPredicate ability(PartAbility ability, int... tiers) {
        StringJoiner sb = new StringJoiner("-");
        for (int tier : tiers) {
            sb.add(GTValues.VN[tier]);
        }
        return customPredicate("Ability[" + sb + "]",
                ctx -> ability.isApplicable(ctx.state().getBlock()) ||
                        ctx.error(new PartAbilityError(ctx.pos(), ability)),
                ability.getBlocks(tiers).stream().map(BlockInfo::fromBlock));
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
                    predicate = predicate.or(abilities(PartAbility.INPUT_ENERGY)
                            .setMinCount(1).setMaxCount(2)
                            .setPreviewCount(1).setPriority(1));
                    break;
                }
            }
        }
        if (checkEnergyOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(EURecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.OUTPUT_ENERGY)
                            .setMinCount(1).setMaxCount(2)
                            .setPreviewCount(1).setPriority(1));
                    break;
                }
            }
        }
        if (checkItemIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.IMPORT_ITEMS)
                            .setPreviewCount(1).setPriority(2));
                    break;
                }
            }
        }
        if (checkItemOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.EXPORT_ITEMS)
                            .setPreviewCount(1).setPriority(2));
                    break;
                }
            }
        }
        if (checkFluidIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.IMPORT_FLUIDS)
                            .setPreviewCount(1).setPriority(3));
                    break;
                }
            }
        }
        if (checkFluidOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                    predicate = predicate.or(abilities(PartAbility.EXPORT_FLUIDS)
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
            predicate = predicate.or(abilities(PartAbility.MAINTENANCE)
                    .setMinCount(ConfigHolder.INSTANCE.machines.enableMaintenance ? 1 : 0)
                    .setMaxCount(1)
                    .setPriority(1));
        }
        if (checkMuffler) {
            predicate = predicate.or(abilities(PartAbility.MUFFLER)
                    .setExactLimit(1)
                    .setPriority(2));
        }
        if (checkParallel) {
            predicate = predicate.or(abilities(PartAbility.PARALLEL_HATCH)
                    .setMaxCount(1)
                    .setPreviewCount(1)
                    .setPriority(3));
        }
        return predicate;
    }

    public static MultiPredicate heatingCoils() {
        return blocks("HeatingCoils",
                GTCEuAPI.HEATING_COILS.values().stream().map(Supplier::get))
                // .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"))
                .setPriority(0);
    }

    public static MultiPredicate cleanroomFilters() {
        return blocks("CleanroomFilters",
                GTCEuAPI.CLEANROOM_FILTERS.values()
                        .stream().map(Supplier::get).toList(),
                GTCEuAPI.CLEANROOM_FILTERS.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getCleanroomType().getTier()))
                        .map(entry -> entry.getValue().get()))
        // .addTooltips(Component.translatable("gtceu.multiblock.pattern.cleanroom"))
        ;
    }

    public static MultiPredicate powerSubstationBatteries() {
        return blocks("PSS-Batteries",
                GTCEuAPI.PSS_BATTERIES.values()
                        .stream().map(Supplier::get).map(Block.class::cast).toList(),
                GTCEuAPI.PSS_BATTERIES.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> e.getValue().get()))
        // .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"))
        ;
    }

    public static @Nullable MultiPredicate dataHatchPredicate() {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            // TODO xor predicate matching :)
            return abilities(PartAbility.DATA_ACCESS)
                    .xor(abilities(PartAbility.OPTICAL_DATA_RECEPTION))
                    .setExactLimit(1)
                    .setPriority(1);
            // return abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
            // .setExactLimit(1)
            // .setPriority(1);
        }
        // this really should not be null
        return null;
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static MultiPredicate frames(Material... frameMaterials) {
        var frameBlocks = Arrays.stream(frameMaterials)
                .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                .filter(obj -> Objects.nonNull(obj) && obj.isPresent())
                .map(RegistryEntry::get)
                .toArray(Block[]::new);
        return blocks("Frames", frameBlocks)
                .or(framedPipes(frameMaterials, frameBlocks));
    }

    public static MultiPredicate framedPipes(Material[] frameMaterials, Block[] frameBlocks) {
        return customPredicate("FramedPipes", ctx -> {
            BlockEntity tileEntity = ctx.blockEntity();
            if (!(tileEntity instanceof IPipeNode<?, ?> pipeNode)) {
                return ctx.error(PLACEHOLDER);
            }
            return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial()) ||
                    ctx.error(PLACEHOLDER);
        }, Arrays.stream(frameBlocks).map(BlockInfo::fromBlock));
    }
}
