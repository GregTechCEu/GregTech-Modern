package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class CircuitRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        waferRecipes(provider);
        componentRecipes(provider);
        boardRecipes(provider);
        circuitRecipes(provider);
    }

    private static void waferRecipes(Consumer<FinishedRecipe> provider) {
        // Boules
        BLAST_RECIPES.recipeBuilder("silicon_boule")
                .inputItems(DUST, Silicon, 32)
                .inputItems(DUST_SMALL, GalliumArsenide)
                .outputItems(SILICON_BOULE)
                .circuitMeta(2)
                .blastFurnaceTemp(1784)
                .duration(9000).EUt(VA[MV]).save(provider);

        BLAST_RECIPES.recipeBuilder("phosphorus_boule")
                .inputItems(DUST, Silicon, 64)
                .inputItems(DUST, Phosphorus, 8)
                .inputItems(DUST_SMALL, GalliumArsenide, 2)
                .inputFluids(Nitrogen.getFluid(8000))
                .outputItems(PHOSPHORUS_BOULE)
                .blastFurnaceTemp(2484)
                .duration(12000).EUt(VA[HV]).save(provider);

        BLAST_RECIPES.recipeBuilder("naquadah_boule")
                .inputItems(BLOCK, Silicon, 16)
                .inputItems(INGOT, Naquadah)
                .inputItems(DUST, GalliumArsenide)
                .inputFluids(Argon.getFluid(8000))
                .outputItems(NAQUADAH_BOULE)
                .blastFurnaceTemp(5400)
                .duration(15000).EUt(VA[EV]).save(provider);

        BLAST_RECIPES.recipeBuilder("neutronium_boule")
                .inputItems(BLOCK, Silicon, 32)
                .inputItems(INGOT, Neutronium, 4)
                .inputItems(DUST, GalliumArsenide, 2)
                .inputFluids(Xenon.getFluid(8000))
                .outputItems(NEUTRONIUM_BOULE)
                .blastFurnaceTemp(6484)
                .duration(18000).EUt(VA[IV]).save(provider);

        // Boule cutting
        CUTTER_RECIPES.recipeBuilder("cut_silicon_boule")
                .inputItems(SILICON_BOULE)
                .outputItems(SILICON_WAFER, 16)
                .duration(400).EUt(64).save(provider);

        CUTTER_RECIPES.recipeBuilder("cut_phosphorus_boule")
                .inputItems(PHOSPHORUS_BOULE)
                .outputItems(PHOSPHORUS_WAFER, 32)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(800).EUt(VA[HV]).save(provider);

        CUTTER_RECIPES.recipeBuilder("cut_naquadah_boule")
                .inputItems(NAQUADAH_BOULE)
                .outputItems(NAQUADAH_WAFER, 64)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1600).EUt(VA[EV]).save(provider);

        CUTTER_RECIPES.recipeBuilder("cut_neutronium_boule")
                .inputItems(NEUTRONIUM_BOULE)
                .outputItems(NEUTRONIUM_WAFER, 64)
                .outputItems(NEUTRONIUM_WAFER, 32)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(2400).EUt(VA[IV]).save(provider);

        // Wafer engraving
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ilc_silicon").duration(900).EUt(VA[MV]).inputItems(SILICON_WAFER)
                .notConsumable(LENS, Color.Red).outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ilc_phosphorus").duration(500).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Red)
                .outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ilc_naquadah").duration(200).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Red)
                .outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ilc_neutronium").duration(50).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Red)
                .outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ram_silicon").duration(900).EUt(VA[MV]).inputItems(SILICON_WAFER)
                .notConsumable(LENS, Color.Green).outputItems(RANDOM_ACCESS_MEMORY_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ram_phosphorus").duration(500).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Green)
                .outputItems(RANDOM_ACCESS_MEMORY_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ram_naquadah").duration(200).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Green).outputItems(RANDOM_ACCESS_MEMORY_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ram_neutronium").duration(50).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Green)
                .outputItems(RANDOM_ACCESS_MEMORY_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_cpu_silicon").duration(900).EUt(VA[MV]).inputItems(SILICON_WAFER)
                .notConsumable(LENS, Color.LightBlue).outputItems(CENTRAL_PROCESSING_UNIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_cpu_phosphorus").duration(500).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.LightBlue)
                .outputItems(CENTRAL_PROCESSING_UNIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_cpu_naquadah").duration(200).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.LightBlue)
                .outputItems(CENTRAL_PROCESSING_UNIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_cpu_neutronium").duration(50).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.LightBlue)
                .outputItems(CENTRAL_PROCESSING_UNIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ulpic_silicon").duration(900).EUt(VA[MV])
                .inputItems(SILICON_WAFER).notConsumable(LENS, Color.Blue)
                .outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ulpic_phosphorus").duration(500).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Blue)
                .outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM)
                .save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ulpic_naquadah").duration(200).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Blue)
                .outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM)
                .save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ulpic_neutronium").duration(50).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Blue)
                .outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_lpic_silicon").duration(900).EUt(VA[MV]).inputItems(SILICON_WAFER)
                .notConsumable(LENS, Color.Orange).outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_lpic_phosphorus").duration(500).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Orange)
                .outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_lpic_naquadah").duration(200).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Orange)
                .outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_lpic_neutronium").duration(50).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Orange)
                .outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ssoc_silicon").duration(900).EUt(VA[MV]).inputItems(SILICON_WAFER)
                .notConsumable(LENS, Color.Cyan).outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ssoc_phosphorus").duration(500).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Cyan)
                .outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ssoc_naquadah").duration(200).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Cyan).outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_ssoc_neutronium").duration(50).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Cyan)
                .outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_nand_phosphorus").duration(900).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Gray).outputItems(NAND_MEMORY_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_nand_naquadah").duration(500).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Gray).outputItems(NAND_MEMORY_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_nand_neutronium").duration(200).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Gray).outputItems(NAND_MEMORY_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_nor_phosphorus").duration(900).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Pink).outputItems(NOR_MEMORY_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_nor_naquadah").duration(500).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Pink).outputItems(NOR_MEMORY_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_nor_neutronium").duration(200).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Pink).outputItems(NOR_MEMORY_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_pic_phosphorus").duration(900).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Brown)
                .outputItems(POWER_INTEGRATED_CIRCUIT_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_pic_naquadah").duration(500).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Brown)
                .outputItems(POWER_INTEGRATED_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_pic_neutronium").duration(200).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Brown)
                .outputItems(POWER_INTEGRATED_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_soc_phosphorus").duration(900).EUt(VA[HV])
                .inputItems(PHOSPHORUS_WAFER).notConsumable(LENS, Color.Yellow).outputItems(SYSTEM_ON_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_soc_naquadah").duration(500).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Yellow).outputItems(SYSTEM_ON_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_soc_neutronium").duration(200).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Yellow).outputItems(SYSTEM_ON_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_asoc_naquadah").duration(900).EUt(VA[EV])
                .inputItems(NAQUADAH_WAFER).notConsumable(LENS, Color.Purple).outputItems(ADVANCED_SYSTEM_ON_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_asoc_neutronium").duration(500).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Purple)
                .outputItems(ADVANCED_SYSTEM_ON_CHIP_WAFER, 2).cleanroom(CleanroomType.CLEANROOM).save(provider);

        // Can replace this with a Quantum Star/Eye Lens if desired
        LASER_ENGRAVER_RECIPES.recipeBuilder("engrave_hasoc_neutronium").duration(900).EUt(VA[IV])
                .inputItems(NEUTRONIUM_WAFER).notConsumable(LENS, Color.Black).outputItems(HIGHLY_ADVANCED_SOC_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);

        // Wafer chemical refining recipes
        CHEMICAL_RECIPES.recipeBuilder("hpic_wafer")
                .inputItems(POWER_INTEGRATED_CIRCUIT_WAFER)
                .inputItems(DUST, IndiumGalliumPhosphide, 2)
                .inputFluids(VanadiumGallium.getFluid(L * 2))
                .outputItems(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[IV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("uhpic_wafer")
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .inputItems(DUST, IndiumGalliumPhosphide, 8)
                .inputFluids(Naquadah.getFluid(L * 4))
                .outputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[LuV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("nano_cpu_wafer")
                .inputItems(CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItems(CARBON_FIBERS, 16)
                .inputFluids(Glowstone.getFluid(L * 4))
                .outputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[EV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("qbit_cpu_wafer_quantum_eye")
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItems(QUANTUM_EYE, 2)
                .inputFluids(GalliumArsenide.getFluid(L * 2))
                .outputItems(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(900).EUt(VA[EV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("qbit_cpu_wafer_radon")
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItems(DUST, IndiumGalliumPhosphide)
                .inputFluids(Radon.getFluid(50))
                .outputItems(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[EV]).save(provider);

        // Wafer cutting
        CUTTER_RECIPES.recipeBuilder("cut_hasoc").duration(900).EUt(VA[IV]).inputItems(HIGHLY_ADVANCED_SOC_WAFER)
                .outputItems(HIGHLY_ADVANCED_SOC, 6).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_asoc").duration(900).EUt(VA[EV]).inputItems(ADVANCED_SYSTEM_ON_CHIP_WAFER)
                .outputItems(ADVANCED_SYSTEM_ON_CHIP, 6).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_soc").duration(900).EUt(VA[HV]).inputItems(SYSTEM_ON_CHIP_WAFER)
                .outputItems(SYSTEM_ON_CHIP, 6).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_ssoc").duration(900).EUt(64).inputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER)
                .outputItems(SIMPLE_SYSTEM_ON_CHIP, 6).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_ram").duration(900).EUt(96).inputItems(RANDOM_ACCESS_MEMORY_WAFER)
                .outputItems(RANDOM_ACCESS_MEMORY, 32).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_qbit_cpu").duration(900).EUt(VA[EV])
                .inputItems(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER).outputItems(QUBIT_CENTRAL_PROCESSING_UNIT, 4)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_ulpic").duration(900).EUt(VA[MV])
                .inputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 6)
                .save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_lpic").duration(900).EUt(VA[HV])
                .inputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(LOW_POWER_INTEGRATED_CIRCUIT, 4)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_pic").duration(900).EUt(VA[EV]).inputItems(POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItems(POWER_INTEGRATED_CIRCUIT, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_hpic").duration(900).EUt(VA[IV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_uhpic").duration(900).EUt(VA[LuV])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_nor").duration(900).EUt(192).inputItems(NOR_MEMORY_CHIP_WAFER)
                .outputItems(NOR_MEMORY_CHIP, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_nand").duration(900).EUt(192).inputItems(NAND_MEMORY_CHIP_WAFER)
                .outputItems(NAND_MEMORY_CHIP, 32).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_cpu").duration(900).EUt(VA[MV]).inputItems(CENTRAL_PROCESSING_UNIT_WAFER)
                .outputItems(CENTRAL_PROCESSING_UNIT, 8).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_ilc").duration(900).EUt(64).inputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER)
                .outputItems(INTEGRATED_LOGIC_CIRCUIT, 8).save(provider);
        CUTTER_RECIPES.recipeBuilder("cut_nano_cpu").duration(900).EUt(VA[HV])
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER).outputItems(NANO_CENTRAL_PROCESSING_UNIT, 8)
                .cleanroom(CleanroomType.CLEANROOM).save(provider);
    }

    private static void componentRecipes(Consumer<FinishedRecipe> provider) {
        // Vacuum Tube
        VanillaRecipeHelper.addShapedRecipe(provider, "vacuum_tube", VACUUM_TUBE.asStack(),
                "PTP", "WWW",
                'P', new MaterialEntry(BOLT, Steel),
                'T', GLASS_TUBE.asStack(),
                'W', new MaterialEntry(WIRE_GT_SINGLE, Copper));

        ASSEMBLER_RECIPES.recipeBuilder("vacuum_tube_plain")
                .inputItems(GLASS_TUBE)
                .inputItems(BOLT, Steel)
                .inputItems(WIRE_GT_SINGLE, Copper, 2)
                .circuitMeta(1)
                .outputItems(VACUUM_TUBE, 2)
                .duration(120).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("vacuum_tube_red_alloy")
                .inputItems(GLASS_TUBE)
                .inputItems(BOLT, Steel)
                .inputItems(WIRE_GT_SINGLE, Copper, 2)
                .inputFluids(RedAlloy.getFluid(18))
                .outputItems(VACUUM_TUBE, 3)
                .duration(40).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("vacuum_tube_red_alloy_annealed")
                .inputItems(GLASS_TUBE)
                .inputItems(BOLT, Steel)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 2)
                .inputFluids(RedAlloy.getFluid(18))
                .outputItems(VACUUM_TUBE, 4)
                .duration(40).EUt(VA[ULV]).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_glass_tube")
                .inputItems(DUST, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GLASS_TUBE)
                .duration(160).EUt(16).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_glass_tube")
                .inputFluids(Glass.getFluid(L))
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GLASS_TUBE)
                .duration(200).EUt(24).save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder("press_glass_tube")
                .inputItems(DUST, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GLASS_TUBE)
                .duration(80).EUt(VA[ULV]).save(provider);

        // Resistor
        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.asStack(),
                'W', new MaterialEntry(WIRE_GT_SINGLE, Copper),
                'C', new MaterialEntry(DUST, Coal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_fine", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.asStack(),
                'W', new MaterialEntry(WIRE_FINE, Copper),
                'C', new MaterialEntry(DUST, Coal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_charcoal", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.asStack(),
                'W', new MaterialEntry(WIRE_GT_SINGLE, Copper),
                'C', new MaterialEntry(DUST, Charcoal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_fine_charcoal", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.asStack(),
                'W', new MaterialEntry(WIRE_FINE, Copper),
                'C', new MaterialEntry(DUST, Charcoal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_carbon", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.asStack(),
                'W', new MaterialEntry(WIRE_GT_SINGLE, Copper),
                'C', new MaterialEntry(DUST, Carbon));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_fine_carbon", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.asStack(),
                'W', new MaterialEntry(WIRE_FINE, Copper),
                'C', new MaterialEntry(DUST, Carbon));

        ASSEMBLER_RECIPES.recipeBuilder("resistor_coal")
                .inputItems(DUST, Coal)
                .inputItems(WIRE_FINE, Copper, 4)
                .outputItems(RESISTOR, 2)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("resistor_charcoal")
                .inputItems(DUST, Charcoal)
                .inputItems(WIRE_FINE, Copper, 4)
                .outputItems(RESISTOR, 2)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("resistor_carbon")
                .inputItems(DUST, Carbon)
                .inputItems(WIRE_FINE, Copper, 4)
                .outputItems(RESISTOR, 2)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("resistor_coal_annealed")
                .inputItems(DUST, Coal)
                .inputItems(WIRE_FINE, AnnealedCopper, 4)
                .outputItems(RESISTOR, 4)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("resistor_charcoal_annealed")
                .inputItems(DUST, Charcoal)
                .inputItems(WIRE_FINE, AnnealedCopper, 4)
                .outputItems(RESISTOR, 4)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("resistor_carbon_annealed")
                .inputItems(DUST, Carbon)
                .inputItems(WIRE_FINE, AnnealedCopper, 4)
                .outputItems(RESISTOR, 4)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        // Capacitor
        ASSEMBLER_RECIPES.recipeBuilder("capacitor")
                .inputItems(FOIL, Polyethylene)
                .inputItems(FOIL, Aluminium, 2)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(CAPACITOR, 8)
                .duration(320).EUt(VA[MV]).save(provider);

        // Transistor
        ASSEMBLER_RECIPES.recipeBuilder("transistor")
                .inputItems(PLATE, Silicon)
                .inputItems(WIRE_FINE, Tin, 6)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(TRANSISTOR, 8)
                .duration(160).EUt(VA[MV]).save(provider);

        // Diode
        ASSEMBLER_RECIPES.recipeBuilder("diode_glass")
                .inputItems(WIRE_FINE, Copper, 4)
                .inputItems(DUST_SMALL, GalliumArsenide)
                .inputFluids(Glass.getFluid(L))
                .outputItems(DIODE)
                .duration(400).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("diode_glass_annealed")
                .inputItems(WIRE_FINE, AnnealedCopper, 4)
                .inputItems(DUST_SMALL, GalliumArsenide)
                .inputFluids(Glass.getFluid(L))
                .outputItems(DIODE, 2)
                .duration(400).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("diode_polyethylene")
                .inputItems(WIRE_FINE, Copper, 4)
                .inputItems(DUST_SMALL, GalliumArsenide)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(DIODE, 2)
                .duration(400).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("diode_polyethylene_wafer")
                .inputItems(WIRE_FINE, Copper, 4)
                .inputItems(SILICON_WAFER)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(DIODE, 2)
                .duration(400).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("diode_polyethylene_annealed")
                .inputItems(WIRE_FINE, AnnealedCopper, 4)
                .inputItems(DUST_SMALL, GalliumArsenide)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(DIODE, 4)
                .duration(400).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("diode_polyethylene_annealed_wafer")
                .inputItems(WIRE_FINE, AnnealedCopper, 4)
                .inputItems(SILICON_WAFER)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(DIODE, 4)
                .duration(400).EUt(VA[LV]).save(provider);

        // Inductor
        ASSEMBLER_RECIPES.recipeBuilder("inductor")
                .inputItems(RING, Steel)
                .inputItems(WIRE_FINE, Copper, 2)
                .inputFluids(Polyethylene.getFluid(L / 4))
                .outputItems(INDUCTOR, 2)
                .duration(320).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("inductor_annealed")
                .inputItems(RING, Steel)
                .inputItems(WIRE_FINE, AnnealedCopper, 2)
                .inputFluids(Polyethylene.getFluid(L / 4))
                .outputItems(INDUCTOR, 4)
                .duration(320).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("inductor_nzf")
                .inputItems(RING, NickelZincFerrite)
                .inputItems(WIRE_FINE, Copper, 2)
                .inputFluids(Polyethylene.getFluid(L / 4))
                .outputItems(INDUCTOR, 4)
                .duration(320).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("inductor_nzf_annealed")
                .inputItems(RING, NickelZincFerrite)
                .inputItems(WIRE_FINE, AnnealedCopper, 2)
                .inputFluids(Polyethylene.getFluid(L / 4))
                .outputItems(INDUCTOR, 8)
                .duration(320).EUt(VA[MV]).save(provider);

        // SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder("smd_resistor_electrum")
                .inputItems(DUST, Carbon)
                .inputItems(WIRE_FINE, Electrum, 4)
                .inputFluids(Polyethylene.getFluid(L * 2))
                .outputItems(SMD_RESISTOR, 16)
                .duration(160).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("smd_resistor_tantalum")
                .inputItems(DUST, Carbon)
                .inputItems(WIRE_FINE, Tantalum, 4)
                .inputFluids(Polyethylene.getFluid(L * 2))
                .outputItems(SMD_RESISTOR, 32)
                .duration(160).EUt(VA[HV]).save(provider);

        // SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder("smd_diode")
                .inputItems(DUST, GalliumArsenide)
                .inputItems(WIRE_FINE, Platinum, 8)
                .inputFluids(Polyethylene.getFluid(L * 2))
                .outputItems(SMD_DIODE, 32)
                .duration(200).EUt(VA[HV]).save(provider);

        // SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder("smd_transistor_annealed_copper")
                .inputItems(FOIL, Gallium)
                .inputItems(WIRE_FINE, AnnealedCopper, 8)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(SMD_TRANSISTOR, 16)
                .duration(160).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("smd_transistor_tantalum")
                .inputItems(FOIL, Gallium)
                .inputItems(WIRE_FINE, Tantalum, 8)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(SMD_TRANSISTOR, 32)
                .duration(160).EUt(VA[HV]).save(provider);

        // SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder("smd_capacitor_silicone")
                .inputItems(FOIL, SiliconeRubber)
                .inputItems(FOIL, Aluminium)
                .inputFluids(Polyethylene.getFluid(L / 2))
                .outputItems(SMD_CAPACITOR, 8)
                .duration(80).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("smd_capacitor_pvc")
                .inputItems(FOIL, PolyvinylChloride, 2)
                .inputItems(FOIL, Aluminium)
                .inputFluids(Polyethylene.getFluid(L / 2))
                .outputItems(SMD_CAPACITOR, 12)
                .duration(80).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("smd_capacitor_silicone_tantalum")
                .inputItems(FOIL, SiliconeRubber)
                .inputItems(FOIL, Tantalum)
                .inputFluids(Polyethylene.getFluid(L / 2))
                .outputItems(SMD_CAPACITOR, 16)
                .duration(120).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("smd_capacitor_pvc_tantalum")
                .inputItems(FOIL, PolyvinylChloride, 2)
                .inputItems(FOIL, Tantalum)
                .inputFluids(Polyethylene.getFluid(L / 2))
                .outputItems(SMD_CAPACITOR, 24)
                .duration(120).EUt(VA[HV]).save(provider);

        // SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder("smd_inductor")
                .inputItems(RING, NickelZincFerrite)
                .inputItems(WIRE_FINE, Cupronickel, 4)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(SMD_INDUCTOR, 16)
                .duration(160).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("inductor_tantalum")
                .inputItems(RING, NickelZincFerrite)
                .inputItems(WIRE_FINE, Tantalum, 4)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(SMD_INDUCTOR, 32)
                .duration(160).EUt(VA[HV]).save(provider);

        // Advanced SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder("asmd_resistor")
                .inputItems(DUST, Graphene)
                .inputItems(WIRE_FINE, Platinum, 4)
                .inputFluids(Polybenzimidazole.getFluid(L * 2))
                .outputItems(ADVANCED_SMD_RESISTOR, 16)
                .EUt(3840).duration(160).save(provider);

        // Advanced SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder("asmd_diode")
                .inputItems(DUST, IndiumGalliumPhosphide)
                .inputItems(WIRE_FINE, NiobiumTitanium, 16)
                .inputFluids(Polybenzimidazole.getFluid(L * 2))
                .outputItems(ADVANCED_SMD_DIODE, 64)
                .EUt(3840).duration(640).save(provider);

        // Advanced SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder("asmd_transistor")
                .inputItems(FOIL, VanadiumGallium)
                .inputItems(WIRE_FINE, HSSG, 8)
                .inputFluids(Polybenzimidazole.getFluid(L))
                .outputItems(ADVANCED_SMD_TRANSISTOR, 16)
                .EUt(3840).duration(160).save(provider);

        // Advanced SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder("asmd_capacitor")
                .inputItems(FOIL, Polybenzimidazole, 2)
                .inputItems(FOIL, HSSS)
                .inputFluids(Polybenzimidazole.getFluid(L / 4))
                .outputItems(ADVANCED_SMD_CAPACITOR, 16)
                .EUt(3840).duration(80).save(provider);

        // Advanced SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder("asmd_inductor")
                .inputItems(RING, HSSE)
                .inputItems(WIRE_FINE, Palladium, 4)
                .inputFluids(Polybenzimidazole.getFluid(L))
                .outputItems(ADVANCED_SMD_INDUCTOR, 16)
                .EUt(3840).duration(160).save(provider);

        // Carbon Fibers
        AUTOCLAVE_RECIPES.recipeBuilder("carbon_fibers_polyethylene")
                .inputItems(DUST, Carbon, 4)
                .inputFluids(Polyethylene.getFluid(L / 4))
                .outputItems(CARBON_FIBERS)
                .duration(37).EUt(VA[LV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("carbon_fibers_ptfe")
                .inputItems(DUST, Carbon, 4)
                .inputFluids(Polytetrafluoroethylene.getFluid(L / 8))
                .outputItems(CARBON_FIBERS, 2)
                .duration(37).EUt(VA[MV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("carbon_fibers_epoxy")
                .inputItems(DUST, Carbon, 4)
                .inputFluids(Epoxy.getFluid(L / 16))
                .outputItems(CARBON_FIBERS, 4)
                .duration(37).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("carbon_fibers_pbi")
                .inputItems(DUST, Carbon, 8)
                .inputFluids(Polybenzimidazole.getFluid(L / 16))
                .outputItems(CARBON_FIBERS, 16)
                .duration(37).EUt(VA[EV]).save(provider);

        // Crystal Circuit Components
        LASER_ENGRAVER_RECIPES.recipeBuilder("crystal_cpu")
                .inputItems(ENGRAVED_CRYSTAL_CHIP)
                .notConsumable(LENS, Color.Lime)
                .outputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(10000).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("crystal_soc")
                .inputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .notConsumable(LENS, Color.Blue)
                .outputItems(CRYSTAL_SYSTEM_ON_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(40000).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("raw_crystal_chip_emerald")
                .inputItems(GEM_EXQUISITE, Emerald)
                .inputFluids(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("raw_crystal_chip_olivine")
                .inputItems(GEM_EXQUISITE, Olivine)
                .inputFluids(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("raw_crystal_chip_part")
                .inputItems(RAW_CRYSTAL_CHIP)
                .outputItems(RAW_CRYSTAL_CHIP_PART, 9)
                .EUt(VA[HV]).duration(100).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("raw_crystal_chip_from_part_europium")
                .inputItems(RAW_CRYSTAL_CHIP_PART)
                .inputFluids(Europium.getFluid(L / 9))
                .outputItems(RAW_CRYSTAL_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("raw_crystal_chip_from_part_mutagen")
                .inputItems(RAW_CRYSTAL_CHIP_PART)
                .inputFluids(Mutagen.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("raw_crystal_chip_from_part_bacterial_sludge")
                .inputItems(RAW_CRYSTAL_CHIP_PART)
                .inputFluids(BacterialSludge.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).save(provider);

        BLAST_RECIPES.recipeBuilder("engraved_crystal_chip_from_emerald")
                .inputItems(PLATE, Emerald)
                .inputItems(RAW_CRYSTAL_CHIP)
                .inputFluids(Helium.getFluid(1000))
                .outputItems(ENGRAVED_CRYSTAL_CHIP)
                .blastFurnaceTemp(5000)
                .duration(900).EUt(VA[HV]).save(provider);

        BLAST_RECIPES.recipeBuilder("engraved_crystal_chip_from_olivine")
                .inputItems(PLATE, Olivine)
                .inputItems(RAW_CRYSTAL_CHIP)
                .inputFluids(Helium.getFluid(1000))
                .outputItems(ENGRAVED_CRYSTAL_CHIP)
                .blastFurnaceTemp(5000)
                .duration(900).EUt(VA[HV]).save(provider);

        // Quantum Parts
        CHEMICAL_BATH_RECIPES.recipeBuilder("quantum_eye")
                .inputItems(GEM, EnderEye)
                .inputFluids(Radon.getFluid(250))
                .outputItems(QUANTUM_EYE)
                .duration(480).EUt(VA[HV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("quantum_star")
                .inputItems(GEM, NetherStar)
                .inputFluids(Radon.getFluid(1250))
                .outputItems(QUANTUM_STAR)
                .duration(1920).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("gravi_star")
                .inputItems(QUANTUM_STAR)
                .inputFluids(Neutronium.getFluid(L * 2))
                .outputItems(GRAVI_STAR)
                .duration(480).EUt(VA[IV]).save(provider);
    }

    private static void boardRecipes(Consumer<FinishedRecipe> provider) {
        // Coated Board
        VanillaRecipeHelper.addShapedRecipe(provider, "coated_board", COATED_BOARD.asStack(3),
                "RRR", "PPP", "RRR",
                'R', STICKY_RESIN.asStack(),
                'P', new MaterialEntry(PLATE, Wood));

        VanillaRecipeHelper.addShapelessRecipe(provider, "coated_board_1x", COATED_BOARD.asStack(),
                new MaterialEntry(PLATE, Wood),
                STICKY_RESIN.asStack(),
                STICKY_RESIN.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "basic_circuit_board", BASIC_CIRCUIT_BOARD.asStack(),
                "WWW", "WBW", "WWW",
                'W', new MaterialEntry(WIRE_GT_SINGLE, Copper),
                'B', COATED_BOARD.asStack());

        // Basic Circuit Board
        ASSEMBLER_RECIPES.recipeBuilder("basic_circuit_board")
                .inputItems(FOIL, Copper, 4)
                .inputItems(PLATE, Wood)
                .inputFluids(Glue.getFluid(100))
                .outputItems(BASIC_CIRCUIT_BOARD)
                .duration(200).EUt(VA[ULV]).save(provider);

        // Phenolic Board
        ASSEMBLER_RECIPES.recipeBuilder("phenolic_board")
                .inputItems(DUST, Wood)
                .circuitMeta(1)
                .inputFluids(Glue.getFluid(50))
                .outputItems(PHENOLIC_BOARD)
                .duration(150).EUt(VA[LV]).save(provider);

        // Good Circuit Board
        VanillaRecipeHelper.addShapedRecipe(provider, "good_circuit_board", GOOD_CIRCUIT_BOARD.asStack(),
                "WWW", "WBW", "WWW",
                'W', new MaterialEntry(WIRE_GT_SINGLE, Silver),
                'B', PHENOLIC_BOARD.asStack());

        CHEMICAL_RECIPES.recipeBuilder("good_circuit_board_persulfate").EUt(VA[LV]).duration(300)
                .inputItems(FOIL, Silver, 4)
                .inputItems(PHENOLIC_BOARD)
                .inputFluids(SodiumPersulfate.getFluid(200))
                .outputItems(GOOD_CIRCUIT_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("good_circuit_board_iron3").EUt(VA[LV]).duration(300)
                .inputItems(FOIL, Silver, 4)
                .inputItems(PHENOLIC_BOARD)
                .inputFluids(Iron3Chloride.getFluid(100))
                .outputItems(GOOD_CIRCUIT_BOARD)
                .save(provider);

        // Plastic Board
        CHEMICAL_RECIPES.recipeBuilder("plastic_board_polyethylene").duration(500).EUt(10)
                .inputItems(PLATE, Polyethylene)
                .inputItems(FOIL, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("plastic_board_pvc").duration(500).EUt(10)
                .inputItems(PLATE, PolyvinylChloride)
                .inputItems(FOIL, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD, 2)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("plastic_board_ptfe").duration(500).EUt(10)
                .inputItems(PLATE, Polytetrafluoroethylene)
                .inputItems(FOIL, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD, 4)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("plastic_board_pbi").duration(500).EUt(10)
                .inputItems(PLATE, Polybenzimidazole)
                .inputItems(FOIL, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD, 8)
                .save(provider);

        // Plastic Circuit Board
        CHEMICAL_RECIPES.recipeBuilder("plastic_circuit_board_persulfate").duration(600).EUt(VA[LV])
                .inputItems(PLASTIC_BOARD)
                .inputItems(FOIL, Copper, 6)
                .inputFluids(SodiumPersulfate.getFluid(500))
                .outputItems(PLASTIC_CIRCUIT_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("plastic_circuit_board_iron3").duration(600).EUt(VA[LV])
                .inputItems(PLASTIC_BOARD)
                .inputItems(FOIL, Copper, 6)
                .inputFluids(Iron3Chloride.getFluid(250))
                .outputItems(PLASTIC_CIRCUIT_BOARD)
                .save(provider);

        // Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder("epoxy_board").duration(600).EUt(VA[LV])
                .inputItems(PLATE, Epoxy)
                .inputItems(FOIL, Gold, 8)
                .inputFluids(SulfuricAcid.getFluid(500))
                .outputItems(EPOXY_BOARD)
                .save(provider);

        // Advanced Circuit Board
        CHEMICAL_RECIPES.recipeBuilder("advanced_circuit_board_persulfate").duration(900).EUt(VA[LV])
                .inputItems(EPOXY_BOARD)
                .inputItems(FOIL, Electrum, 8)
                .inputFluids(SodiumPersulfate.getFluid(1000))
                .outputItems(ADVANCED_CIRCUIT_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("advanced_circuit_board_iron3").duration(900).EUt(VA[LV])
                .inputItems(EPOXY_BOARD)
                .inputItems(FOIL, Electrum, 8)
                .inputFluids(Iron3Chloride.getFluid(500))
                .outputItems(ADVANCED_CIRCUIT_BOARD)
                .save(provider);

        // Fiber Reinforced Epoxy Board
        CHEMICAL_BATH_RECIPES.recipeBuilder("reinforced_epoxy_sheet_glass").duration(240).EUt(16)
                .inputItems(WIRE_FINE, BorosilicateGlass)
                .inputFluids(Epoxy.getFluid(L))
                .outputItems(PLATE, ReinforcedEpoxyResin)
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("reinforced_epoxy_sheet_carbon_fibers").duration(240).EUt(16)
                .inputItems(CARBON_FIBERS)
                .inputFluids(Epoxy.getFluid(L))
                .outputItems(PLATE, ReinforcedEpoxyResin)
                .save(provider);

        // Borosilicate Glass Recipes
        EXTRUDER_RECIPES.recipeBuilder("borosilicate_glass_fine_wire").duration(160).EUt(96)
                .inputItems(INGOT, BorosilicateGlass)
                .notConsumable(SHAPE_EXTRUDER_WIRE)
                .outputItems(WIRE_FINE, BorosilicateGlass, 8)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("fiber_board").duration(500).EUt(10)
                .inputItems(PLATE, ReinforcedEpoxyResin)
                .inputItems(FOIL, AnnealedCopper, 8)
                .inputFluids(SulfuricAcid.getFluid(125))
                .outputItems(FIBER_BOARD)
                .save(provider);

        // Extreme Circuit Board
        CHEMICAL_RECIPES.recipeBuilder("extreme_circuit_board_persulfate").duration(1200).EUt(VA[LV])
                .inputItems(FIBER_BOARD)
                .inputItems(FOIL, AnnealedCopper, 12)
                .inputFluids(SodiumPersulfate.getFluid(2000))
                .outputItems(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("extreme_circuit_board_iron3").duration(1200).EUt(VA[LV])
                .inputItems(FIBER_BOARD)
                .inputItems(FOIL, AnnealedCopper, 12)
                .inputFluids(Iron3Chloride.getFluid(1000))
                .outputItems(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Multi-Layer Fiber Reinforced Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder("multilayer_fiber_board").duration(500).EUt(VA[HV])
                .inputItems(FIBER_BOARD, 2)
                .inputItems(FOIL, Palladium, 8)
                .inputFluids(SulfuricAcid.getFluid(500))
                .outputItems(MULTILAYER_FIBER_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Elite Circuit Board
        CHEMICAL_RECIPES.recipeBuilder("elite_circuit_board_persulfate").duration(1500).EUt(VA[MV])
                .inputItems(MULTILAYER_FIBER_BOARD)
                .inputItems(FOIL, Platinum, 8)
                .inputFluids(SodiumPersulfate.getFluid(4000))
                .outputItems(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("elite_circuit_board_iron3").duration(1500).EUt(VA[MV])
                .inputItems(MULTILAYER_FIBER_BOARD)
                .inputItems(FOIL, Platinum, 8)
                .inputFluids(Iron3Chloride.getFluid(2000))
                .outputItems(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Wetware Board

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("petri_dish_ptfe").duration(160).EUt(VA[HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .inputFluids(Polytetrafluoroethylene.getFluid(L / 4))
                .outputItems(PETRI_DISH)
                .save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("petri_dish_pbi").duration(40).EUt(VA[HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .inputFluids(Polybenzimidazole.getFluid(L / 8))
                .outputItems(PETRI_DISH, 2)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("wetware_board").duration(1200).EUt(VA[LuV])
                .inputItems(MULTILAYER_FIBER_BOARD, 16)
                .inputItems(PETRI_DISH)
                .inputItems(ELECTRIC_PUMP_LuV)
                .inputItems(SENSOR_IV)
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(FOIL, NiobiumTitanium, 16)
                .inputFluids(SterileGrowthMedium.getFluid(4000))
                .outputItems(WETWARE_BOARD, 16)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("wetware_circuit_board_persulfate").duration(1800).EUt(VA[HV])
                .inputItems(WETWARE_BOARD)
                .inputItems(FOIL, NiobiumTitanium, 32)
                .inputFluids(SodiumPersulfate.getFluid(10000))
                .outputItems(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("wetware_circuit_board_iron3").duration(1800).EUt(VA[HV])
                .inputItems(WETWARE_BOARD)
                .inputItems(FOIL, NiobiumTitanium, 32)
                .inputFluids(Iron3Chloride.getFluid(5000))
                .outputItems(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);
    }

    private static void circuitRecipes(Consumer<FinishedRecipe> provider) {
        int outputAmount = ConfigHolder.INSTANCE.recipes.harderCircuitRecipes ? 1 : 2;

        // T1: Electronic ==============================================================================================

        // LV
        VanillaRecipeHelper.addShapedRecipe(provider, "electronic_circuit_lv", ELECTRONIC_CIRCUIT_LV.asStack(),
                "RPR", "VBV", "CCC",
                'R', RESISTOR.asStack(),
                'P', new MaterialEntry(PLATE, Steel),
                'V', VACUUM_TUBE.asStack(),
                'B', BASIC_CIRCUIT_BOARD.asStack(),
                'C', new MaterialEntry(CABLE_GT_SINGLE, RedAlloy));

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("electronic_circuit_lv").EUt(16).duration(200)
                .inputItems(BASIC_CIRCUIT_BOARD)
                .inputItems(CustomTags.RESISTORS, 2)
                .inputItems(WIRE_GT_SINGLE, RedAlloy, 2)
                .inputItems(CustomTags.ULV_CIRCUITS, 2)
                .outputItems(ELECTRONIC_CIRCUIT_LV, outputAmount)
                .save(provider);

        // MV
        VanillaRecipeHelper.addShapedRecipe(provider, "electronic_circuit_mv", ELECTRONIC_CIRCUIT_MV.asStack(),
                "DPD", "CBC", "WCW",
                'W', new MaterialEntry(WIRE_GT_SINGLE, Copper),
                'P', new MaterialEntry(PLATE, Steel),
                'C', ELECTRONIC_CIRCUIT_LV.asStack(),
                'B', GOOD_CIRCUIT_BOARD.asStack(),
                'D', DIODE.asStack());

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("electronic_circuit_mv").EUt(VA[LV]).duration(300)
                .inputItems(GOOD_CIRCUIT_BOARD)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .inputItems(CustomTags.DIODES, 2)
                .inputItems(WIRE_GT_SINGLE, Copper, 2)
                .outputItems(ELECTRONIC_CIRCUIT_MV)
                .save(provider);

        // T2: Integrated ==============================================================================================

        // LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("integrated_circuit_lv").EUt(16).duration(200)
                .inputItems(BASIC_CIRCUIT_BOARD)
                .inputItems(INTEGRATED_LOGIC_CIRCUIT)
                .inputItems(CustomTags.RESISTORS, 2)
                .inputItems(CustomTags.DIODES, 2)
                .inputItems(WIRE_FINE, Copper, 2)
                .inputItems(BOLT, Tin, 2)
                .outputItems(INTEGRATED_CIRCUIT_LV, outputAmount)
                .save(provider);

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("integrated_circuit_mv").EUt(24).duration(400)
                .inputItems(GOOD_CIRCUIT_BOARD)
                .inputItems(INTEGRATED_CIRCUIT_LV, 2)
                .inputItems(CustomTags.RESISTORS, 2)
                .inputItems(CustomTags.DIODES, 2)
                .inputItems(WIRE_FINE, Gold, 4)
                .inputItems(BOLT, Silver, 4)
                .outputItems(INTEGRATED_CIRCUIT_MV, outputAmount)
                .save(provider);

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("integrated_circuit_hv").EUt(VA[LV]).duration(800)
                .inputItems(INTEGRATED_CIRCUIT_MV, outputAmount)// a little generous for this first HV if harder recipes
                                                                // enabled
                .inputItems(INTEGRATED_LOGIC_CIRCUIT, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 2)
                .inputItems(CustomTags.TRANSISTORS, 4)
                .inputItems(WIRE_FINE, Electrum, 8)
                .inputItems(BOLT, AnnealedCopper, 8)
                .outputItems(INTEGRATED_CIRCUIT_HV)
                .save(provider);

        // T2.5: Misc ==================================================================================================

        // NAND Chip ULV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nand_chip_ulv_good_board").EUt(VA[MV]).duration(300)
                .inputItems(GOOD_CIRCUIT_BOARD)
                .inputItems(SIMPLE_SYSTEM_ON_CHIP)
                .inputItems(BOLT, RedAlloy, 2)
                .inputItems(WIRE_FINE, Tin, 2)
                .outputItems(NAND_CHIP_ULV, outputAmount * 4)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nand_chip_ulv_plastic_board").EUt(VA[MV]).duration(300)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(SIMPLE_SYSTEM_ON_CHIP)
                .inputItems(BOLT, RedAlloy, 2)
                .inputItems(WIRE_FINE, Tin, 2)
                .outputItems(NAND_CHIP_ULV, outputAmount * 6)
                .save(provider);

        // Microprocessor LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("microprocessor_lv").EUt(60).duration(200)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(CENTRAL_PROCESSING_UNIT)
                .inputItems(CustomTags.RESISTORS, 2)
                .inputItems(CustomTags.CAPACITORS, 2)
                .inputItems(CustomTags.TRANSISTORS, 2)
                .inputItems(WIRE_FINE, Copper, 2)
                .outputItems(MICROPROCESSOR_LV, ConfigHolder.INSTANCE.recipes.harderCircuitRecipes ? 2 : 3)
                .save(provider);

        // Microprocessor LV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("microprocessor_lv_soc").EUt(600).duration(50)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(SYSTEM_ON_CHIP)
                .inputItems(WIRE_FINE, Copper, 2)
                .inputItems(BOLT, Tin, 2)
                .outputItems(MICROPROCESSOR_LV, ConfigHolder.INSTANCE.recipes.harderCircuitRecipes ? 3 : 6)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T3: Processor ===============================================================================================

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("processor_mv").EUt(60).duration(200)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(CENTRAL_PROCESSING_UNIT)
                .inputItems(CustomTags.RESISTORS, 4)
                .inputItems(CustomTags.CAPACITORS, 4)
                .inputItems(CustomTags.TRANSISTORS, 4)
                .inputItems(WIRE_FINE, RedAlloy, 4)
                .outputItems(PROCESSOR_MV, outputAmount)
                .save(provider);

        // MV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("processor_mv_soc").EUt(2400).duration(50)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(SYSTEM_ON_CHIP)
                .inputItems(WIRE_FINE, RedAlloy, 4)
                .inputItems(BOLT, AnnealedCopper, 4)
                .outputItems(PROCESSOR_MV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("processor_assembly_hv").EUt(VA[MV]).duration(400)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(PROCESSOR_MV, 2)
                .inputItems(CustomTags.INDUCTORS, 4)
                .inputItems(CustomTags.CAPACITORS, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(WIRE_FINE, RedAlloy, 8)
                .outputItems(PROCESSOR_ASSEMBLY_HV, 2)
                .solderMultiplier(2)
                .save(provider);

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("workstation_ev").EUt(VA[MV]).duration(400)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(PROCESSOR_ASSEMBLY_HV, 2)
                .inputItems(CustomTags.DIODES, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(WIRE_FINE, Electrum, 16)
                .inputItems(BOLT, BlueAlloy, 16)
                .outputItems(WORKSTATION_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("mainframe_iv").EUt(VA[HV]).duration(800)
                .inputItems(FRAME_GT, Aluminium, 2)
                .inputItems(WORKSTATION_EV, 2)
                .inputItems(CustomTags.INDUCTORS, 8)
                .inputItems(CustomTags.CAPACITORS, 16)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 16)
                .outputItems(MAINFRAME_IV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("mainframe_iv_asmd").EUt(VA[HV]).duration(400)
                .inputItems(FRAME_GT, Aluminium, 2)
                .inputItems(WORKSTATION_EV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 16)
                .outputItems(MAINFRAME_IV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T4: Nano ====================================================================================================

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_processor_hv").EUt(600).duration(200)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(SMD_RESISTOR, 8)
                .inputItems(SMD_CAPACITOR, 8)
                .inputItems(SMD_TRANSISTOR, 8)
                .inputItems(WIRE_FINE, Electrum, 8)
                .outputItems(NANO_PROCESSOR_HV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_processor_hv_asmd").EUt(600).duration(100)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(ADVANCED_SMD_RESISTOR, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 2)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 2)
                .inputItems(WIRE_FINE, Electrum, 8)
                .outputItems(NANO_PROCESSOR_HV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // HV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_processor_hv_soc").EUt(9600).duration(50)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(ADVANCED_SYSTEM_ON_CHIP)
                .inputItems(WIRE_FINE, Electrum, 4)
                .inputItems(BOLT, Platinum, 4)
                .outputItems(NANO_PROCESSOR_HV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_processor_assembly_ev").EUt(600).duration(400)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_HV, 2)
                .inputItems(SMD_INDUCTOR, 4)
                .inputItems(SMD_CAPACITOR, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 8)
                .inputItems(WIRE_FINE, Electrum, 16)
                .outputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_processor_assembly_ev_asmd").EUt(600).duration(200)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_HV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR)
                .inputItems(ADVANCED_SMD_CAPACITOR, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 8)
                .inputItems(WIRE_FINE, Electrum, 16)
                .outputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_computer_iv").EUt(600).duration(400)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .inputItems(SMD_DIODE, 8)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_FINE, Electrum, 16)
                .outputItems(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_computer_iv_asmd").EUt(600).duration(200)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .inputItems(ADVANCED_SMD_DIODE, 2)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_FINE, Electrum, 16)
                .outputItems(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_mainframe_luv").EUt(VA[EV]).duration(800)
                .inputItems(FRAME_GT, Aluminium, 2)
                .inputItems(NANO_COMPUTER_IV, 2)
                .inputItems(SMD_INDUCTOR, 16)
                .inputItems(SMD_CAPACITOR, 32)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 32)
                .outputItems(NANO_MAINFRAME_LuV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("nano_mainframe_luv_asmd").EUt(VA[EV]).duration(400)
                .inputItems(FRAME_GT, Aluminium, 2)
                .inputItems(NANO_COMPUTER_IV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 4)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 32)
                .outputItems(NANO_MAINFRAME_LuV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T5: Quantum =================================================================================================

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_processor_ev").EUt(2400).duration(200)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUBIT_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(SMD_CAPACITOR, 12)
                .inputItems(SMD_TRANSISTOR, 12)
                .inputItems(WIRE_FINE, Platinum, 12)
                .outputItems(QUANTUM_PROCESSOR_EV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_processor_ev_asmd").EUt(2400).duration(100)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUBIT_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(ADVANCED_SMD_CAPACITOR, 3)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 3)
                .inputItems(WIRE_FINE, Platinum, 12)
                .outputItems(QUANTUM_PROCESSOR_EV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // EV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_processor_ev_soc").EUt(38400).duration(50)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(ADVANCED_SYSTEM_ON_CHIP)
                .inputItems(WIRE_FINE, Platinum, 12)
                .inputItems(BOLT, NiobiumTitanium, 8)
                .outputItems(QUANTUM_PROCESSOR_EV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_assembly_iv").EUt(2400).duration(400)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_PROCESSOR_EV, 2)
                .inputItems(SMD_INDUCTOR, 8)
                .inputItems(SMD_CAPACITOR, 16)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(WIRE_FINE, Platinum, 16)
                .outputItems(QUANTUM_ASSEMBLY_IV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_assembly_iv_asmd").EUt(2400).duration(200)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_PROCESSOR_EV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(WIRE_FINE, Platinum, 16)
                .outputItems(QUANTUM_ASSEMBLY_IV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_computer_luv").EUt(2400).duration(400)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_ASSEMBLY_IV, 2)
                .inputItems(SMD_DIODE, 8)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_FINE, Platinum, 32)
                .outputItems(QUANTUM_COMPUTER_LuV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_computer_luv_asmd").EUt(2400).duration(200)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_ASSEMBLY_IV, 2)
                .inputItems(ADVANCED_SMD_DIODE, 2)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(WIRE_FINE, Platinum, 32)
                .outputItems(QUANTUM_COMPUTER_LuV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_mainframe_zpm").EUt(VA[IV]).duration(800)
                .inputItems(FRAME_GT, HSSG, 2)
                .inputItems(QUANTUM_COMPUTER_LuV, 2)
                .inputItems(SMD_INDUCTOR, 24)
                .inputItems(SMD_CAPACITOR, 48)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 48)
                .solderMultiplier(4)
                .outputItems(QUANTUM_MAINFRAME_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("quantum_mainframe_zpm_asmd").EUt(VA[IV]).duration(400)
                .inputItems(FRAME_GT, HSSG, 2)
                .inputItems(QUANTUM_COMPUTER_LuV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 6)
                .inputItems(ADVANCED_SMD_CAPACITOR, 12)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(WIRE_GT_SINGLE, AnnealedCopper, 48)
                .solderMultiplier(4)
                .outputItems(QUANTUM_MAINFRAME_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T6: Crystal =================================================================================================

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("crystal_processor_iv").EUt(9600).duration(200)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 6)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 6)
                .inputItems(WIRE_FINE, NiobiumTitanium, 8)
                .outputItems(CRYSTAL_PROCESSOR_IV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("crystal_processor_iv_soc").EUt(86000).duration(100)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_SYSTEM_ON_CHIP)
                .inputItems(WIRE_FINE, NiobiumTitanium, 8)
                .inputItems(BOLT, YttriumBariumCuprate, 8)
                .outputItems(CRYSTAL_PROCESSOR_IV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("crystal_assembly_luv").EUt(9600).duration(400)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_PROCESSOR_IV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 4)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(WIRE_FINE, NiobiumTitanium, 16)
                .outputItems(CRYSTAL_ASSEMBLY_LuV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("crystal_computer_zpm").EUt(9600).duration(400)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_ASSEMBLY_LuV, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(NOR_MEMORY_CHIP, 32)
                .inputItems(NAND_MEMORY_CHIP, 64)
                .inputItems(WIRE_FINE, NiobiumTitanium, 32)
                .solderMultiplier(2)
                .outputItems(CRYSTAL_COMPUTER_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder("crystal_mainframe_uv").EUt(VA[LuV]).duration(800)
                .inputItems(FRAME_GT, HSSE, 2)
                .inputItems(CRYSTAL_COMPUTER_ZPM, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 32)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(WIRE_GT_SINGLE, NiobiumTitanium, 8)
                .inputItems(ADVANCED_SMD_INDUCTOR, 8)
                .inputItems(ADVANCED_SMD_CAPACITOR, 16)
                .inputItems(ADVANCED_SMD_DIODE, 8)
                .inputFluids(SolderingAlloy.getFluid(L * 10))
                .outputItems(CRYSTAL_MAINFRAME_UV)
                .stationResearch(b -> b
                        .researchStack(CRYSTAL_COMPUTER_ZPM.asStack())
                        .CWUt(16))
                .save(provider);

        // T7: Wetware =================================================================================================

        // Neuro Processing Unit
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("neuro_processor").EUt(80000).duration(600)
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(STEM_CELLS, 16)
                .inputItems(PIPE_SMALL_FLUID, Polybenzimidazole, 8)
                .inputItems(PLATE, Electrum, 8)
                .inputItems(FOIL, SiliconeRubber, 16)
                .inputItems(BOLT, HSSE, 8)
                .inputFluids(SterileGrowthMedium.getFluid(250))
                .outputItems(NEURO_PROCESSOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("wetware_processor_luv").EUt(38400).duration(200)
                .inputItems(NEURO_PROCESSOR)
                .inputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 8)
                .inputItems(WIRE_FINE, YttriumBariumCuprate, 8)
                .outputItems(WETWARE_PROCESSOR_LuV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // SoC LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("wetware_processor_luv_soc").EUt(150000).duration(100)
                .inputItems(NEURO_PROCESSOR)
                .inputItems(HIGHLY_ADVANCED_SOC)
                .inputItems(WIRE_FINE, YttriumBariumCuprate, 8)
                .inputItems(BOLT, Naquadah, 8)
                .outputItems(WETWARE_PROCESSOR_LuV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("wetware_processor_assembly_zpm").EUt(38400).duration(400)
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(WETWARE_PROCESSOR_LuV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 6)
                .inputItems(ADVANCED_SMD_CAPACITOR, 12)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(WIRE_FINE, YttriumBariumCuprate, 16)
                .solderMultiplier(2)
                .outputItems(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder("wetware_super_computer_uv").EUt(38400).duration(400)
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .inputItems(ADVANCED_SMD_DIODE, 8)
                .inputItems(NOR_MEMORY_CHIP, 16)
                .inputItems(RANDOM_ACCESS_MEMORY, 32)
                .inputItems(WIRE_FINE, YttriumBariumCuprate, 24)
                .inputItems(FOIL, Polybenzimidazole, 32)
                .inputItems(PLATE, Europium, 4)
                .inputFluids(SolderingAlloy.getFluid(1152))
                .outputItems(WETWARE_SUPER_COMPUTER_UV)
                .stationResearch(b -> b
                        .researchStack(WETWARE_PROCESSOR_ASSEMBLY_ZPM.asStack())
                        .CWUt(16))
                .save(provider);

        // UHV
        ASSEMBLY_LINE_RECIPES.recipeBuilder("wetware_mainframe_uhv")
                .inputItems(FRAME_GT, Tritanium, 2)
                .inputItems(WETWARE_SUPER_COMPUTER_UV, 2)
                .inputItems(ADVANCED_SMD_DIODE, 32)
                .inputItems(ADVANCED_SMD_CAPACITOR, 32)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 32)
                .inputItems(ADVANCED_SMD_RESISTOR, 32)
                .inputItems(ADVANCED_SMD_INDUCTOR, 32)
                .inputItems(FOIL, Polybenzimidazole, 64)
                .inputItems(RANDOM_ACCESS_MEMORY, 32)
                .inputItems(WIRE_GT_DOUBLE, EnrichedNaquadahTriniumEuropiumDuranide, 16)
                .inputItems(PLATE, Europium, 8)
                .inputFluids(SolderingAlloy.getFluid(L * 20))
                .inputFluids(Polybenzimidazole.getFluid(L * 8))
                .outputItems(WETWARE_MAINFRAME_UHV)
                .stationResearch(b -> b
                        .researchStack(WETWARE_SUPER_COMPUTER_UV.asStack())
                        .CWUt(96)
                        .EUt(VA[UV]))
                .EUt(300000).duration(2000).save(provider);

        // Misc ========================================================================================================

        // Data Stick
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("data_stick")
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(CENTRAL_PROCESSING_UNIT, 2)
                .inputItems(NAND_MEMORY_CHIP, 32)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(WIRE_FINE, RedAlloy, 16)
                .inputItems(PLATE, Polyethylene, 4)
                .outputItems(TOOL_DATA_STICK)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(90).save(provider);

        // Data Orb
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("data_orb")
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(NOR_MEMORY_CHIP, 32)
                .inputItems(NAND_MEMORY_CHIP, 64)
                .inputItems(WIRE_FINE, Platinum, 32)
                .outputItems(TOOL_DATA_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(1200).save(provider);

        // Data Module
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("data_module")
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 32)
                .inputItems(NOR_MEMORY_CHIP, 64)
                .inputItems(NAND_MEMORY_CHIP, 64)
                .inputItems(WIRE_FINE, YttriumBariumCuprate, 32)
                .outputItems(TOOL_DATA_MODULE)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .duration(400).EUt(38400).save(provider);
    }
}
