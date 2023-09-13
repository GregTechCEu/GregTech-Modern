package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GCyMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GCyMMachines.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_TEMPERED_GLASS;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.CustomTags.*;

public class GCyMRecipes {

    private GCyMRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        registerManualRecipes(provider);
        registerMachineRecipes(provider);
    }

    private static void registerManualRecipes(Consumer<FinishedRecipe> provider) {
        registerPartsRecipes(provider);
        registerMultiblockControllerRecipes(provider);
    }

    private static void registerMultiblockControllerRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_macerator", LARGE_MACERATOR.asStack(), "PCP", "BXB", "MKM", 'C', IV_CIRCUITS, 'P', ChemicalHelper.get(plate, TungstenCarbide), 'B', ELECTRIC_PISTON_IV.asStack(), 'M', ELECTRIC_MOTOR_IV.asStack(), 'X', MACERATOR[IV - 1].asStack(), 'K', new UnificationEntry(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_arc_smelter", LARGE_ARC_FURNACE.asStack(), "KDK", "CXC", "PPP", 'C', IV_CIRCUITS, 'P', ChemicalHelper.get(plate, TantalumCarbide), 'X', ARC_FURNACE[IV - 1].asStack(), 'D',ChemicalHelper.get(dust, Graphite) ,'K', new UnificationEntry(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_ore_washer", LARGE_ORE_WASHER.asStack(), "PGP", "CXC", "MKM", 'C', IV_CIRCUITS, 'G',CASING_TEMPERED_GLASS.asStack() ,'P', ELECTRIC_PUMP_IV.asStack(), 'M', CONVEYOR_MODULE_IV.asStack(), 'X', ORE_WASHER[IV - 1].asStack(), 'K', new UnificationEntry(TagPrefix.cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_sifter", LARGE_SIFTER.asStack(), "PCP", "EXE", "PKP", 'C', IV_CIRCUITS, 'P', ChemicalHelper.get(plate, HSLASteel), 'E', ELECTRIC_PISTON_IV.asStack(), 'X', SIFTER[IV - 1].asStack(), 'K', new UnificationEntry(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_engraver", LARGE_ENGRAVING_LASER.asStack(), "ICI", "EXE", "PKP", 'C', IV_CIRCUITS, 'P', ChemicalHelper.get(plateDouble, TantalumCarbide),'I',EMITTER_IV.asStack() ,'E', ELECTRIC_PISTON_IV.asStack(), 'X', LASER_ENGRAVER[IV - 1].asStack(), 'K', new UnificationEntry(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_packer", LARGE_PACKER.asStack(), "RCR", "PXP", "KPK", 'C', EV_CIRCUITS, 'P', ChemicalHelper.get(plate, HSLASteel),'R',ROBOT_ARM_HV.asStack() ,'K', CONVEYOR_MODULE_HV.asStack(), 'X', PACKER[HV - 1].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_mixer", LARGE_MIXER.asStack(), "FCF", "RXR", "MKM", 'C', IV_CIRCUITS, 'F', ChemicalHelper.get(pipeNormalFluid, Polybenzimidazole),'R',ChemicalHelper.get(rotor, Osmiridium) ,'M', ELECTRIC_MOTOR_IV.asStack(), 'X', MIXER[IV - 1].asStack(), 'K', new UnificationEntry(TagPrefix.cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_centrifuge", LARGE_CENTRIFUGE.asStack(), "SFS", "CXC", "MKM", 'C', IV_CIRCUITS, 'F', ChemicalHelper.get(pipeHugeFluid, StainlessSteel),'S',ChemicalHelper.get(spring, MolybdenumDisilicide) ,'M', ELECTRIC_MOTOR_IV.asStack(), 'X', CENTRIFUGE[IV - 1].asStack(), 'K', new UnificationEntry(TagPrefix.cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_assembler", LARGE_ASSEMBLER.asStack(), "RKR", "CXC", "MKM", 'C', IV_CIRCUITS, 'R', ROBOT_ARM_IV.asStack() ,'M', CONVEYOR_MODULE_IV.asStack(), 'X', ASSEMBLER[IV - 1].asStack(), 'K', new UnificationEntry(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_electrolyzer", LARGE_ELECTROLYZER.asStack(), "PCP", "WXW", "PKP", 'C', IV_CIRCUITS, 'P', ChemicalHelper.get(plate, BlackSteel) ,'W', ChemicalHelper.get(wireGtQuadruple, Osmium), 'X', ELECTROLYZER[GTValues.IV - 1].asStack(), 'K', new UnificationEntry(TagPrefix.cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "blast_alloy_smelter", BLAST_ALLOY_SMELTER.asStack(), "TCT", "WXW", "TCT", 'C', EV_CIRCUITS, 'T', ChemicalHelper.get(plate, TantalumCarbide) ,'W', ChemicalHelper.get(wireGtQuadruple, Osmium), 'X', ALLOY_SMELTER[EV - 1].asStack(), 'K', new UnificationEntry(TagPrefix.cableGtSingle, Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "mega_blast_furnace", MEGA_BLAST_FURNACE.asStack(),"PCP", "FSF", "DWD", 'C', ZPM_CIRCUITS,'S', ELECTRIC_BLAST_FURNACE.asStack(), 'F', FIELD_GENERATOR_ZPM.asStack(), 'P', new UnificationEntry(spring, Naquadah), 'D', new UnificationEntry(plateDense, NaquadahAlloy), 'W', new UnificationEntry(wireGtQuadruple, RutheniumTriniumAmericiumNeutronate));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "mega_vacuum_freezer", MEGA_VACUUM_FREEZER.asStack(),  "PCP", "FSF", "DWD", 'C', ZPM_CIRCUITS, 'S', VACUUM_FREEZER.asStack(), 'F', FIELD_GENERATOR_ZPM.asStack(), 'P', new UnificationEntry(pipeNormalFluid, NiobiumTitanium), 'D', new UnificationEntry(plateDense, RhodiumPlatedPalladium), 'W', new UnificationEntry(wireGtQuadruple, RutheniumTriniumAmericiumNeutronate));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_distillery", LARGE_DISTILLERY.asStack(),  "PCP", "MSM", "PCP", 'C', IV_CIRCUITS, 'S', DISTILLATION_TOWER.asStack(), 'P', new UnificationEntry(pipeLargeFluid, Iridium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_fluidworks", LARGE_FLUIDWORKS.asStack(),  "DAD", "CSC", "PKP", 'C', IV_CIRCUITS, 'A', AUTOCLAVE[IV - 1].asStack(), 'P', ChemicalHelper.get(plateDouble, HSLASteel), 'K', ChemicalHelper.get(cableGtSingle, Platinum), 'S', FLUID_SOLIDIFIER[IV - 1].asStack(), 'P', ELECTRIC_PUMP_IV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_materialworks", LARGE_MATERIALWORKS.asStack(),  "PKP", "BZC", "FKH", 'Z', IV_CIRCUITS, 'W', WIREMILL[IV - 1].asStack(), 'P', ELECTRIC_PISTON_IV.asStack(), 'B', BENDER[IV - 1].asStack(), 'C', COMPRESSOR[IV - 1].asStack(), 'F', FORMING_PRESS[IV - 1].asStack(), 'H', FORGE_HAMMER[IV - 1].asStack(), 'K', ChemicalHelper.get(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_cutter", LARGE_CUTTER.asStack(),  "SCS", "LZK", "MPM", 'Z', IV_CIRCUITS, 'L', LATHE[IV - 1].asStack(), 'P', ChemicalHelper.get(cableGtSingle, Platinum), 'K', CUTTER[IV - 1].asStack(), 'M', ELECTRIC_MOTOR_IV.asStack(), 'S', ChemicalHelper.get(toolHeadBuzzSaw, TungstenCarbide), 'C', CONVEYOR_MODULE_IV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_extractor", LARGE_EXTRACTOR.asStack(),  "PTP", "EZC", "BKB", 'Z', IV_CIRCUITS, 'E', EXTRACTOR[IV - 1].asStack(), 'B', ELECTRIC_PISTON_IV.asStack(), 'P', ELECTRIC_PUMP_IV.asStack(), 'C', CANNER[IV - 1].asStack(), 'K', ChemicalHelper.get(cableGtSingle, Platinum));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_extruder", LARGE_EXTRUDER.asStack(),  "PBP", "WBE", "SKS", 'Z', IV_CIRCUITS, 'E', EXTRUDER[IV - 1].asStack(), 'B', ELECTRIC_PISTON_IV.asStack(), 'P', ChemicalHelper.get(pipeLargeFluid, NiobiumTitanium), 'W', WIREMILL[IV - 1].asStack(), 'S', ChemicalHelper.get(spring, MolybdenumDisilicide));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_brewer", LARGE_BREWER.asStack(),  "SZS", "BFH", "PKP", 'Z', IV_CIRCUITS, 'B', BREWERY[IV - 1].asStack(), 'P', ELECTRIC_PUMP_IV.asStack(), 'H', FLUID_HEATER[IV - 1].asStack(), 'F', FERMENTER[IV - 1].asStack(), 'S', ChemicalHelper.get(spring, MolybdenumDisilicide), 'K', ChemicalHelper.get(cableGtSingle, Platinum));
    }

    private static void registerPartsRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "crushing_wheels", CRUSHING_WHEELS.asStack(2), "TTT", "UCU","UMU", 'T', ChemicalHelper.get(gearSmall,TungstenCarbide), 'U', ChemicalHelper.get(gear, Ultimet), 'C', CASING_SECURE_MACERATION.asStack(), 'M', ELECTRIC_MOTOR_IV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "slicing_blades", SLICING_BLADES.asStack(2), "PPP", "UCU","UMU", 'P', ChemicalHelper.get(plate,TungstenCarbide), 'U', ChemicalHelper.get(gear, Ultimet), 'C', CASING_SHOCK_PROOF.asStack(), 'M', ELECTRIC_MOTOR_IV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "electrolytic_cell", ELECTROLYTIC_CELL.asStack(2), "WWW", "WCW","ZKZ", 'W', ChemicalHelper.get(wireGtDouble,Platinum), 'Z', IV_CIRCUITS, 'C', CASING_NONCONDUCTING.asStack(), 'K', ChemicalHelper.get(cableGtSingle,Tungsten));
        VanillaRecipeHelper.addShapedRecipe(provider, "heat_vent", HEAT_VENT.asStack(2), "PDP", "RLR","PDP", 'P', ChemicalHelper.get(plate,TantalumCarbide), 'D', ChemicalHelper.get(plateDouble,MolybdenumDisilicide), 'R', ChemicalHelper.get(rotor,Titanium), 'L', ChemicalHelper.get(rodLong,MolybdenumDisilicide));
    }

    private static void registerMachineRecipes(Consumer<FinishedRecipe> provider) {
        registerAssemblerRecipes(provider);
        registerMixerRecipes(provider);
        registerBlastAlloyRecipes(provider);
    }

    private static void registerAssemblerRecipes(Consumer<FinishedRecipe> provider){
        ASSEMBLER_RECIPES.recipeBuilder("crushing_wheels")
                .inputItems(ChemicalHelper.get(gearSmall,TungstenCarbide,2))
                .inputItems(ChemicalHelper.get(gear,Ultimet,3))
                .inputItems(CASING_SECURE_MACERATION.asStack())
                .inputItems(GTItems.ELECTRIC_MOTOR_IV.asStack())
                .outputItems(CRUSHING_WHEELS.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("slicing_blades")
                .inputItems(ChemicalHelper.get(plate,TungstenCarbide,2))
                .inputItems(ChemicalHelper.get(gear,Ultimet,3))
                .inputItems(CASING_SHOCK_PROOF.asStack())
                .inputItems(GTItems.ELECTRIC_MOTOR_IV.asStack())
                .outputItems(SLICING_BLADES.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electrolytic_cell")
                .inputItems(ChemicalHelper.get(wireGtDouble,Platinum,4))
                .inputItems(ChemicalHelper.get(cableGtSingle,Tungsten,1))
                .inputItems(CASING_NONCONDUCTING.asStack())
                .inputItems(IV_CIRCUITS)
                .outputItems(ELECTROLYTIC_CELL.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("mds_coil_block")
                .inputItems(ChemicalHelper.get(ring,MolybdenumDisilicide,32))
                .inputItems(ChemicalHelper.get(foil,Graphene,16))
                .inputFluids(HSLASteel.getFluid(144))
                .outputItems(MOLYBDENUM_DISILICIDE_COIL_BLOCK.asStack(1))
                .duration(500).EUt(1920)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("heat_vent")
                .inputItems(ChemicalHelper.get(plate,TantalumCarbide,3))
                .inputItems(ChemicalHelper.get(plateDouble,MolybdenumDisilicide,2))
                .inputItems(ChemicalHelper.get(rotor,Titanium,1))
                .inputItems(ChemicalHelper.get(rodLong,MolybdenumDisilicide,1))
                .outputItems(HEAT_VENT.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_nonconducting")
                .inputItems(plate, HSLASteel, 6).inputItems(frameGt, HSLASteel).circuitMeta(6)
                .outputItems(CASING_NONCONDUCTING.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_vibration_safe")
                .inputItems(plate, IncoloyMA956, 6).inputItems(frameGt, IncoloyMA956).circuitMeta(6)
                .outputItems(CASING_VIBRATION_SAFE.asStack(2))
                .EUt(16).duration(50)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_watertight")
                .inputItems(plate, WatertightSteel, 6).inputItems(frameGt, WatertightSteel).circuitMeta(6)
                .outputItems(CASING_WATERTIGHT.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_secure_maceration")
                .inputItems(plate, Zeron100, 6).inputItems(frameGt, Titanium).circuitMeta(6)
                .outputItems(CASING_SECURE_MACERATION.asStack(2))
                .EUt(16).duration(50)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_high_temperature_smelting")
                .inputItems(plate, TitaniumCarbide, 4).inputItems(plate, HSLASteel, 2).inputItems(frameGt, TungstenCarbide).circuitMeta(6)
                .outputItems(CASING_HIGH_TEMPERATURE_SMELTING.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_laser_safe_engraving")
                .inputItems(plate, TitaniumTungstenCarbide, 6).inputItems(frameGt, Titanium).circuitMeta(6)
                .outputItems(CASING_LASER_SAFE_ENGRAVING.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_large_scale_assembling")
                .inputItems(plate, Stellite100, 6).inputItems(frameGt, Tungsten).circuitMeta(6)
                .outputItems(CASING_LARGE_SCALE_ASSEMBLING.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_shock_proof")
                .inputItems(plate, HastelloyC276, 6).inputItems(frameGt, HastelloyC276).circuitMeta(6)
                .outputItems(CASING_SHOCK_PROOF.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_stress_proof")
                .inputItems(plate, MaragingSteel300, 6).inputItems(frameGt, StainlessSteel).circuitMeta(6)
                .outputItems(CASING_SHOCK_PROOF.asStack(2))
                .duration(50).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_corrosion_proof")
                .inputItems(plate, CobaltBrass, 6).inputItems(frameGt, HSLASteel).circuitMeta(6)
                .outputItems(CASING_CORROSION_PROOF.asStack(2))
                .duration(50).EUt(16)
                .save(provider);
    }

    private static void registerMixerRecipes(Consumer<FinishedRecipe> provider){
        MIXER_RECIPES.recipeBuilder("tantalum_carbide")
                .inputItems(dust, Tantalum)
                .inputItems(dust, Carbon)
                .outputItems(dust, TantalumCarbide, 2)
                .duration(150).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hsla_steel")
                .inputItems(dust, Invar, 2)
                .inputItems(dust, Vanadium)
                .inputItems(dust, Titanium)
                .inputItems(dust, Molybdenum)
                .outputItems(dust, HSLASteel, 5)
                .duration(140).EUt(VA[HV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("incoloy_ma_956")
                .inputItems(dust, VanadiumSteel, 4)
                .inputItems(dust, Manganese, 2)
                .inputItems(dust, Aluminium, 5)
                .inputItems(dust, Yttrium, 2)
                .outputItems(dust, IncoloyMA956, 13)
                .duration(200).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("watertight_steel")
                .inputItems(dust, Iron, 7)
                .inputItems(dust, Aluminium, 4)
                .inputItems(dust, Nickel, 2)
                .inputItems(dust, Chromium)
                .inputItems(dust, Sulfur)
                .outputItems(dust, HSLASteel, 15)
                .duration(220).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("molybdenum_disilicide")
                .inputItems(dust, Molybdenum)
                .inputItems(dust, Silicon, 2)
                .outputItems(dust, MolybdenumDisilicide, 3)
                .duration(180).EUt(VA[EV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hastelloy_x")
                .inputItems(dust, Nickel, 8)
                .inputItems(dust, Iron, 3)
                .inputItems(dust, Tungsten, 4)
                .inputItems(dust, Molybdenum, 2)
                .inputItems(dust, Chromium)
                .inputItems(dust, Niobium)
                .outputItems(dust, HastelloyX, 19)
                .duration(210).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("maraging_steel_300")
                .inputItems(dust, Iron, 16)
                .inputItems(dust, Titanium)
                .inputItems(dust, Aluminium)
                .inputItems(dust, Nickel, 4)
                .inputItems(dust, Cobalt, 2)
                .outputItems(dust, MaragingSteel300, 24)
                .duration(230).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("stellite_100")
                .inputItems(dust, Iron, 4)
                .inputItems(dust, Chromium, 3)
                .inputItems(dust, Tungsten, 2)
                .inputItems(dust, Molybdenum)
                .outputItems(dust, Stellite100, 10)
                .duration(200).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("titanium_carbide")
                .inputItems(dust, Titanium)
                .inputItems(dust, Carbon)
                .outputItems(dust, TitaniumCarbide, 2)
                .duration(160).EUt(VA[EV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("titanium_tungsten_carbide")
                .inputItems(dust, TungstenCarbide)
                .inputItems(dust, TitaniumCarbide, 2)
                .outputItems(dust, TitaniumTungstenCarbide, 3)
                .duration(180).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hastelloy_c_276")
                .inputItems(dust, Nickel, 12)
                .inputItems(dust, Molybdenum, 8)
                .inputItems(dust, Chromium, 7)
                .inputItems(dust, Tungsten, 1)
                .inputItems(dust, Cobalt, 1)
                .inputItems(dust, Copper, 1)
                .outputItems(dust, HastelloyC276, 30)
                .duration(240).EUt(VA[IV])
                .save(provider);
    }

    private static void registerBlastAlloyRecipes(Consumer<FinishedRecipe> provider) {
        ingot.executeHandler(PropertyKey.ALLOY_BLAST, (tagPrefix, material, property) -> generateAlloyBlastRecipes(tagPrefix, material, property, provider));
    }

    /**
     * Generates alloy blast recipes for a material
     *
     * @param material the material to generate for
     * @param property the blast property of the material
     */
    public static void generateAlloyBlastRecipes(@Nullable TagPrefix unused, @Nonnull Material material,
                                                 @Nonnull AlloyBlastProperty property,
                                                 @Nonnull Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.BLAST)) {
            property.getRecipeProducer().produce(material, material.getProperty(PropertyKey.BLAST), provider);
        }
    }
}
