package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.DistillationTowerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym.*;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.NaquadahAlloy;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMachines;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GCYMMachines {

    public static void init() {}

    public static final MachineDefinition[] PARALLEL_HATCH = registerTieredMachines("parallel_hatch",
            ParallelHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(switch (tier) {
                        case 5 -> "Elite";
                        case 6 -> "Master";
                        case 7 -> "Ultimate";
                        case 8 -> "Super";
                        default -> "Simple"; // Should never be hit.
                    } + " Parallel Control Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.PARALLEL_HATCH)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableTieredHullMachineModel(
                            GTCEu.id("block/machines/parallel_hatch_mk" + (tier - 4)))
                            .andThen((ctx, prov, model) -> {
                                model.addReplaceableTextures("bottom", "top", "side");
                            }))
                    .tooltips(Component.translatable("gtceu.machine.parallel_hatch_mk" + tier + ".tooltip"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register(),
            IV, LuV, ZPM, UV);

    public final static MultiblockMachineDefinition LARGE_MACERATION_TOWER = REGISTRATE
            .multiblock("large_maceration_tower", LargeMacerationTowerMachine::new)
            .langValue("Large Maceration Tower")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.macerator")))
            .rotationState(RotationState.ALL)
            .recipeType(MACERATOR_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_SECURE_MACERATION)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XGGGX", "XGGGX", "XAAAX")
                    .aisle("XXXXX", "XGGGX", "XGGGX", "XAAAX")
                    .aisle("XXXXX", "XGGGX", "XGGGX", "XAAAX")
                    .aisle("XXXXX", "XXXXX", "XXSXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_SECURE_MACERATION.get()).setMinGlobalLimited(55)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', Predicates.blocks(CRUSHING_WHEELS.get()))
                    .where('A', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/secure_maceration_casing"),
                    GTCEu.id("block/multiblock/gcym/large_maceration_tower"))
            .register();

    public final static MultiblockMachineDefinition LARGE_CHEMICAL_BATH = REGISTRATE
            .multiblock("large_chemical_bath", LargeChemicalBathMachine::new)
            .langValue("Large Chemical Bath")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.ore_washer"), Component.translatable("gtceu.chemical_bath")))
            .rotationState(RotationState.ALL)
            .recipeTypes(CHEMICAL_BATH_RECIPES, ORE_WASHER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XTTTX", "X   X")
                    .aisle("XXXXX", "X   X", "X   X")
                    .aisle("XXXXX", "X   X", "X   X")
                    .aisle("XXXXX", "X   X", "X   X")
                    .aisle("XXXXX", "XTTTX", "X   X")
                    .aisle("XXXXX", "XXSXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(55)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where(' ', Predicates.air())
                    .where('T', Predicates.blocks(CASING_TITANIUM_PIPE.get()))
                    .build())
            .hasBER(true)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_chemical_bath"))
                    .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::makeRecipeFluidAreaRender)))
            .register();

    public final static MultiblockMachineDefinition LARGE_CENTRIFUGE = REGISTRATE
            .multiblock("large_centrifuge", WorkableElectricMultiblockMachine::new)
            .langValue("Large Centrifugal Unit")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.centrifuge"), Component.translatable("gtceu.thermal_centrifuge")))
            .rotationState(RotationState.ALL)
            .recipeTypes(CENTRIFUGE_RECIPES, THERMAL_CENTRIFUGE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_VIBRATION_SAFE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "XXXXX", "#XXX#")
                    .aisle("XXXXX", "XAPAX", "XXXXX")
                    .aisle("XXXXX", "XPAPX", "XXXXX")
                    .aisle("XXXXX", "XAPAX", "XXXXX")
                    .aisle("#XXX#", "XXSXX", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(40)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('P', Predicates.blocks(CASING_STEEL_PIPE.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_centrifuge"))
            .register();

    public final static MultiblockMachineDefinition LARGE_MIXER = REGISTRATE
            .multiblock("large_mixer", LargeMixerMachine::new)
            .langValue("Large Mixing Vessel")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.mixer")))
            .rotationState(RotationState.ALL)
            .recipeType(MIXER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_REACTION_SAFE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#", "#XXX#", "##F##")
                    .aisle("XXXXX", "XAPAX", "XAAAX", "XAPAX", "XAAAX", "##F##")
                    .aisle("XXXXX", "XPPPX", "XAPAX", "XPPPX", "XAGAX", "FFGFF")
                    .aisle("XXXXX", "XAPAX", "XAAAX", "XAPAX", "XAAAX", "##F##")
                    .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#", "#XXX#", "##F##")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_REACTION_SAFE.get()).setMinGlobalLimited(50)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.HastelloyX)))
                    .where('G', blocks(CASING_STAINLESS_STEEL_GEARBOX.get()))
                    .where('P', blocks(CASING_TITANIUM_PIPE.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .hasBER(true)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/gcym/reaction_safe_mixing_casing"),
                    GTCEu.id("block/multiblock/gcym/large_mixer"))
                    .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::makeRecipeFluidAreaRender)))
            .register();

    public final static MultiblockMachineDefinition LARGE_ELECTROLYZER = REGISTRATE
            .multiblock("large_electrolyzer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Electrolysis Chamber")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.electrolyzer")))
            .rotationState(RotationState.ALL)
            .recipeType(ELECTROLYZER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_NONCONDUCTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XCCCX", "XCCCX")
                    .aisle("XXXXX", "XCCCX", "XCCCX")
                    .aisle("XXXXX", "XXSXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_NONCONDUCTING.get()).setMinGlobalLimited(30)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', blocks(ELECTROLYTIC_CELL.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/nonconducting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_electrolyzer"))
            .register();

    public final static MultiblockMachineDefinition LARGE_ELECTROMAGNET = REGISTRATE
            .multiblock("large_electromagnet", WorkableElectricMultiblockMachine::new)
            .langValue("Large Electromagnet")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.electromagnetic_separator"),
                    Component.translatable("gtceu.polarizer")))
            .rotationState(RotationState.ALL)
            .recipeTypes(ELECTROMAGNETIC_SEPARATOR_RECIPES, POLARIZER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_NONCONDUCTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX")
                    .aisle("XCXCX", "XCXCX", "XCXCX")
                    .aisle("XCXCX", "XCXCX", "XCXCX")
                    .aisle("XXXXX", "XXSXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_NONCONDUCTING.get()).setMinGlobalLimited(35)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', blocks(ELECTROLYTIC_CELL.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/nonconducting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_electrolyzer"))
            .register();

    public final static MultiblockMachineDefinition LARGE_PACKER = REGISTRATE
            .multiblock("large_packer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Packaging Machine")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.packer")))
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PACKER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XAX", "XXX")
                    .aisle("XXX", "XAX", "XXX")
                    .aisle("XXX", "XAX", "XXX")
                    .aisle("XXX", "XAX", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_TUNGSTENSTEEL_ROBUST.get()).setMinGlobalLimited(30)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('A', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/gcym/large_packer"))
            .register();

    public final static MultiblockMachineDefinition LARGE_ASSEMBLER = REGISTRATE
            .multiblock("large_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Large Assembling Factory")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.multiblock.exact_hatch_1.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.assembler")))
            .conditionalTooltip(GTMachineUtils.defaultEnvironmentRequirement(),
                    ConfigHolder.INSTANCE.gameplay.environmentalHazards)
            .rotationState(RotationState.ALL)
            .recipeType(ASSEMBLER_RECIPES)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK,
                    BATCH_MODE)
            .appearanceBlock(CASING_LARGE_SCALE_ASSEMBLING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX")
                    .aisle("XXXXXXXXX", "XAAAXAAAX", "XGGGXXXXX")
                    .aisle("XXXXXXXXX", "XGGGXXSXX", "XGGGX###X")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_LARGE_SCALE_ASSEMBLING.get()).setMinGlobalLimited(40)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes(), false, false, true, true, true,
                                    true))
                            .or(Predicates.abilities(INPUT_ENERGY).setExactLimit(1))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', Predicates.blocks(CASING_TEMPERED_GLASS.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/gcym/large_assembler"))
            .register();

    public final static MultiblockMachineDefinition LARGE_CIRCUIT_ASSEMBLER = REGISTRATE
            .multiblock("large_circuit_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Large Circuit Assembling Facility")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.circuit_assembler")))
            .tooltips(Component.translatable("gtceu.multiblock.exact_hatch_1.tooltip"))
            .conditionalTooltip(GTMachineUtils.defaultEnvironmentRequirement(),
                    ConfigHolder.INSTANCE.gameplay.environmentalHazards)
            .rotationState(RotationState.ALL)
            .recipeType(CIRCUIT_ASSEMBLER_RECIPES)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK,
                    BATCH_MODE)
            .appearanceBlock(CASING_LARGE_SCALE_ASSEMBLING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX")
                    .aisle("XXXXXXX", "XPPPPPX", "XGGGGGX")
                    .aisle("XXXXXXX", "XAAAAPX", "XGGGGGX")
                    .aisle("XXXXXXX", "XTTTTXX", "XXXXXXX")
                    .aisle("#####XX", "#####SX", "#####XX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_LARGE_SCALE_ASSEMBLING.get()).setMinGlobalLimited(55)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes(), false, false, true, true, true,
                                    true))
                            .or(Predicates.abilities(INPUT_ENERGY).setExactLimit(1))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('T', Predicates.blocks(CASING_TEMPERED_GLASS.get()))
                    .where('G', Predicates.blocks(CASING_GRATE.get()))
                    .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/gcym/large_circuit_assembler"))
            .register();

    public final static MultiblockMachineDefinition LARGE_ARC_SMELTER = REGISTRATE
            .multiblock("large_arc_smelter", WorkableElectricMultiblockMachine::new)
            .langValue("Large Arc Smelter")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.arc_furnace")))
            .rotationState(RotationState.ALL)
            .recipeType(ARC_FURNACE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#")
                    .aisle("XXXXX", "XCACX", "XCACX", "XXXXX")
                    .aisle("XXXXX", "XAAAX", "XAAAX", "XXMXX")
                    .aisle("XXXXX", "XACAX", "XACAX", "XXXXX")
                    .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(45)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', Predicates.blocks(MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .where('M', Predicates.abilities(MUFFLER))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_arc_smelter"))
            .register();

    public final static MultiblockMachineDefinition LARGE_ENGRAVING_LASER = REGISTRATE
            .multiblock("large_engraving_laser", WorkableElectricMultiblockMachine::new)
            .langValue("Large Engraving Laser")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.laser_engraver")))
            .conditionalTooltip(GTMachineUtils.defaultEnvironmentRequirement(),
                    ConfigHolder.INSTANCE.gameplay.environmentalHazards)
            .rotationState(RotationState.ALL)
            .recipeType(LASER_ENGRAVER_RECIPES)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK,
                    BATCH_MODE)
            .appearanceBlock(CASING_LASER_SAFE_ENGRAVING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXGXX", "XXGXX", "XXXXX")
                    .aisle("XXXXX", "XAAAX", "XAAAX", "XKKKX")
                    .aisle("XXXXX", "GAAAG", "GACAG", "XKXKX")
                    .aisle("XXXXX", "XAAAX", "XAAAX", "XKKKX")
                    .aisle("XXSXX", "XXGXX", "XXGXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('C', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('X', blocks(CASING_LASER_SAFE_ENGRAVING.get()).setMinGlobalLimited(50)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('K', blocks(CASING_GRATE.get()))
                    .where('A', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"),
                    GTCEu.id("block/multiblock/gcym/large_engraving_laser"))
            .register();

    public final static MultiblockMachineDefinition LARGE_SIFTING_FUNNEL = REGISTRATE
            .multiblock("large_sifting_funnel", WorkableElectricMultiblockMachine::new)
            .langValue("Large Sifting Funnel")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.sifter")))
            .rotationState(RotationState.ALL)
            .recipeType(SIFTER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_VIBRATION_SAFE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#X#X#", "#X#X#", "#XXX#", "XXXXX", "#XXX#")
                    .aisle("XXXXX", "XAXAX", "XKKKX", "XKKKX", "X###X")
                    .aisle("#XXX#", "#XAX#", "XKKKX", "XKKKX", "X###X")
                    .aisle("XXXXX", "XAXAX", "XKKKX", "XKKKX", "X###X")
                    .aisle("#X#X#", "#X#X#", "#XSX#", "XXXXX", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(50)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('K', blocks(CASING_GRATE.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_sifting_funnel"))
            .register();

    public final static MultiblockMachineDefinition BLAST_ALLOY_SMELTER = REGISTRATE
            .multiblock("alloy_blast_smelter", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Alloy Blast Smelter")
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.alloy_blast_smelter")))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"))
            .rotationState(RotationState.ALL)
            .recipeType(ALLOY_BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                    .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                    .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXMXX")
                    .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                    .aisle("#XSX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(30)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('C', heatingCoils())
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('G', blocks(HEAT_VENT.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("#XSX#", "#CCC#", "#GGG#", "#CCC#", "#XMX#")
                        .aisle("IXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                        .aisle("XXXXD", "CAAAC", "GAAAG", "CAAAC", "XXHXX")
                        .aisle("FXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                        .aisle("#EXE#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                        .where('X', CASING_HIGH_TEMPERATURE_SMELTING.getDefaultState())
                        .where('S', definition, Direction.NORTH)
                        .where('G', HEAT_VENT.getDefaultState())
                        .where('A', Blocks.AIR.defaultBlockState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.WEST)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.WEST)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.EAST)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.UP)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH);
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    public final static MultiblockMachineDefinition LARGE_AUTOCLAVE = REGISTRATE
            .multiblock("large_autoclave", WorkableElectricMultiblockMachine::new)
            .langValue("Large Crystallization Chamber")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.autoclave")))
            .rotationState(RotationState.ALL)
            .recipeType(AUTOCLAVE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XTX", "XXX")
                    .aisle("XXX", "XTX", "XXX")
                    .aisle("XXX", "XTX", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(30)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('T', blocks(CASING_STEEL_PIPE.get()))
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_autoclave"))
            .register();

    public final static MultiblockMachineDefinition LARGE_MATERIAL_PRESS = REGISTRATE
            .multiblock("large_material_press", WorkableElectricMultiblockMachine::new)
            .langValue("Large Material Press")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_4.tooltip",
                    Component.translatable("gtceu.bender"), Component.translatable("gtceu.compressor"),
                    Component.translatable("gtceu.forge_hammer"), Component.translatable("gtceu.forming_press")))
            .rotationState(RotationState.ALL)
            .recipeTypes(BENDER_RECIPES, COMPRESSOR_RECIPES, FORGE_HAMMER_RECIPES, FORMING_PRESS_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_STRESS_PROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX")
                    .aisle("XXXXXXX", "XAXGGGX", "XXXXXXX")
                    .aisle("XXXXXXX", "XSXCCCX", "XXXXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STRESS_PROOF.get()).setMinGlobalLimited(40)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('G', blocks(CASING_STEEL_GEARBOX.get()))
                    .where('C', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('A', air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_material_press"))
            .register();

    public final static MultiblockMachineDefinition LARGE_BREWER = REGISTRATE
            .multiblock("large_brewer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Brewing Vat")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_3.tooltip",
                    Component.translatable("gtceu.brewery"), Component.translatable("gtceu.fermenter"),
                    Component.translatable("gtceu.fluid_heater")))
            .rotationState(RotationState.ALL)
            .recipeTypes(BREWING_RECIPES, FERMENTING_RECIPES, FLUID_HEATER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_CORROSION_PROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#", "#####")
                    .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                    .aisle("XXXXX", "XCPCX", "XAPAX", "XAPAX", "#XXX#")
                    .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                    .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#", "#####")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_CORROSION_PROOF.get()).setMinGlobalLimited(50)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('P', blocks(CASING_STEEL_PIPE.get()))
                    .where('C', blocks(MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/corrosion_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_brewer"))
            .register();

    public final static MultiblockMachineDefinition LARGE_CUTTER = REGISTRATE
            .multiblock("large_cutter", WorkableElectricMultiblockMachine::new)
            .langValue("Large Cutting Saw")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.cutter"), Component.translatable("gtceu.lathe")))
            .rotationState(RotationState.ALL)
            .recipeTypes(CUTTER_RECIPES, LATHE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_SHOCK_PROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX", "##XXXXX")
                    .aisle("XXXXXXX", "XAXCCCX", "XXXAAAX", "##XXXXX")
                    .aisle("XXXXXXX", "XAXCCCX", "XXXAAAX", "##XXXXX")
                    .aisle("XXXXXXX", "XSXGGGX", "XXXGGGX", "##XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_SHOCK_PROOF.get()).setMinGlobalLimited(65)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('C', blocks(SLICING_BLADES.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/shock_proof_cutting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_cutter"))
            .register();

    public final static MultiblockMachineDefinition LARGE_DISTILLERY = REGISTRATE
            .multiblock("large_distillery", DistillationTowerMachine::new)
            .langValue("Large Fractionating Distillery")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.distillation_tower"), Component.translatable("gtceu.distillery")))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(DISTILLATION_RECIPES, DISTILLERY_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> {
                TraceabilityPredicate casingPredicate = blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(40);
                TraceabilityPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X);
                if (GTCEu.Mods.isAE2Loaded())
                    exportPredicate = exportPredicate.or(blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
                exportPredicate.setMaxLayerLimited(1);
                return FactoryBlockPattern.start(RIGHT, BACK, UP)
                        .aisle("#YYY#", "YYYYY", "YYYYY", "YYYYY", "#YYY#")
                        .aisle("#YSY#", "YAAAY", "YAAAY", "YAAAY", "#YYY#")
                        .aisle("##X##", "#XAX#", "XAPAX", "#XAX#", "##X##").setRepeatable(1, 12)
                        .aisle("#####", "#ZZZ#", "#ZCZ#", "#ZZZ#", "#####")
                        .where('S', controller(blocks(definition.get())))
                        .where('Y', casingPredicate.or(abilities(IMPORT_ITEMS))
                                .or(abilities(INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                                .or(abilities(IMPORT_FLUIDS).setMinGlobalLimited(1))
                                .or(abilities(EXPORT_ITEMS))
                                .or(autoAbilities(true, false, true)))
                        .where('X', casingPredicate.or(exportPredicate))
                        .where('Z', casingPredicate)
                        .where('P', blocks(CASING_STEEL_PIPE.get()))
                        .where('C', abilities(MUFFLER))
                        .where('A', air())
                        .where('#', any())
                        .build();
            })
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .where('S', definition, Direction.NORTH)
                        .where('C', CASING_WATERTIGHT.getDefaultState())
                        .where('M', MUFFLER_HATCH[IV], Direction.UP)
                        .where('X', PARALLEL_HATCH[IV], Direction.NORTH)
                        .where('H', FLUID_IMPORT_HATCH[IV], Direction.NORTH)
                        .where('B', ITEM_EXPORT_BUS[IV], Direction.NORTH)
                        .where('N', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('P', CASING_STEEL_PIPE.getDefaultState())
                        .where('F', FLUID_EXPORT_HATCH[IV], Direction.SOUTH)
                        .where('E', ENERGY_INPUT_HATCH[IV], Direction.SOUTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                List<String> aisle1 = new ArrayList<>(16);
                aisle1.add("#HCB#");
                aisle1.add("#NSX#");
                aisle1.add("#####");
                List<String> aisle2 = new ArrayList<>(16);
                aisle2.add("CCCCC");
                aisle2.add("C###C");
                aisle2.add("#CCC#");
                List<String> aisle3 = new ArrayList<>(16);
                aisle3.add("CCCCC");
                aisle3.add("C###C");
                aisle3.add("#CMC#");
                List<String> aisle4 = new ArrayList<>(16);
                aisle4.add("CCCCC");
                aisle4.add("C###C");
                aisle4.add("#CCC#");
                List<String> aisle5 = new ArrayList<>(16);
                aisle5.add("#CEC#");
                aisle5.add("#CCC#");
                aisle5.add("#####");
                for (int i = 1; i <= 12; ++i) {
                    aisle1.add(2, "##C##");
                    aisle2.add(2, "#C#C#");
                    aisle3.add(2, "C#P#C");
                    aisle4.add(2, "#C#C#");
                    aisle5.add(2, "##F##");
                    var copy = builder.shallowCopy()
                            .aisle(aisle1.toArray(String[]::new))
                            .aisle(aisle2.toArray(String[]::new))
                            .aisle(aisle3.toArray(String[]::new))
                            .aisle(aisle4.toArray(String[]::new))
                            .aisle(aisle5.toArray(String[]::new));
                    shapeInfos.add(copy.build());
                }
                return shapeInfos;
            })
            .allowExtendedFacing(false)
            .partSorter(Comparator.comparingInt(p -> p.self().getPos().getY()))
            .workableCasingModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_distillery"))
            .register();

    public final static MultiblockMachineDefinition LARGE_EXTRACTOR = REGISTRATE
            .multiblock("large_extractor", WorkableElectricMultiblockMachine::new)
            .langValue("Large Extraction Machine")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.extractor"), Component.translatable("gtceu.canner")))
            .rotationState(RotationState.ALL)
            .recipeTypes(EXTRACTOR_RECIPES, CANNER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XCACX", "XXXXX")
                    .aisle("XXXXX", "XXSXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(25)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('C', blocks(CASING_STEEL_PIPE.get()))
                    .where('A', air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_extractor"))
            .register();

    public final static MultiblockMachineDefinition LARGE_EXTRUDER = REGISTRATE
            .multiblock("large_extruder", WorkableElectricMultiblockMachine::new)
            .langValue("Large Extrusion Machine")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.extruder")))
            .rotationState(RotationState.ALL)
            .recipeType(EXTRUDER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_STRESS_PROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("##XXX", "##XXX", "##XXX")
                    .aisle("##XXX", "##XPX", "##XGX").setRepeatable(2)
                    .aisle("XXXXX", "XXXPX", "XXXGX")
                    .aisle("XXXXX", "XAXPX", "XXXGX")
                    .aisle("XXXXX", "XSXXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STRESS_PROOF.get()).setMinGlobalLimited(40)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('P', blocks(CASING_TITANIUM_PIPE.get()))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_extruder"))
            .register();

    public final static MultiblockMachineDefinition LARGE_SOLIDIFIER = REGISTRATE
            .multiblock("large_solidifier", WorkableElectricMultiblockMachine::new)
            .langValue("Large Solidification Array")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.fluid_solidifier")))
            .rotationState(RotationState.ALL)
            .recipeType(FLUID_SOLIDFICATION_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#")
                    .aisle("XXXXX", "XCACX", "XCACX", "XXXXX")
                    .aisle("XXXXX", "XAAAX", "XAAAX", "XXXXX")
                    .aisle("XXXXX", "XCACX", "XCACX", "XXXXX")
                    .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(45)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('C', blocks(CASING_STEEL_PIPE.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_solidifier"))
            .register();

    public final static MultiblockMachineDefinition LARGE_WIREMILL = REGISTRATE
            .multiblock("large_wiremill", WorkableElectricMultiblockMachine::new)
            .langValue("Large Wire Factory")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.wiremill")))
            .rotationState(RotationState.ALL)
            .recipeType(WIREMILL_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_STRESS_PROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXX##")
                    .aisle("XXXXX", "X#CCX", "XXXXX")
                    .aisle("XXXXX", "XSXXX", "XXX##")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STRESS_PROOF.get()).setMinGlobalLimited(25)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('C', blocks(CASING_TITANIUM_GEARBOX.get()))
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_wiremill"))
            .register();

    // spotless:off
    public final static MultiblockMachineDefinition MEGA_BLAST_FURNACE = REGISTRATE
            .multiblock("mega_blast_furnace", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Rotary Hearth Furnace")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.electric_blast_furnace")))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"))
            .rotationState(RotationState.ALL)
            .recipeType(BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers::ebfOverclock, BATCH_MODE)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> {
                TraceabilityPredicate casing = blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(360);
                return FactoryBlockPattern.start()
                        .aisle("##XXXXXXXXX##", "##XXXXXXXXX##", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###", "#############", "#############", "#############", "#############", "#############", "####FFFFF####", "#############", "#############", "#############", "#############", "#############", "#############")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "#############", "#############", "#############", "#############", "#############", "###TTTTTTT###")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#", "######P######", "######P######", "######P######", "######P######", "######P######", "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######", "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "###PPAAAPP###", "###PTAAATP###", "#FHPHAAAHPHF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###", "#FHPHAAAHPHF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###", "##TPPPMPPPT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#", "######P######", "######P######", "######P######", "######P######", "######P######", "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######", "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "#############", "#############", "#############", "#############", "#############", "###TTTTTTT###")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###", "#############", "#############", "#############", "#############", "#############", "####FFFFF####", "#############", "#############", "#############", "#############", "#############", "#############")
                        .aisle("##XXXXXXXXX##", "##XXXXSXXXX##", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                        .where('S', controller(blocks(definition.get())))
                        .where('X', casing.or(autoAbilities(definition.getRecipeTypes()))
                                .or(Predicates.autoAbilities(true, false, true)))
                        .where('C', heatingCoils())
                        .where('M', abilities(PartAbility.MUFFLER))
                        .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, NaquadahAlloy)))
                        .where('H', casing)
                        .where('T', blocks(CASING_TUNGSTENSTEEL_ROBUST.get()))
                        .where('B', blocks(FIREBOX_TUNGSTENSTEEL.get()))
                        .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                        .where('I', blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                        .where('V', blocks(HEAT_VENT.get()))
                        .where('A', air())
                        .where('#', any())
                        .build();
            })
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("##XODXXXQLX##", "##XXXXSXXXX##", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###", "#############", "#############", "#############", "#############", "#############", "####FFFFF####", "#############", "#############", "#############", "#############", "#############", "#############")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFXXXFFF##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##FFFXXXFFF##", "#############", "#############", "#############", "#############", "#############", "###TTTTTTT###")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFXXXPXXXFF#", "######P######", "######P######", "######P######", "######P######", "######P######", "##FXXXPXXXF##", "######P######", "######P######", "######P######", "######P######", "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFXXXXXXXFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "#FFXXXXXXXFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FXXXAAAXXXF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "#FXXXAAAXXXF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "###PPAAAPP###", "###PTAAATP###", "#FXPXAAAXPXF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###", "#FXPXAAAXPXF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###", "##TPPPHPPPT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FXXXAAAXXXF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "#FXXXAAAXXXF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFXXXXXXXFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "#FFXXXXXXXFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFXXXPXXXFF#", "######P######", "######P######", "######P######", "######P######", "######P######", "##FXXXPXXXF##", "######P######", "######P######", "######P######", "######P######", "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFXXXFFF##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##FFFXXXFFF##", "#############", "#############", "#############", "#############", "#############", "###TTTTTTT###")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###", "#############", "#############", "#############", "#############", "#############", "####FFFFF####", "#############", "#############", "#############", "#############", "#############", "#############")
                        .aisle("##XXXEMEXXX##", "##XXXXXXXXX##", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                        .where('X', CASING_HIGH_TEMPERATURE_SMELTING.getDefaultState())
                        .where('S', definition, Direction.NORTH)
                        .where('A', Blocks.AIR.defaultBlockState())
                        .where('T', CASING_TUNGSTENSTEEL_ROBUST.getDefaultState())
                        .where('B', FIREBOX_TUNGSTENSTEEL.getDefaultState())
                        .where('P', CASING_TUNGSTENSTEEL_PIPE.getDefaultState())
                        .where('I', CASING_EXTREME_ENGINE_INTAKE.getDefaultState())
                        .where('F', ChemicalHelper.getBlock(TagPrefix.frameGt, NaquadahAlloy))
                        .where('V', HEAT_VENT.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('L', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('Q', FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.UP)
                        .where('M', MAINTENANCE_HATCH, Direction.SOUTH);
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/mega_blast_furnace"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component.translatable(
                                    FormattingUtil.formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) + "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    public final static MultiblockMachineDefinition MEGA_VACUUM_FREEZER = REGISTRATE
            .multiblock("mega_vacuum_freezer", WorkableElectricMultiblockMachine::new)
            .langValue("Bulk Blast Chiller")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.vacuum_freezer")))
            .rotationState(RotationState.ALL)
            .recipeType(VACUUM_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX#KKK", "XXXXXXX#KVK", "XXXXXXX#KVK", "XXXXXXX#KVK", "XXXXXXX#KKK", "XXXXXXX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPPPPPPPPPV", "XPAPAPX#VPV", "XPPPPPPPPPV", "XPAPAPX#KVK", "XPPPPPX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPAPAPXAVPV", "XAAAAAX#VPV", "XPAAAPX#VPV", "XAAAAAX#KVK", "XPAPAPX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPAPAPPPPPV", "XAAAAAX#VPV", "XPAAAPPPPPV", "XAAAAAX#KVK", "XPAPAPX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KKK", "XPPPPPX#KVK", "XPA#APX#KVK", "XPAAAPX#KVK", "XPAAAPX#KKK", "XPPPPPX####", "XXXXXXX####")
                    .aisle("#XXXXX#####", "#XXSXX#####", "#XGGGX#####", "#XGGGX#####", "#XGGGX#####", "#XXXXX#####", "###########")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(140)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('K', blocks(CASING_STAINLESS_CLEAN.get()))
                    .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('V', blocks(HEAT_VENT.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/gcym/mega_vacuum_freezer"))
            .register();
    // spotless:on
}
