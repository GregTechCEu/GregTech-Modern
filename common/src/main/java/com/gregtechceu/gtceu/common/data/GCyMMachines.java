package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GCyMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.registerTieredMachines;
import static com.gregtechceu.gtceu.common.data.GTMaterials.NaquadahAlloy;

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
                    .overlayTieredHullRenderer("parallel_hatch_mk" + (tier - 4))
                    .compassNode("parallel_hatch")
                    .register(),
            IV, LuV, ZPM, UV);

    public final static MultiblockMachineDefinition LARGE_MACERATION_TOWER = REGISTRATE.multiblock("large_maceration_tower", WorkableElectricMultiblockMachine::new)
            .langValue("Large Maceration Tower")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_SECURE_MACERATION)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX","XXXXX", "XXXXX")
                    .aisle("XXXXX", "XGGGX","XGGGX", "X###X")
                    .aisle("XXXXX", "XGGGX","XGGGX", "X###X")
                    .aisle("XXXXX", "XGGGX","XGGGX", "X###X")
                    .aisle("XXXXX", "XXXXX","XXSXX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_SECURE_MACERATION.get()).setMinGlobalLimited(55)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', Predicates.blocks(CRUSHING_WHEELS.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/secure_maceration_casing"),
                    GTCEu.id("block/multiblock/gcym/large_maceration_tower"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CHEMICAL_BATH = REGISTRATE.multiblock("large_chemical_bath", WorkableElectricMultiblockMachine::new)
            .langValue("Large Chemical Bath")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.CHEMICAL_BATH_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_WATERTIGHT)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, FRONT, UP)
                    .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXSXX", "XCTCX", "XAAAX", "XAAAX", "XAAAX", "XCCCX", "XXXXX")
                    .aisle("XXXXX", "XAAAX", "XAAAX", "XAAAX", "XAAAX", "XAAAX", "XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_WATERTIGHT.get()).setMinGlobalLimited(55)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('A', Predicates.air())
                    .where('C', Predicates.blocks(CASING_TITANIUM_PIPE.get()))
                    .where('T', Predicates.blocks(CASING_TITANIUM_PIPE.get()))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_ore_washing_plant"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CENTRIFUGE = REGISTRATE.multiblock("large_centrifuge", WorkableElectricMultiblockMachine::new)
            .langValue("Large Centrifugal Unit")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.CENTRIFUGE_RECIPES, GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_VIBRATION_SAFE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#","XXXXX","#XXX#")
                    .aisle("XXXXX","X#P#X","XXXXX")
                    .aisle("XXXXX","XP#PX","XXXXX")
                    .aisle("XXXXX","X#P#X","XXXXX")
                    .aisle("#XXX#","XXSXX","#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('P', Predicates.blocks(CASING_STEEL_PIPE.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_centrifuge"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_MIXER = REGISTRATE.multiblock("large_mixer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Mixing Vessel")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.MIXER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_VIBRATION_SAFE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#","#XXX#","#XXX#","#XXX#","#XXX#","##F##")
                    .aisle("XXXXX","X#P#X","X###X","X#P#X","X###X","##F##")
                    .aisle("XXXXX","XPPPX","X#P#X","XPPPX","X#G#X","FFGFF")
                    .aisle("XXXXX","X#P#X","X###X","X#P#X","X###X","##F##")
                    .aisle("#XXX#","#XSX#","#XXX#","#XXX#","#XXX#","##F##")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(14)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.HastelloyX)))
                    .where('G', blocks(CASING_STAINLESS_STEEL_GEARBOX.get()))
                    .where('P', blocks(CASING_TITANIUM_PIPE.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_mixer"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ELECTROLYZER = REGISTRATE.multiblock("large_electrolyzer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Electrolysis Chamber")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ELECTROLYZER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_NONCONDUCTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX","XXXXX","XXXXX")
                    .aisle("XXXXX","XCCCX","XCCCX")
                    .aisle("XXXXX","XCCCX","XCCCX")
                    .aisle("XXXXX","XXSXX","XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_NONCONDUCTING.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', blocks(ELECTROLYTIC_CELL.get()))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/nonconducting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_electrolyzer"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_PACKER = REGISTRATE.multiblock("large_packer", WorkableElectricMultiblockMachine::new)
            .langValue("Large Packaging Machine")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.PACKER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX","XXX","XXX")
                    .aisle("XXX","X#X","XXX")
                    .aisle("XXX","X#X","XXX")
                    .aisle("XXX","X#X","XXX")
                    .aisle("XXX","X#X","XXX")
                    .aisle("XXX","XSX","XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_TUNGSTENSTEEL_ROBUST.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/gcym/large_packer"), false)
            .compassSections(GTCompassSections.TIER[HV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ASSEMBLER = REGISTRATE.multiblock("large_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Large Assembling Factory")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_LARGE_SCALE_ASSEMBLING)
            .pattern(definition -> FactoryBlockPattern.start(FRONT, UP, RIGHT)
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "CAX", "CCX").setRepeatable(3)
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XAX", "#XX")
                    .aisle("XXX", "S#X", "#XX")
                    .aisle("XXX", "X#X", "#XX")
                    .aisle("XXX", "XXX", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_LARGE_SCALE_ASSEMBLING.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('A', Predicates.blocks(CASING_TEMPERED_GLASS.get()))
                    .where('C', Predicates.blocks(CASING_ASSEMBLY_LINE.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/gcym/large_assembler"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_CIRCUIT_ASSEMBLER = REGISTRATE.multiblock("large_circuit_assembler", WorkableElectricMultiblockMachine::new)
            .langValue("Large Circuit Assembling Facility")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_LARGE_SCALE_ASSEMBLING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXXXX","XXXXXXXXX","XXXXXXXXX")
                    .aisle("XXXXXXXXX","XGGGCCCCX","XXXXXXXXX")
                    .aisle("XXXXXXXXX","XGGGXXSXX","XXXXX###X")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_LARGE_SCALE_ASSEMBLING.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', Predicates.blocks(CASING_TEMPERED_GLASS.get()))
                    .where('C', Predicates.blocks(CASING_ASSEMBLY_LINE.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/gcym/large_circuit_assembler"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ARC_SMELTER = REGISTRATE.multiblock("large_arc_smelter", WorkableElectricMultiblockMachine::new)
            .langValue("Large Arc Smelter")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ARC_FURNACE_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#","#XXX#","#XXX#","#XXX#")
                    .aisle("XXXXX","XC#CX","XC#CX","XXXXX")
                    .aisle("XXXXX","X###X","X###X","XXXXX")
                    .aisle("XXXXX","X#C#X","X#C#X","XXXXX")
                    .aisle("#XXX#","#XSX#","#XXX#","#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', Predicates.blocks(MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/large_arc_smelter"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_ENGRAVING_LASER = REGISTRATE.multiblock("large_engraving_laser", WorkableElectricMultiblockMachine::new)
            .langValue("Large Engraving Laser")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.LASER_ENGRAVER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_LASER_SAFE_ENGRAVING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXX","XXGXX","XXGXX","XXXXX")
                    .aisle("XXXXX","X###X","X###X","XKKKX")
                    .aisle("XXXXX","G###G","G#C#G","XKXKX")
                    .aisle("XXXXX","X###X","X###X","XKKKX")
                    .aisle("XXSXX","XXGXX","XXGXX","XXXXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('C', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('X', blocks(CASING_LASER_SAFE_ENGRAVING.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('K', blocks(CASING_GRATE.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"),
                    GTCEu.id("block/multiblock/gcym/large_engraving_laser"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition LARGE_SIFTING_FUNNEL = REGISTRATE.multiblock("large_sifting_funnel", WorkableElectricMultiblockMachine::new)
            .langValue("Large Sifting Funnel")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.SIFTER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_VIBRATION_SAFE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#X#X#","#X#X#","#XXX#","XXXXX","#XXX#")
                    .aisle("XXXXX","X#X#X","XKKKX","XKKKX","X###X")
                    .aisle("#XXX#","#X#X#","XKKKX","XKKKX","X###X")
                    .aisle("XXXXX","X#X#X","XKKKX","XKKKX","X###X")
                    .aisle("#X#X#","#X#X#","#XSX#","XXXXX","#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_VIBRATION_SAFE.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('K', blocks(CASING_GRATE.get()))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/vibration_safe_casing"),
                    GTCEu.id("block/multiblock/gcym/large_sifting_funnel"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition BLAST_ALLOY_SMELTER = REGISTRATE.multiblock("alloy_blast_smelter", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Alloy Blast Smelter")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GCyMRecipeTypes.ALLOY_BLAST_RECIPES)
            .recipeModifier(GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                    .aisle("XXXXX", "C###C", "G###G", "C###C", "XXXXX")
                    .aisle("XXXXX", "C###C", "G###G", "C###C", "XXMXX")
                    .aisle("XXXXX", "C###C", "G###G", "C###C", "XXXXX")
                    .aisle("#XSX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(14)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('C', heatingCoils())
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('G', blocks(HEAT_VENT.get()))
                    .where('#', air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"), false)
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition MEGA_BLAST_FURNACE = REGISTRATE.multiblock("mega_blast_furnace", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Rotary Hearth Furnace")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifier(GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("##XXXXXXXXX##", "##XXXXXXXXX##", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                    .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###", "#############", "#############", "#############", "#############", "#############", "####FFFFF####", "#############", "#############", "#############", "#############", "#############", "#############")
                    .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "#############", "#############", "#############", "#############", "#############", "###TTTTTTT###")
                    .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#", "######P######", "######P######", "######P######", "######P######", "######P######", "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######", "######P######", "##TTTTPTTTT##")
                    .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "##TTTTPTTTT##")
                    .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####B###B####", "####I###I####", "#FHHH###HHHF#", "####I###I####", "####C###C####", "####C###C####", "####C###C####", "####I###I####", "#FHHH###HHHF#", "####I###I####", "####C###C####", "####C###C####", "####C###C####", "####I###I####", "##TTTTPTTTT##")
                    .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "###PP###PP###", "###PT###TP###", "#FHPH###HPHF#", "###PT###TP###", "###PC###CP###", "###PC###CP###", "###PC###CP###", "###PT###TP###", "#FHPH###HPHF#", "###PT###TP###", "###PC###CP###", "###PC###CP###", "###PC###CP###", "###PT###TP###", "##TPPPMPPPT##")
                    .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####B###B####", "####I###I####", "#FHHH###HHHF#", "####I###I####", "####C###C####", "####C###C####", "####C###C####", "####I###I####", "#FHHH###HHHF#", "####I###I####", "####C###C####", "####C###C####", "####C###C####", "####I###I####", "##TTTTPTTTT##")
                    .aisle("XXXXXXXXXXXXX", "XXVXXXXXXXVXX", "####BBPBB####", "####TITIT####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "#FFHHHHHHHFF#", "####BITIB####", "####CCCCC####", "####CCCCC####", "####CCCCC####", "####BITIB####", "##TTTTPTTTT##")
                    .aisle("XXXXXXXXXXXXX", "XXXXXXXXXXXXX", "#F####P####F#", "#F####P####F#", "#FFHHHPHHHFF#", "######P######", "######P######", "######P######", "######P######", "######P######", "##FHHHPHHHF##", "######P######", "######P######", "######P######", "######P######", "######P######", "##TTTTPTTTT##")
                    .aisle("XXXXXXXXXXXXX", "XXXXVVVVVXXXX", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##FFFHHHFFF##", "#############", "#############", "#############", "#############", "#############", "###TTTTTTT###")
                    .aisle("#XXXXXXXXXXX#", "#XXXXXXXXXXX#", "###F#####F###", "###F#####F###", "###FFFFFFF###", "#############", "#############", "#############", "#############", "#############", "####FFFFF####", "#############", "#############", "#############", "#############", "#############", "#############")
                    .aisle("##XXXXXXXXX##", "##XXXXSXXXX##", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(14)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', heatingCoils())
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,NaquadahAlloy)))
                    .where('H', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('T', blocks(CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where('B', blocks(FIREBOX_TUNGSTENSTEEL.get()))
                    .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('I', blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('V', blocks(HEAT_VENT.get()))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('#', air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/mega_blast_furnace"), false)
            .compassSections(GTCompassSections.TIER[LuV])
            .compassNodeSelf()
            .register();

    public final static MultiblockMachineDefinition MEGA_VACUUM_FREEZER = REGISTRATE.multiblock("mega_vacuum_freezer", WorkableElectricMultiblockMachine::new)
            .langValue("Bulk Blast Chiller")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH.apply(OverclockingLogic.PERFECT_OVERCLOCK, GTRecipeModifiers.ELECTRIC_OVERCLOCK))
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX#KKK", "XXXXXXX#KVK", "XXXXXXX#KVK", "XXXXXXX#KVK", "XXXXXXX#KKK", "XXXXXXX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XPPPPPPPPPV", "XP#P#PX#VPV", "XPPPPPPPPPV", "XP#P#PX#KVK", "XPPPPPX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XP#P#PX#VPV", "X#####X#VPV", "XP###PX#VPV", "X#####X#KVK", "XP#P#PX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KVK", "XP#P#PPPPPV", "X#####X#VPV", "XP###PPPPPV", "X#####X#KVK", "XP#P#PX####", "XXXXXXX####")
                    .aisle("XXXXXXX#KKK", "XPPPPPX#KVK", "XP###PX#KVK", "XP###PX#KVK", "XP###PX#KKK", "XPPPPPX####", "XXXXXXX####")
                    .aisle("#XXXXX#####", "#XXSXX#####", "#XGGGX#####", "#XGGGX#####", "#XGGGX#####", "#XXXXX#####", "###########")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(140)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('K', blocks(CASING_STAINLESS_CLEAN.get()))
                    .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('V', blocks(HEAT_VENT.get()))
                    .where('#', air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/gcym/mega_vacuum_freezer"), false)
            .compassSections(GTCompassSections.TIER[LuV])
            .compassNodeSelf()
            .register();
}
