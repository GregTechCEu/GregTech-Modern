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

    public static BasePredicate controller(MultiblockMachineDefinition def) {
        return controller(blocks(def.getBlock()));
    }

    public static BasePredicate controller(BasePredicate predicate) {
        return new BasePredicate() {

            @Override
            public boolean test(PredicateContext ctx) {
                return predicate.test(ctx);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return predicate.getCandidates();
            }

            @Override
            public StringBuilder appendType(StringBuilder builder) {
                return builder.append("Controller");
            }

            @Override
            protected StringBuilder appendContents(StringBuilder builder) {
                return builder.append(predicate);
            }

            @Override
            public boolean isController() {
                return true;
            }
        };
    }

    public static BasePredicate states(BlockState... allowedStates) {
        return states("States", allowedStates);
    }

    public static BasePredicate states(@Nullable String debugName, BlockState... allowedStates) {
        List<BlockState> states = new ArrayList<>();
        for (BlockState state : allowedStates) {
            states.add(state);
            if (state.getBlock() instanceof ActiveBlock block) {
                states.add(block.changeActive(state, !block.isActive(state)));
            }
        }
        return customPredicate(debugName,
                ctx -> states.contains(ctx.state()) || ctx.error(PLACEHOLDER),
                () -> states.stream().map(BlockInfo::fromBlockState),
                builder -> {
                    StringJoiner joiner = new StringJoiner(", ");
                    states.forEach(state -> joiner.add(blockToString(state)));
                    builder.append(joiner);
                });
    }

    public static BasePredicate blocks(Block block) {
        return customPredicate("Block",
                ctx -> ctx.state().is(block) || ctx.error(new BlockMatchingError(ctx.pos(), List.of(block))),
                () -> Stream.of(BlockInfo.fromBlock(block)),
                builder -> builder.append(blockToString(block)));
    }

    public static BasePredicate blocks(Block... blocks) {
        return blocks("Blocks", blocks);
    }

    public static BasePredicate blocks(@Nullable String debugName, Block... blocks) {
        return blocks(debugName, () -> Arrays.stream(blocks));
    }

    public static BasePredicate blocks(@Nullable String debugName,
                                       Supplier<Stream<Block>> blocks) {
        return blocks(debugName, blocks, blocks);
    }

    public static BasePredicate blocks(@Nullable String debugName,
                                       Supplier<Stream<Block>> blocks,
                                       Supplier<Stream<Block>> candidates) {
        return customPredicate(debugName, ctx -> {
            var blockList = blocks.get().toList();
            for (var block : blockList) {
                if (ctx.state().is(block)) return true;
            }
            return ctx.error(new BlockMatchingError(ctx.pos(), blockList));
        }, () -> candidates.get().map(BlockInfo::fromBlock), builder -> {
            StringJoiner joiner = new StringJoiner(", ");
            blocks.get().forEach(block -> joiner.add(blockToString(block)));
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

    public static BasePredicate machines(MachineDefinition... definitions) {
        Validate.noNullElements(definitions, "MachineDefinition array has null element at index %s");
        return blocks(Arrays.stream(definitions).map(MachineDefinition::get).toArray(MetaMachineBlock[]::new));
    }

    public static BasePredicate blockTag(TagKey<Block> tag) {
        return customPredicate("BlockTag",
                ctx -> ctx.state().is(tag),
                () -> Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                        .getTag(tag).stream()
                        .map(BlockInfo::fromBlock),
                builder -> builder.append(tag.location()));
    }

    public static BasePredicate fluids(Fluid... fluids) {
        return customPredicate("Fluids",
                ctx -> ArrayUtils.contains(fluids, ctx.fluid()) || ctx.error(PLACEHOLDER),
                () -> Arrays.stream(fluids).map(BlockInfo::fromFluid),
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

    public static BasePredicate fluidTag(TagKey<Fluid> tag) {
        return customPredicate("FluidTag",
                ctx -> ctx.fluidState().is(tag),
                () -> Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                        .getTag(tag).stream()
                        .map(BlockInfo::fromFluid),
                builder -> builder.append(tag.location()));
    }

    public static BasePredicate customFunction(Function<CurrentBlockInfo, @Nullable PatternError> predicate,
                                               @Nullable List<BlockInfo> candidates) {
        return customPredicate(ctx -> {
            PatternError error = predicate.apply(ctx.blockInfo());
            return error == null || ctx.error(error);
        }, Optional.ofNullable(candidates).orElse(Collections.emptyList())::stream);
    }

    public static BasePredicate customPredicate(Predicate<PredicateContext> predicate,
                                                Supplier<Stream<BlockInfo>> candidates) {
        return customPredicate(null, predicate, candidates);
    }

    public static BasePredicate customPredicate(@Nullable String debugName,
                                                Predicate<PredicateContext> predicate,
                                                Supplier<Stream<BlockInfo>> candidates) {
        return customPredicate(debugName, predicate, candidates, null);
    }

    public static BasePredicate customPredicate(@Nullable String debugName,
                                                Predicate<PredicateContext> predicate,
                                                Supplier<Stream<BlockInfo>> candidates,
                                                @Nullable Consumer<StringBuilder> contents) {
        return new BasePredicate.Custom(debugName, predicate, candidates, contents);
    }

    public static BasePredicate any() {
        return BasePredicate.ANY;
    }

    public static BasePredicate air() {
        return BasePredicate.AIR;
    }

    public static BasePredicate abilities(PartAbility ability) {
        return customPredicate("Ability",
                ctx -> ability.isApplicable(ctx.state().getBlock()) ||
                        ctx.error(new PartAbilityError(ctx.pos(), ability)),
                () -> ability.getAllBlocks().stream().map(BlockInfo::fromBlock),
                builder -> builder.append(ability.getName()));
    }

    public static BasePredicate abilities(PartAbility... abilities) {
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
                () -> Arrays.stream(abilities)
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

    public static BasePredicate ability(PartAbility ability, int... tiers) {
        StringJoiner sb = new StringJoiner("-");
        for (int tier : tiers) {
            sb.add(GTValues.VN[tier]);
        }
        return customPredicate("Ability[" + sb + "]",
                ctx -> ability.isApplicable(ctx.state().getBlock()) ||
                        ctx.error(new PartAbilityError(ctx.pos(), ability)),
                () -> ability.getBlocks(tiers).stream().map(BlockInfo::fromBlock));
    }

    public static BasePredicate autoAbilities(GTRecipeType... recipeType) {
        return autoAbilities(recipeType, true, true, true, true, true, true);
    }

    public static BasePredicate autoAbilities(GTRecipeType[] recipeType,
                                              boolean checkEnergyIn, boolean checkEnergyOut,
                                              boolean checkItemIn, boolean checkItemOut,
                                              boolean checkFluidIn, boolean checkFluidOut) {
        List<BasePredicate> predicates = new ArrayList<>();

        if (checkEnergyIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(EURecipeCapability.CAP) > 0) {
                    predicates.add(abilities(PartAbility.INPUT_ENERGY)
                            .setMinCount(1).setMaxCount(2)
                            .setPreviewCount(1).setPriority(1));
                    break;
                }
            }
        }
        if (checkEnergyOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(EURecipeCapability.CAP) > 0) {
                    predicates.add(abilities(PartAbility.OUTPUT_ENERGY)
                            .setMinCount(1).setMaxCount(2)
                            .setPreviewCount(1).setPriority(1));
                    break;
                }
            }
        }
        if (checkItemIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(ItemRecipeCapability.CAP) > 0) {
                    predicates.add(abilities(PartAbility.IMPORT_ITEMS)
                            .setPreviewCount(1).setPriority(2));
                    break;
                }
            }
        }
        if (checkItemOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(ItemRecipeCapability.CAP) > 0) {
                    predicates.add(abilities(PartAbility.EXPORT_ITEMS)
                            .setPreviewCount(1).setPriority(2));
                    break;
                }
            }
        }
        if (checkFluidIn) {
            for (var type : recipeType) {
                if (type.getMaxInputs(FluidRecipeCapability.CAP) > 0) {
                    predicates.add(abilities(PartAbility.IMPORT_FLUIDS)
                            .setPreviewCount(1).setPriority(3));
                    break;
                }
            }
        }
        if (checkFluidOut) {
            for (var type : recipeType) {
                if (type.getMaxOutputs(FluidRecipeCapability.CAP) > 0) {
                    predicates.add(abilities(PartAbility.EXPORT_FLUIDS)
                            .setPreviewCount(1).setPriority(3));
                    break;
                }
            }
        }
        return BasePredicate.or("AutoAbilities", predicates);
    }

    public static BasePredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler,
                                              boolean checkParallel) {
        List<BasePredicate> predicates = new ArrayList<>();
        if (checkMaintenance) {
            predicates.add(abilities(PartAbility.MAINTENANCE)
                    .setMinCount(ConfigHolder.INSTANCE.machines.enableMaintenance ? 1 : 0)
                    .setMaxCount(1)
                    .setPriority(1));
        }
        if (checkMuffler) {
            predicates.add(abilities(PartAbility.MUFFLER)
                    .setExactLimit(1)
                    .setPriority(2));
        }
        if (checkParallel) {
            predicates.add(abilities(PartAbility.PARALLEL_HATCH)
                    .setMaxCount(1)
                    .setPreviewCount(1)
                    .setPriority(3));
        }
        return BasePredicate.or("AutoAbilities", predicates);
    }

    public static BasePredicate heatingCoils() {
        return blocks("HeatingCoils",
                () -> GTCEuAPI.HEATING_COILS.values().stream().map(Supplier::get))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"))
                .setPriority(0);
    }

    public static BasePredicate cleanroomFilters() {
        return blocks("CleanroomFilters",
                () -> GTCEuAPI.CLEANROOM_FILTERS.values()
                        .stream().map(Supplier::get),
                () -> GTCEuAPI.CLEANROOM_FILTERS.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getCleanroomType().getTier()))
                        .map(entry -> entry.getValue().get()))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.cleanroom"));
    }

    public static BasePredicate powerSubstationBatteries() {
        return blocks("PSS-Batteries",
                () -> GTCEuAPI.PSS_BATTERIES.values()
                        .stream().map(Supplier::get),
                () -> GTCEuAPI.PSS_BATTERIES.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> e.getValue().get()))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
    }

    public static @Nullable BasePredicate dataHatchPredicate() {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            // TODO xor predicate matching :)
            return abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                    .setExactLimit(1)
                    .setPriority(1);
        }
        // this really should not be null
        return null;
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static BasePredicate frames(Material... frameMaterials) {
        var frameBlocks = Arrays.stream(frameMaterials)
                .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                .filter(obj -> Objects.nonNull(obj) && obj.isPresent())
                .map(RegistryEntry::get)
                .toArray(Block[]::new);
        return blocks("Frames", frameBlocks)
                .or(framedPipes(frameMaterials, frameBlocks));
    }

    public static BasePredicate framedPipes(Material[] frameMaterials, Block[] frameBlocks) {
        return customPredicate("FramedPipes", ctx -> {
            BlockEntity tileEntity = ctx.blockEntity();
            if (!(tileEntity instanceof IPipeNode<?, ?> pipeNode)) {
                return ctx.error(PLACEHOLDER);
            }
            return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial()) ||
                    ctx.error(PLACEHOLDER);
        }, () -> Arrays.stream(frameBlocks).map(BlockInfo::fromBlock));
    }
}
