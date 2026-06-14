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
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
                return predicate.computeCandidates();
            }

            @Override
            public String getDebugName() {
                return "Controller{" + predicate + "}";
            }

            @Override
            public boolean isController() {
                return true;
            }
        };
    }

    public static BasePredicate states(@Nullable String debugName, BlockState... allowedStates) {
        List<BlockState> states = new ArrayList<>();
        for (BlockState state : allowedStates) {
            states.add(state);
            if (state.getBlock() instanceof ActiveBlock block) {
                states.add(block.changeActive(state, !block.isActive(state)));
            }
        }
        return new BasePredicate() {

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return states.contains(ctx.state()) || ctx.error(PLACEHOLDER);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return states.stream().map(BlockInfo::fromBlockState).toList();
            }

            @Override
            public String getDebugName() {
                if (debugName == null) {
                    return "States{" + states.size() + "}";
                } else {
                    return "States#" + debugName + "{" + states.size() + "}";

                }
            }
        };
    }

    public static BasePredicate blocks(Block block) {
        return new BasePredicate() {

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return ctx.state().is(block) || ctx.error(new BlockMatchingError(ctx.pos(), List.of(block)));
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return List.of(BlockInfo.fromBlock(block));
            }

            @Override
            public String getDebugName() {
                return block.toString();
            }
        };
    }

    public static BasePredicate blocks(Block... blocks) {
        return blocks(null, blocks);
    }

    public static BasePredicate blocks(@Nullable String debugName, Block... blocks) {
        return new BasePredicate() {

            private final List<Block> blockList = Arrays.asList(blocks);

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return blockList.contains(ctx.state().getBlock()) ||
                        ctx.error(new BlockMatchingError(ctx.pos(), this.blockList));
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return blockList.stream()
                        .map(BlockInfo::fromBlock)
                        .toList();
            }

            @Override
            public String getDebugName() {
                if (debugName == null) {
                    return "Blocks{" + blockList.size() + "}";
                } else {
                    return "Blocks#" + debugName + "{" + blockList.size() + "}";
                }
            }
        };
    }

    public static BasePredicate machines(MachineDefinition... definitions) {
        Validate.noNullElements(definitions, "MachineDefinition array has null element at index %s");
        return blocks(Arrays.stream(definitions).map(MachineDefinition::get).toArray(MetaMachineBlock[]::new));
    }

    public static BasePredicate blockTag(TagKey<Block> tag) {
        return new BasePredicate() {

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return ctx.state().is(tag) || ctx.error(PLACEHOLDER);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                        .getTag(tag)
                        .stream()
                        .map(BlockInfo::fromBlock)
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "BlockTag{" + tag.location() + "}";
            }
        };
    }

    public static BasePredicate fluids(Fluid... fluids) {
        return new BasePredicate() {

            final List<Fluid> fluidList = Arrays.asList(fluids);

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return fluidList.contains(ctx.fluid()) || ctx.error(PLACEHOLDER);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return fluidList.stream()
                        .map(Fluid::defaultFluidState)
                        .map(FluidState::createLegacyBlock)
                        .map(BlockInfo::fromBlockState)
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "Fluids{" + fluidList.size() + "}";
            }
        };
    }

    public static BasePredicate fluidTag(TagKey<Fluid> tag) {
        return new BasePredicate() {
            @Override
            public boolean testInternal(PredicateContext ctx) {
                return ctx.fluidState().is(tag) || ctx.error(PLACEHOLDER);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                        .getTag(tag).stream()
                        .map(BlockInfo::fromFluid)
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "Fluids{" + tag.location() + "}";
            }
        };
    }

    public static BasePredicate customFunction(Function<CurrentBlockInfo, @Nullable PatternError> predicate,
                                               @Nullable List<BlockInfo> candidates) {
        return customPredicate(ctx -> {
            PatternError error = predicate.apply(ctx.blockInfo());
            return error == null || ctx.error(error);
        }, candidates);
    }

    public static BasePredicate customPredicate(Predicate<PredicateContext> predicate,
                                                @Nullable List<BlockInfo> candidates) {
        return new BasePredicate() {
            @Override
            public boolean testInternal(PredicateContext ctx) {
                return predicate.test(ctx);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return Optional.ofNullable(candidates).orElse(Collections.emptyList());
            }

            @Override
            public String getDebugName() {
                return "Custom";
            }
        };
    }

    public static BasePredicate any() {
        return BasePredicate.ANY;
    }

    public static BasePredicate air() {
        return BasePredicate.AIR;
    }

    public static BasePredicate abilities(PartAbility ability) {
        return new BasePredicate() {

            final Collection<Block> blockList = ability.getAllBlocks();

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return blockList.contains(ctx.state().getBlock()) || ctx.error(new PartAbilityError(ctx.pos(), ability));
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return blockList.stream()
                        .map(BlockInfo::fromBlock)
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "Ability{" + ability.getName() + "}";
            }
        };
    }

    public static BasePredicate abilities(PartAbility... abilities) {
        return new BasePredicate() {

            final List<PartAbility> abilityList = List.of(abilities);
            final String debugName = computeDebugName();

            @Override
            public boolean testInternal(PredicateContext ctx) {
                List<PartAbilityError> errors = new ArrayList<>();
                for (PartAbility ability : this.abilityList) {
                    if (ability.getAllBlocks().contains(ctx.state().getBlock())) {
                        return true;
                    } else {
                        errors.add(new PartAbilityError(ctx.pos(), ability));
                    }
                }
                errors.forEach(ctx::error);
                return false;
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return this.abilityList.stream()
                        .flatMap(ability -> ability.getAllBlocks().stream())
                        .map(BlockInfo::fromBlock)
                        .toList();
            }

            private String computeDebugName() {
                StringJoiner sb = new StringJoiner(", ");
                for (PartAbility ability : this.abilityList) {
                    sb.add(ability.getName());
                }
                return sb.toString();
            }

            @Override
            public String getDebugName() {
                return "Abilities{" + debugName + "}";
            }
        };
    }

    public static BasePredicate ability(PartAbility ability, int... tiers) {
        return new BasePredicate() {

            final Collection<Block> blockList = ability.getBlocks(tiers);
            final String debugName = computeDebugName();

            @Override
            public boolean testInternal(PredicateContext ctx) {
                return blockList.contains(ctx.state().getBlock()) || ctx.error(new PartAbilityError(ctx.pos(), ability));
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return blockList.stream()
                        .map(BlockInfo::fromBlock)
                        .toList();
            }

            private String computeDebugName() {
                StringJoiner sb = new StringJoiner("-");
                for (int tier : tiers) {
                    sb.add(GTValues.VN[tier]);
                }
                return ability.getName() + sb;
            }

            @Override
            public String getDebugName() {
                return "Ability{" + debugName + "}";
            }
        };
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
        return new BasePredicate() {{
            addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
            setPriority(0);
        }

            private List<Block> getCoils() {
                return GTCEuAPI.HEATING_COILS.values()
                        .stream()
                        .map(Supplier::get)
                        .map(Block.class::cast)
                        .toList();
            }

            @Override
            public boolean testInternal(PredicateContext ctx) {
                var blockState = ctx.state();
                List<Block> coils = this.getCoils();
                for (var blockCoil : coils) {
                    if (blockState.is(blockCoil)) {
                        return true;
                    }
                }
                return ctx.error(new BlockMatchingError(ctx.pos(), coils));
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return GTCEuAPI.HEATING_COILS.entrySet().stream()
                        // sort to make autogenerated jei previews not pick random coils each game load
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(Map.Entry::getValue)
                        .map(Supplier::get)
                        .map(BlockInfo::fromBlock)
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "HeatingCoils";
            }
        };
    }

    public static BasePredicate cleanroomFilters() {
        return new BasePredicate() {{
            addTooltips(Component.translatable("gtceu.multiblock.pattern.cleanroom"));
        }
            @Override
            public boolean testInternal(PredicateContext ctx) {
                var blockState = ctx.state();
                for (var entry : GTCEuAPI.CLEANROOM_FILTERS.entrySet()) {
                    if (blockState.is(entry.getValue().get())) {
                        return true;
                    }
                }
                return ctx.error(PLACEHOLDER);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return GTCEuAPI.CLEANROOM_FILTERS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getCleanroomType().getTier()))
                        .map(e -> new BlockInfo(e.getValue().get()))
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "CleanroomFilters";
            }
        };
    }

    public static BasePredicate powerSubstationBatteries() {
        return new BasePredicate() {{
            addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
        }
            @Override
            public boolean testInternal(PredicateContext ctx) {
                var state = ctx.state();
                for (var entry : GTCEuAPI.PSS_BATTERIES.entrySet()) {
                    if (state.is(entry.getValue().get())) {
                        return true;
                    }
                }
                return ctx.error(PLACEHOLDER);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> new BlockInfo(e.getValue().get().defaultBlockState(), null))
                        .toList();
            }

            @Override
            public String getDebugName() {
                return "PSSBatteries";
            }
        };

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

    public static @Nullable BasePredicate dataHatchPredicate() {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            // TODO xor predicate matching :)
            return abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                    .setExactLimit(1)
                    .setPriority(1);
        }
        return null;
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static BasePredicate frames(Material... frameMaterials) {
        var frameBlocks = Arrays.stream(frameMaterials)
                .map(m -> GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, m))
                .filter(Objects::nonNull)
                .filter(RegistryEntry::isPresent)
                .map(RegistryEntry::get)
                .toArray(Block[]::new);
        return blocks(frameBlocks)
                .or(new BasePredicate() {
                    @Override
                    public boolean testInternal(PredicateContext ctx) {
                        BlockEntity tileEntity = ctx.blockEntity();
                        if (!(tileEntity instanceof IPipeNode<?, ?> pipeNode)) {
                            return ctx.error(PLACEHOLDER);
                        }
                        return ArrayUtils.contains(frameMaterials, pipeNode.getFrameMaterial()) ||
                                ctx.error(PLACEHOLDER);
                    }

                    @Override
                    public List<BlockInfo> computeCandidates() {
                        return Arrays.stream(frameBlocks).map(BlockInfo::fromBlock).toList();
                    }

                    @Override
                    public String getDebugName() {
                        return "FramedPipes";
                    }
                });
    }
}
