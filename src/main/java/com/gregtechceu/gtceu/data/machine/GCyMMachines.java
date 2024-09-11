package com.gregtechceu.gtceu.data.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.multiblock.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.data.compass.GTCompassSections;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.GTRecipeModifiers;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.data.block.GCyMBlocks.*;
import static com.gregtechceu.gtceu.data.block.GTBlocks.*;
import static com.gregtechceu.gtceu.data.machine.GTMachines.*;
import static com.gregtechceu.gtceu.data.material.GTMaterials.NaquadahAlloy;
import static com.gregtechceu.gtceu.data.recipe.GCyMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.*;

/**
 * @author Rundas
 * @implNote Gregicality Multiblocks
 */
public class GCyMMachines {

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
                    .workableTieredHullRenderer(GTCEu.id("block/machines/parallel_hatch_mk" + (tier - 4)))
                    .tooltips(Component.translatable("gtceu.machine.parallel_hatch_mk" + tier + ".tooltip"))
                    .compassNode("parallel_hatch")
                    .register(),
            IV, LuV, ZPM, UV);

    public final static MultiblockMachineDefinition LARGE_MACERATION_TOWER = REGISTRATE
            .multiblock("large_maceration_tower", WorkableElectricMultiblockMachine::new)
            .langValue("Large Maceration Tower")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    MACERATOR_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(MACERATOR_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/secure_maceration_casing"),
                    GTCEu.id("block/multiblock/gcym/large_maceration_tower"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CHEMICAL_BATH = REGISTRATE
            .multiblock("large_chemical_bath", WorkableElectricMultiblockMachine::new)
            .langValue("Large Chemical Bath")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    ORE_WASHER_RECIPES.getName(), CHEMICAL_BATH_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(CHEMICAL_BATH_RECIPES, ORE_WASHER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_chemical_bath"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CENTRIFUGE = REGISTRATE
            .multiblock("large_centrifuge", WorkableElectricMultiblockMachine::new)
            .langValue("Large Centrifugal Unit")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    CENTRIFUGE_RECIPES.getName(), THERMAL_CENTRIFUGE_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(CENTRIFUGE_RECIPES, THERMAL_CENTRIFUGE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_centrifuge"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_MIXER = REGISTRATE
            .multiblock("large_mixer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Mixing Vessel")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    MIXER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(MIXER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/reaction_safe_mixing_casing"),
                    GTCEu.id("block/multiblock/gcym/large_mixer"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ELECTROLYZER = REGISTRATE
            .multiblock("large_electrolyzer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Electrolysis Chamber")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    ELECTROLYZER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(ELECTROLYZER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/nonconducting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_electrolyzer"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ELECTROMAGNET = REGISTRATE
            .multiblock("large_electromagnet", WorkableElectricMultiblockMachine::new)
            .langValue("Large Electromagnet")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    ELECTROMAGNETIC_SEPARATOR_RECIPES.getName(),
                    POLARIZER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(ELECTROMAGNETIC_SEPARATOR_RECIPES, POLARIZER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/nonconducting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_electrolyzer"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_PACKER = REGISTRATE
            .multiblock("large_packer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Packaging Machine")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    PACKER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PACKER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/gcym/large_packer"))
            .compassSections(GTCompassSections.TIER[HV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ASSEMBLER = REGISTRATE
            .multiblock("large_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Large Assembling Factory")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.multiblock.exact_hatch_1.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    ASSEMBLER_RECIPES.getName()))
            .tooltips(GTMachines.defaultEnvironmentRequirement())
            .rotationState(RotationState.ALL)
            .recipeType(ASSEMBLER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/gcym/large_assembler"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CIRCUIT_ASSEMBLER = REGISTRATE
            .multiblock("large_circuit_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Large Circuit Assembling Facility")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.multiblock.exact_hatch_1.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    CIRCUIT_ASSEMBLER_RECIPES.getName()))
            .tooltips(GTMachines.defaultEnvironmentRequirement())
            .rotationState(RotationState.ALL)
            .recipeType(CIRCUIT_ASSEMBLER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/gcym/large_circuit_assembler"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ARC_SMELTER = REGISTRATE
            .multiblock("large_arc_smelter", WorkableElectricMultiblockMachine::new)
            .langValue("Large Arc Smelter")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    ARC_FURNACE_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(ARC_FURNACE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_arc_smelter"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ENGRAVING_LASER = REGISTRATE
            .multiblock("large_engraving_laser", WorkableElectricMultiblockMachine::new)
            .langValue("Large Engraving Laser")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    LASER_ENGRAVER_RECIPES.getName()))
            .tooltips(GTMachines.defaultEnvironmentRequirement())
            .rotationState(RotationState.ALL)
            .recipeType(LASER_ENGRAVER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"),
                    GTCEu.id("block/multiblock/gcym/large_engraving_laser"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_SIFTING_FUNNEL = REGISTRATE
            .multiblock("large_sifting_funnel", WorkableElectricMultiblockMachine::new)
            .langValue("Large Sifting Funnel")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    SIFTER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(SIFTER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_sifting_funnel"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition BLAST_ALLOY_SMELTER = REGISTRATE
            .multiblock("alloy_blast_smelter", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Alloy Blast Smelter")
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    ALLOY_BLAST_RECIPES.getName()))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_AUTOCLAVE = REGISTRATE
            .multiblock("large_autoclave", WorkableElectricMultiblockMachine::new)
            .langValue("Large Crystallization Chamber")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    AUTOCLAVE_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(AUTOCLAVE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_autoclave"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_MATERIAL_PRESS = REGISTRATE
            .multiblock("large_material_press", WorkableElectricMultiblockMachine::new)
            .langValue("Large Material Press")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_4.tooltip",
                    BENDER_RECIPES.getName(), COMPRESSOR_RECIPES.getName(),
                    FORGE_HAMMER_RECIPES.getName(), FORMING_PRESS_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(BENDER_RECIPES, COMPRESSOR_RECIPES, FORGE_HAMMER_RECIPES, FORMING_PRESS_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_material_press"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_BREWER = REGISTRATE
            .multiblock("large_brewer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Brewing Vat")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_3.tooltip",
                    BREWING_RECIPES.getName(), FERMENTING_RECIPES.getName(),
                    FLUID_HEATER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(BREWING_RECIPES, FERMENTING_RECIPES, FLUID_HEATER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_CORROSION_PROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#", "#####")
                    .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                    .aisle("XXXXX", "XCPCX", "XAPAX", "XAPAX", "#XMX#")
                    .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                    .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#", "#####")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_CORROSION_PROOF.get()).setMinGlobalLimited(50)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('P', blocks(CASING_STEEL_PIPE.get()))
                    .where('C', blocks(MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .where('M', abilities(MUFFLER))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/corrosion_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_brewer"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CUTTER = REGISTRATE
            .multiblock("large_cutter", WorkableElectricMultiblockMachine::new)
            .langValue("Large Cutting Saw")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    CUTTER_RECIPES.getName(), LATHE_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(CUTTER_RECIPES, LATHE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/shock_proof_cutting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_cutter"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_DISTILLERY = REGISTRATE
            .multiblock("large_distillery", WorkableElectricMultiblockMachine::new)
            .langValue("Large Fractionating Distillery")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    DISTILLATION_RECIPES.getName(), DISTILLERY_RECIPES.getName()))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(DISTILLATION_RECIPES, DISTILLERY_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> {
                TraceabilityPredicate casingPredicate = blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(40);

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
                        .where('X', casingPredicate
                                .or(abilities(EXPORT_FLUIDS_1X).setMinLayerLimited(1).setMaxLayerLimited(1)))
                        .where('Z', casingPredicate)
                        .where('P', blocks(CASING_STEEL_PIPE.get()))
                        .where('C', abilities(MUFFLER))
                        .where('A', air())
                        .where('#', any())
                        .build();
            })
            .partSorter(Comparator.comparingInt(a -> a.self().getPos().getY()))
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_distillery"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_EXTRACTOR = REGISTRATE
            .multiblock("large_extractor", WorkableElectricMultiblockMachine::new)
            .langValue("Large Extraction Machine")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    EXTRACTOR_RECIPES.getName(), CANNER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeTypes(EXTRACTOR_RECIPES, CANNER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_extractor"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_EXTRUDER = REGISTRATE
            .multiblock("large_extruder", WorkableElectricMultiblockMachine::new)
            .langValue("Large Extrusion Machine")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    EXTRUDER_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(EXTRUDER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_extruder"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_SOLIDIFIER = REGISTRATE
            .multiblock("large_solidifier", WorkableElectricMultiblockMachine::new)
            .langValue("Large Solidification Array")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    FLUID_SOLIDFICATION_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(FLUID_SOLIDFICATION_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_solidifier"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_WIREMILL = REGISTRATE
            .multiblock("large_wiremill", WorkableElectricMultiblockMachine::new)
            .langValue("Large Wire Factory")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    WIREMILL_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(WIREMILL_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
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
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/gcym/large_wiremill"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition ROTARY_HEARTH_FURNACE = REGISTRATE
            .multiblock("rotary_hearth_furnace", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Rotary Hearth Furnace")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    BLAST_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> {
                TraceabilityPredicate casing = blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(360);
                return FactoryBlockPattern.start()
                        .aisle("##XXXXXXXXX##", "##XXXXXXXXX##", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###",
                                "#############", "#############", "#############", "#############", "#############",
                                "####FFFFF####", "#############", "#############", "#############", "#############",
                                "#############", "#############")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##",
                                "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##",
                                "##FFFHHHFFF##", "#############", "#############", "#############", "#############",
                                "#############", "###TTTTTTT###")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#",
                                "######P######", "######P######", "######P######", "######P######", "######P######",
                                "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######",
                                "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#",
                                "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####",
                                "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####",
                                "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FHHHAAAHHHF#",
                                "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####",
                                "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####",
                                "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "###PPAAAPP###", "###PTAAATP###", "#FHPHAAAHPHF#",
                                "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###",
                                "#FHPHAAAHPHF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###",
                                "###PTAAATP###", "##TPPPMPPPT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FHHHAAAHHHF#",
                                "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####",
                                "#FHHHAAAHHHF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####",
                                "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#",
                                "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####",
                                "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####",
                                "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#",
                                "######P######", "######P######", "######P######", "######P######", "######P######",
                                "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######",
                                "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##",
                                "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##",
                                "##FFFHHHFFF##", "#############", "#############", "#############", "#############",
                                "#############", "###TTTTTTT###")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###",
                                "#############", "#############", "#############", "#############", "#############",
                                "####FFFFF####", "#############", "#############", "#############", "#############",
                                "#############", "#############")
                        .aisle("##XXXXXXXXX##", "##XXXXSXXXX##", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############")
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
                        .aisle("##XODXXXQLX##", "##XXXXSXXXX##", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###",
                                "#############", "#############", "#############", "#############", "#############",
                                "####FFFFF####", "#############", "#############", "#############", "#############",
                                "#############", "#############")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFXXXFFF##",
                                "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##",
                                "##FFFXXXFFF##", "#############", "#############", "#############", "#############",
                                "#############", "###TTTTTTT###")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFXXXPXXXFF#",
                                "######P######", "######P######", "######P######", "######P######", "######P######",
                                "##FXXXPXXXF##", "######P######", "######P######", "######P######", "######P######",
                                "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFXXXXXXXFF#",
                                "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####",
                                "#FFXXXXXXXFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####",
                                "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FXXXAAAXXXF#",
                                "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####",
                                "#FXXXAAAXXXF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####",
                                "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "###PPAAAPP###", "###PTAAATP###", "#FXPXAAAXPXF#",
                                "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###", "###PTAAATP###",
                                "#FXPXAAAXPXF#", "###PTAAATP###", "###PCAAACP###", "###PCAAACP###", "###PCAAACP###",
                                "###PTAAATP###", "##TPPPHPPPT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BAAAB####", "####IAAAI####", "#FXXXAAAXXXF#",
                                "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####", "####IAAAI####",
                                "#FXXXAAAXXXF#", "####IAAAI####", "####CAAAC####", "####CAAAC####", "####CAAAC####",
                                "####IAAAI####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFXXXXXXXFF#",
                                "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####",
                                "#FFXXXXXXXFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####",
                                "####BITIB####", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFXXXPXXXFF#",
                                "######P######", "######P######", "######P######", "######P######", "######P######",
                                "##FXXXPXXXF##", "######P######", "######P######", "######P######", "######P######",
                                "######P######", "##TTTTPTTTT##")
                        .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFXXXFFF##",
                                "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##",
                                "##FFFXXXFFF##", "#############", "#############", "#############", "#############",
                                "#############", "###TTTTTTT###")
                        .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###",
                                "#############", "#############", "#############", "#############", "#############",
                                "####FFFFF####", "#############", "#############", "#############", "#############",
                                "#############", "#############")
                        .aisle("##XXXEMEXXX##", "##XXXXXXXXX##", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############", "#############", "#############", "#############",
                                "#############", "#############")
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
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/mega_blast_furnace"))
            .compassSections(GTCompassSections.TIER[LuV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition MEGA_VACUUM_FREEZER = REGISTRATE
            .multiblock("mega_vacuum_freezer", WorkableElectricMultiblockMachine::new)
            .langValue("Bulk Blast Chiller")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    VACUUM_RECIPES.getName()))
            .rotationState(RotationState.ALL)
            .recipeType(VACUUM_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX#KKK", "XXXXXXX#KVK", "XXXXXXX#KVK", "XXXXXXX#KVK", "XXXXXXX#KKK", "XXXXXXX####",
                            "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPPPPPPPPPV", "XPAPAPX#VPV", "XPPPPPPPPPV", "XPAPAPX#KVK", "XPPPPPX####",
                            "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPAPAPXAVPV", "XAAAAAX#VPV", "XPAAAPX#VPV", "XAAAAAX#KVK", "XPAPAPX####",
                            "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPAPAPPPPPV", "XAAAAAX#VPV", "XPAAAPPPPPV", "XAAAAAX#KVK", "XPAPAPX####",
                            "XXXXXXX####")
                    .aisle("XXXXXXX#KKK", "XPPPPPX#KVK", "XPA#APX#KVK", "XPAAAPX#KVK", "XPAAAPX#KKK", "XPPPPPX####",
                            "XXXXXXX####")
                    .aisle("#XXXXX#####", "#XXSXX#####", "#XGGGX#####", "#XGGGX#####", "#XGGGX#####", "#XXXXX#####",
                            "###########")
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
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/gcym/mega_vacuum_freezer"))
            .compassSections(GTCompassSections.TIER[LuV])
            .compassNodeSelf()
            .register();
}
