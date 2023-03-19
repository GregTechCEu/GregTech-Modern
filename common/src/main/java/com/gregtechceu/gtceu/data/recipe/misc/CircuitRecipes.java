package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.*;

public class CircuitRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        waferRecipes(provider);
        componentRecipes(provider);
        boardRecipes(provider);
        circuitRecipes(provider);
    }

    private static void waferRecipes(Consumer<FinishedRecipe> provider) {

        // Boules
        BLAST_RECIPES.recipeBuilder(SILICON_BOULE.getId())
                .inputItems(dust, Silicon, 32)
                .inputItems(dustSmall, GalliumArsenide)
                .outputItems(SILICON_BOULE)
                .blastFurnaceTemp(1784)
                .duration(9000).EUt(GTValues.VA[GTValues.MV]).save(provider);

        BLAST_RECIPES.recipeBuilder(GLOWSTONE_BOULE.getId())
                .inputItems(dust, Silicon, 64)
                .inputItems(dust, Glowstone, 8)
                .inputFluids(Nitrogen.getFluid(8000))
                .outputItems(GLOWSTONE_BOULE)
                .blastFurnaceTemp(2484)
                .duration(12000).EUt(GTValues.VA[GTValues.HV]).save(provider);

        BLAST_RECIPES.recipeBuilder(NAQUADAH_BOULE.getId())
                .inputItems(block, Silicon, 16)
                .inputItems(ingot, Naquadah)
                .inputFluids(Argon.getFluid(8000))
                .outputItems(NAQUADAH_BOULE)
                .blastFurnaceTemp(5400)
                .duration(15000).EUt(GTValues.VA[GTValues.EV]).save(provider);

        BLAST_RECIPES.recipeBuilder(NEUTRONIUM_BOULE.getId())
                .inputItems(block, Silicon, 32)
                .inputItems(ingot, Neutronium, 4)
                .inputFluids(Xenon.getFluid(8000))
                .outputItems(NEUTRONIUM_BOULE)
                .blastFurnaceTemp(6484)
                .duration(18000).EUt(GTValues.VA[GTValues.IV]).save(provider);

        // Boule cutting
        CUTTER_RECIPES.recipeBuilder(SILICON_WAFER.getId())
                .inputItems(SILICON_BOULE)
                .outputItems(SILICON_WAFER, 16)
                .duration(400).EUt(64).save(provider);

        CUTTER_RECIPES.recipeBuilder(GLOWSTONE_WAFER.getId())
                .inputItems(GLOWSTONE_BOULE)
                .outputItems(GLOWSTONE_WAFER, 32)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(800).EUt(GTValues.VA[GTValues.HV]).save(provider);

        CUTTER_RECIPES.recipeBuilder(NAQUADAH_WAFER.getId())
                .inputItems(NAQUADAH_BOULE)
                .outputItems(NAQUADAH_WAFER, 64)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1600).EUt(GTValues.VA[GTValues.EV]).save(provider);

        CUTTER_RECIPES.recipeBuilder(NEUTRONIUM_WAFER.getId())
                .inputItems(NEUTRONIUM_BOULE)
                .outputItems(NEUTRONIUM_WAFER, 64)
                .outputItems(NEUTRONIUM_WAFER, 32)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(2400).EUt(GTValues.VA[GTValues.IV]).save(provider);

        // Wafer engraving
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(INTEGRATED_LOGIC_CIRCUIT_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(SILICON_WAFER).notConsumable(craftingLens, Color.Red).outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(INTEGRATED_LOGIC_CIRCUIT_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Red).outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(INTEGRATED_LOGIC_CIRCUIT_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Red).outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(INTEGRATED_LOGIC_CIRCUIT_WAFER.getId() + ".3")).duration(50).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Red).outputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(RANDOM_ACCESS_MEMORY_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(SILICON_WAFER).notConsumable(craftingLens, Color.Green).outputItems(RANDOM_ACCESS_MEMORY_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(RANDOM_ACCESS_MEMORY_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Green).outputItems(RANDOM_ACCESS_MEMORY_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(RANDOM_ACCESS_MEMORY_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Green).outputItems(RANDOM_ACCESS_MEMORY_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(RANDOM_ACCESS_MEMORY_WAFER.getId() + ".3")).duration(50).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Green).outputItems(RANDOM_ACCESS_MEMORY_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(CENTRAL_PROCESSING_UNIT_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(SILICON_WAFER).notConsumable(craftingLens, Color.LightBlue).outputItems(CENTRAL_PROCESSING_UNIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(CENTRAL_PROCESSING_UNIT_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.LightBlue).outputItems(CENTRAL_PROCESSING_UNIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(CENTRAL_PROCESSING_UNIT_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.LightBlue).outputItems(CENTRAL_PROCESSING_UNIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(CENTRAL_PROCESSING_UNIT_WAFER.getId() + ".3")).duration(50).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.LightBlue).outputItems(CENTRAL_PROCESSING_UNIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(SILICON_WAFER).notConsumable(craftingLens, Color.Blue).outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Blue).outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Blue).outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".3")).duration(50).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Blue).outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(SILICON_WAFER).notConsumable(craftingLens, Color.Orange).outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Orange).outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Orange).outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(LOW_POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".3")).duration(50).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Orange).outputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SIMPLE_SYSTEM_ON_CHIP_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(SILICON_WAFER).notConsumable(craftingLens, Color.Cyan).outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SIMPLE_SYSTEM_ON_CHIP_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Cyan).outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SIMPLE_SYSTEM_ON_CHIP_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Cyan).outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SIMPLE_SYSTEM_ON_CHIP_WAFER.getId() + ".3")).duration(50).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Cyan).outputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(NAND_MEMORY_CHIP_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Gray).outputItems(NAND_MEMORY_CHIP_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(NAND_MEMORY_CHIP_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Gray).outputItems(NAND_MEMORY_CHIP_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(NAND_MEMORY_CHIP_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Gray).outputItems(NAND_MEMORY_CHIP_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(NOR_MEMORY_CHIP_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Pink).outputItems(NOR_MEMORY_CHIP_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(NOR_MEMORY_CHIP_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Pink).outputItems(NOR_MEMORY_CHIP_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(NOR_MEMORY_CHIP_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Pink).outputItems(NOR_MEMORY_CHIP_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Brown).outputItems(POWER_INTEGRATED_CIRCUIT_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Brown).outputItems(POWER_INTEGRATED_CIRCUIT_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(POWER_INTEGRATED_CIRCUIT_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Brown).outputItems(POWER_INTEGRATED_CIRCUIT_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SYSTEM_ON_CHIP_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Yellow).outputItems(SYSTEM_ON_CHIP_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SYSTEM_ON_CHIP_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Yellow).outputItems(SYSTEM_ON_CHIP_WAFER, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(SYSTEM_ON_CHIP_WAFER.getId() + ".2")).duration(200).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Yellow).outputItems(SYSTEM_ON_CHIP_WAFER, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(ADVANCED_SYSTEM_ON_CHIP_WAFER.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.EV]).inputItems(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Purple).outputItems(ADVANCED_SYSTEM_ON_CHIP_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);
        LASER_ENGRAVER_RECIPES.recipeBuilder(new ResourceLocation(ADVANCED_SYSTEM_ON_CHIP_WAFER.getId() + ".1")).duration(500).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Purple).outputItems(ADVANCED_SYSTEM_ON_CHIP_WAFER, 2).cleanroom(CleanroomType.CLEANROOM).save(provider);

        // Can replace this with a Quantum Star/Eye Lens if desired
        LASER_ENGRAVER_RECIPES.recipeBuilder(HIGHLY_ADVANCED_SOC_WAFER.getId()).duration(900).EUt(GTValues.VA[GTValues.IV]).inputItems(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Black).outputItems(HIGHLY_ADVANCED_SOC_WAFER).cleanroom(CleanroomType.CLEANROOM).save(provider);

        // Wafer chemical refining recipes
        CHEMICAL_RECIPES.recipeBuilder(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER.getId())
                .inputItems(POWER_INTEGRATED_CIRCUIT_WAFER)
                .inputItems(dust, IndiumGalliumPhosphide, 2)
                .inputFluids(VanadiumGallium.getFluid(GTValues.L * 2))
                .outputItems(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(GTValues.VA[GTValues.IV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER.getId())
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .inputItems(dust, IndiumGalliumPhosphide, 8)
                .inputFluids(Naquadah.getFluid(GTValues.L * 4))
                .outputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(NANO_CENTRAL_PROCESSING_UNIT_WAFER.getId())
                .inputItems(CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItems(CARBON_FIBERS, 16)
                .inputFluids(Glowstone.getFluid(GTValues.L * 4))
                .outputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(GTValues.VA[GTValues.EV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER.getId() + ".0"))
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItems(QUANTUM_EYE, 2)
                .inputFluids(GalliumArsenide.getFluid(GTValues.L * 2))
                .outputItems(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(900).EUt(GTValues.VA[GTValues.EV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER.getId() + ".1"))
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItems(dust, IndiumGalliumPhosphide)
                .inputFluids(Radon.getFluid(50))
                .outputItems(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(GTValues.VA[GTValues.EV]).save(provider);

        // Wafer cutting
        CUTTER_RECIPES.recipeBuilder(HIGHLY_ADVANCED_SOC.getId()).duration(900).EUt(GTValues.VA[GTValues.IV]).inputItems(HIGHLY_ADVANCED_SOC_WAFER).outputItems(HIGHLY_ADVANCED_SOC, 6).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(ADVANCED_SYSTEM_ON_CHIP.getId()).duration(900).EUt(GTValues.VA[GTValues.EV]).inputItems(ADVANCED_SYSTEM_ON_CHIP_WAFER).outputItems(ADVANCED_SYSTEM_ON_CHIP, 6).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(SYSTEM_ON_CHIP.getId()).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(SYSTEM_ON_CHIP_WAFER).outputItems(SYSTEM_ON_CHIP, 6).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(SIMPLE_SYSTEM_ON_CHIP.getId()).duration(900).EUt(64).inputItems(SIMPLE_SYSTEM_ON_CHIP_WAFER).outputItems(SIMPLE_SYSTEM_ON_CHIP, 6).save(provider);
        CUTTER_RECIPES.recipeBuilder(RANDOM_ACCESS_MEMORY.getId()).duration(900).EUt(96).inputItems(RANDOM_ACCESS_MEMORY_WAFER).outputItems(RANDOM_ACCESS_MEMORY, 32).save(provider);
        CUTTER_RECIPES.recipeBuilder(QUBIT_CENTRAL_PROCESSING_UNIT.getId()).duration(900).EUt(GTValues.VA[GTValues.EV]).inputItems(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER).outputItems(QUBIT_CENTRAL_PROCESSING_UNIT, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getId()).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 6).save(provider);
        CUTTER_RECIPES.recipeBuilder(LOW_POWER_INTEGRATED_CIRCUIT.getId()).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(LOW_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(LOW_POWER_INTEGRATED_CIRCUIT, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(POWER_INTEGRATED_CIRCUIT.getId()).duration(900).EUt(GTValues.VA[GTValues.EV]).inputItems(POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(POWER_INTEGRATED_CIRCUIT, 4).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(HIGH_POWER_INTEGRATED_CIRCUIT.getId()).duration(900).EUt(GTValues.VA[GTValues.IV]).inputItems(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getId()).duration(900).EUt(GTValues.VA[GTValues.LuV]).inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER).outputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(NOR_MEMORY_CHIP.getId()).duration(900).EUt(192).inputItems(NOR_MEMORY_CHIP_WAFER).outputItems(NOR_MEMORY_CHIP, 16).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(NAND_MEMORY_CHIP.getId()).duration(900).EUt(192).inputItems(NAND_MEMORY_CHIP_WAFER).outputItems(NAND_MEMORY_CHIP, 32).cleanroom(CleanroomType.CLEANROOM).save(provider);
        CUTTER_RECIPES.recipeBuilder(CENTRAL_PROCESSING_UNIT.getId()).duration(900).EUt(GTValues.VA[GTValues.MV]).inputItems(CENTRAL_PROCESSING_UNIT_WAFER).outputItems(CENTRAL_PROCESSING_UNIT, 8).save(provider);
        CUTTER_RECIPES.recipeBuilder(INTEGRATED_LOGIC_CIRCUIT.getId()).duration(900).EUt(64).inputItems(INTEGRATED_LOGIC_CIRCUIT_WAFER).outputItems(INTEGRATED_LOGIC_CIRCUIT, 8).save(provider);
        CUTTER_RECIPES.recipeBuilder(NANO_CENTRAL_PROCESSING_UNIT.getId()).duration(900).EUt(GTValues.VA[GTValues.HV]).inputItems(NANO_CENTRAL_PROCESSING_UNIT_WAFER).outputItems(NANO_CENTRAL_PROCESSING_UNIT, 8).cleanroom(CleanroomType.CLEANROOM).save(provider);
    }

    private static void componentRecipes(Consumer<FinishedRecipe> provider) {

        // Vacuum Tube
        VanillaRecipeHelper.addShapedRecipe(provider, "vacuum_tube", VACUUM_TUBE.asStack(),
                "PTP", "WWW",
                'P', new UnificationEntry(bolt, Steel),
                'T', GLASS_TUBE.get(),
                'W', new UnificationEntry(wireGtSingle, Copper));

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(VACUUM_TUBE.getId() + ".0"))
                .inputItems(GLASS_TUBE)
                .inputItems(bolt, Steel)
                .inputItems(wireGtSingle, Copper, 2)
                .circuitMeta(1)
                .outputItems(VACUUM_TUBE, 2)
                .duration(120).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(VACUUM_TUBE.getId() + ".1"))
                .inputItems(GLASS_TUBE)
                .inputItems(bolt, Steel)
                .inputItems(wireGtSingle, Copper, 2)
                .inputFluids(RedAlloy.getFluid(18))
                .outputItems(VACUUM_TUBE, 3)
                .duration(40).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(VACUUM_TUBE.getId() + ".2"))
                .inputItems(GLASS_TUBE)
                .inputItems(bolt, Steel)
                .inputItems(wireGtSingle, AnnealedCopper, 2)
                .inputFluids(RedAlloy.getFluid(18))
                .outputItems(VACUUM_TUBE, 4)
                .duration(40).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder(GLASS_TUBE.getId())
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GLASS_TUBE)
                .duration(160).EUt(16).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder(GLASS_TUBE.getId())
                .inputFluids(Glass.getFluid(GTValues.L))
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GLASS_TUBE)
                .duration(200).EUt(24).save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder(GLASS_TUBE.getId())
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GLASS_TUBE)
                .duration(80).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        // Resistor
        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.get(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Coal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_fine", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.get(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Coal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_charcoal", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.get(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_fine_charcoal", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.get(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_carbon", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.get(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Carbon));

        VanillaRecipeHelper.addShapedRecipe(provider, "resistor_wire_fine_carbon", RESISTOR.asStack(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.get(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(RESISTOR.getId() + ".0"))
                .inputItems(dust, Coal)
                .inputItems(wireFine, Copper, 4)
                .outputItems(RESISTOR, 2)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(RESISTOR.getId() + ".1"))
                .inputItems(dust, Charcoal)
                .inputItems(wireFine, Copper, 4)
                .outputItems(RESISTOR, 2)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(RESISTOR.getId() + ".2"))
                .inputItems(dust, Carbon)
                .inputItems(wireFine, Copper, 4)
                .outputItems(RESISTOR, 2)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(RESISTOR.getId() + ".3"))
                .inputItems(dust, Coal)
                .inputItems(wireFine, AnnealedCopper, 4)
                .outputItems(RESISTOR, 4)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(RESISTOR.getId() + ".4"))
                .inputItems(dust, Charcoal)
                .inputItems(wireFine, AnnealedCopper, 4)
                .outputItems(RESISTOR, 4)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(RESISTOR.getId() + ".5"))
                .inputItems(dust, Carbon)
                .inputItems(wireFine, AnnealedCopper, 4)
                .outputItems(RESISTOR, 4)
                .inputFluids(Glue.getFluid(100))
                .duration(160).EUt(6).save(provider);

        // Capacitor
        ASSEMBLER_RECIPES.recipeBuilder(CAPACITOR.getId())
                .inputItems(foil, Polyethylene)
                .inputItems(foil, Aluminium, 2)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(CAPACITOR, 8)
                .duration(320).EUt(GTValues.VA[GTValues.MV]).save(provider);

        // Transistor
        ASSEMBLER_RECIPES.recipeBuilder(TRANSISTOR.getId())
                .inputItems(plate, Silicon)
                .inputItems(wireFine, Tin, 6)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(TRANSISTOR, 8)
                .duration(160).EUt(GTValues.VA[GTValues.MV]).save(provider);

        // Diode
        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(DIODE.getId() + ".0"))
                .inputItems(wireFine, Copper, 4)
                .inputItems(dustSmall, GalliumArsenide)
                .inputFluids(Glass.getFluid(GTValues.L))
                .outputItems(DIODE)
                .duration(400).EUt(GTValues.VA[GTValues.LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(DIODE.getId() + ".1"))
                .inputItems(wireFine, AnnealedCopper, 4)
                .inputItems(dustSmall, GalliumArsenide)
                .inputFluids(Glass.getFluid(GTValues.L))
                .outputItems(DIODE, 2)
                .duration(400).EUt(GTValues.VA[GTValues.LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(DIODE.getId() + ".2"))
                .inputItems(wireFine, Copper, 4)
                .inputItems(dustSmall, GalliumArsenide)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(DIODE, 2)
                .duration(400).EUt(GTValues.VA[GTValues.LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(DIODE.getId() + ".3"))
                .inputItems(wireFine, Copper, 4)
                .inputItems(SILICON_WAFER)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(DIODE, 2)
                .duration(400).EUt(GTValues.VA[GTValues.LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(DIODE.getId() + ".4"))
                .inputItems(wireFine, AnnealedCopper, 4)
                .inputItems(dustSmall, GalliumArsenide)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(DIODE, 4)
                .duration(400).EUt(GTValues.VA[GTValues.LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(DIODE.getId() + ".5"))
                .inputItems(wireFine, AnnealedCopper, 4)
                .inputItems(SILICON_WAFER)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(DIODE, 4)
                .duration(400).EUt(GTValues.VA[GTValues.LV]).save(provider);

        // Inductor
        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(INDUCTOR.getId() + ".0"))
                .inputItems(ring, Steel)
                .inputItems(wireFine, Copper, 2)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 4))
                .outputItems(INDUCTOR, 2)
                .duration(320).EUt(GTValues.VA[GTValues.MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(INDUCTOR.getId() + ".1"))
                .inputItems(ring, Steel)
                .inputItems(wireFine, AnnealedCopper, 2)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 4))
                .outputItems(INDUCTOR, 4)
                .duration(320).EUt(GTValues.VA[GTValues.MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(INDUCTOR.getId() + ".2"))
                .inputItems(ring, NickelZincFerrite)
                .inputItems(wireFine, Copper, 2)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 4))
                .outputItems(INDUCTOR, 4)
                .duration(320).EUt(GTValues.VA[GTValues.MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(INDUCTOR.getId() + ".3"))
                .inputItems(ring, NickelZincFerrite)
                .inputItems(wireFine, AnnealedCopper, 2)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 4))
                .outputItems(INDUCTOR, 8)
                .duration(320).EUt(GTValues.VA[GTValues.MV]).save(provider);

        // SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder(SMD_RESISTOR.getId())
                .inputItems(dust, Carbon)
                .inputItems(wireFine, Electrum, 4)
                .inputFluids(Polyethylene.getFluid(GTValues.L * 2))
                .outputItems(SMD_RESISTOR, 16)
                .duration(160).EUt(GTValues.VA[GTValues.HV]).save(provider);

        // SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder(SMD_DIODE.getId())
                .inputItems(dust, GalliumArsenide)
                .inputItems(wireFine, Platinum, 8)
                .inputFluids(Polyethylene.getFluid(GTValues.L * 2))
                .outputItems(SMD_DIODE, 32)
                .duration(200).EUt(GTValues.VA[GTValues.HV]).save(provider);

        // SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder(SMD_TRANSISTOR.getId())
                .inputItems(foil, Gallium)
                .inputItems(wireFine, AnnealedCopper, 8)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(SMD_TRANSISTOR, 16)
                .duration(160).EUt(GTValues.VA[GTValues.HV]).save(provider);

        // SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(SMD_CAPACITOR.getId() + ".0"))
                .inputItems(foil, SiliconeRubber)
                .inputItems(foil, Aluminium)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 2))
                .outputItems(SMD_CAPACITOR, 8)
                .duration(80).EUt(GTValues.VA[GTValues.HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(SMD_CAPACITOR.getId() + ".1"))
                .inputItems(foil, PolyvinylChloride, 2)
                .inputItems(foil, Aluminium)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 2))
                .outputItems(SMD_CAPACITOR, 12)
                .duration(80).EUt(GTValues.VA[GTValues.HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(SMD_CAPACITOR.getId() + ".2"))
                .inputItems(foil, SiliconeRubber)
                .inputItems(foil, Tantalum)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 2))
                .outputItems(SMD_CAPACITOR, 16)
                .duration(120).EUt(GTValues.VA[GTValues.HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(SMD_CAPACITOR.getId() + ".3"))
                .inputItems(foil, PolyvinylChloride, 2)
                .inputItems(foil, Tantalum)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 2))
                .outputItems(SMD_CAPACITOR, 24)
                .duration(120).EUt(GTValues.VA[GTValues.HV]).save(provider);

        // SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder(SMD_INDUCTOR.getId())
                .inputItems(ring, NickelZincFerrite)
                .inputItems(wireFine, Cupronickel, 4)
                .inputFluids(Polyethylene.getFluid(GTValues.L))
                .outputItems(SMD_INDUCTOR, 16)
                .duration(160).EUt(GTValues.VA[GTValues.HV]).save(provider);

        // Advanced SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder(ADVANCED_SMD_RESISTOR.getId())
                .inputItems(dust, Graphene)
                .inputItems(wireFine, Platinum, 4)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L * 2))
                .outputItems(ADVANCED_SMD_RESISTOR, 16)
                .EUt(3840).duration(160).save(provider);

        // Advanced SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder(ADVANCED_SMD_DIODE.getId())
                .inputItems(dustSmall, IndiumGalliumPhosphide)
                .inputItems(wireFine, NiobiumTitanium, 4)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L / 2))
                .outputItems(ADVANCED_SMD_DIODE, 16)
                .EUt(3840).duration(150).save(provider);

        // Advanced SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder(ADVANCED_SMD_TRANSISTOR.getId())
                .inputItems(foil, VanadiumGallium)
                .inputItems(wireFine, HSSG, 8)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L))
                .outputItems(ADVANCED_SMD_TRANSISTOR, 16)
                .EUt(3840).duration(160).save(provider);

        // Advanced SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder(ADVANCED_SMD_CAPACITOR.getId())
                .inputItems(foil, Polybenzimidazole, 2)
                .inputItems(foil, HSSS)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L / 4))
                .outputItems(ADVANCED_SMD_CAPACITOR, 16)
                .EUt(3840).duration(80).save(provider);

        // Advanced SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder(ADVANCED_SMD_INDUCTOR.getId())
                .inputItems(ring, HSSE)
                .inputItems(wireFine, Palladium, 4)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L))
                .outputItems(ADVANCED_SMD_INDUCTOR, 16)
                .EUt(3840).duration(160).save(provider);

        // Carbon Fibers
        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(CARBON_FIBERS.getId() + ".0"))
                .inputItems(dust, Carbon, 4)
                .inputFluids(Polyethylene.getFluid(GTValues.L / 4))
                .outputItems(CARBON_FIBERS)
                .duration(37).EUt(GTValues.VA[GTValues.LV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(CARBON_FIBERS.getId() + ".1"))
                .inputItems(dust, Carbon, 4)
                .inputFluids(Polytetrafluoroethylene.getFluid(GTValues.L / 8))
                .outputItems(CARBON_FIBERS, 2)
                .duration(37).EUt(GTValues.VA[GTValues.MV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(CARBON_FIBERS.getId() + ".2"))
                .inputItems(dust, Carbon, 4)
                .inputFluids(Epoxy.getFluid(GTValues.L / 16))
                .outputItems(CARBON_FIBERS, 4)
                .duration(37).EUt(GTValues.VA[GTValues.HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(CARBON_FIBERS.getId() + ".3"))
                .inputItems(dust, Carbon, 8)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L / 16))
                .outputItems(CARBON_FIBERS, 16)
                .duration(37).EUt(GTValues.VA[GTValues.EV]).save(provider);

        // Crystal Circuit Components
        LASER_ENGRAVER_RECIPES.recipeBuilder("%s_%s".formatted(FormattingUtil.toLowerCaseUnder(craftingLens.name), Color.Lime.getName()))
                .inputItems(ENGRAVED_CRYSTAL_CHIP)
                .notConsumable(craftingLens, Color.Lime)
                .outputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(10000).save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder("%s_%s".formatted(FormattingUtil.toLowerCaseUnder(craftingLens.name), Color.Blue.getName()))
                .inputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .notConsumable(craftingLens, Color.Blue)
                .outputItems(CRYSTAL_SYSTEM_ON_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(40000).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(RAW_CRYSTAL_CHIP.getId() + ".0"))
                .inputItems(gemExquisite, Emerald)
                .inputFluids(Europium.getFluid(GTValues.L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(RAW_CRYSTAL_CHIP.getId() + ".1"))
                .inputItems(gemExquisite, Olivine)
                .inputFluids(Europium.getFluid(GTValues.L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder(RAW_CRYSTAL_CHIP_PART.getId())
                .inputItems(RAW_CRYSTAL_CHIP)
                .outputItems(RAW_CRYSTAL_CHIP_PART, 9)
                .EUt(GTValues.VA[GTValues.HV]).duration(100).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(RAW_CRYSTAL_CHIP.getId() + ".2"))
                .inputItems(RAW_CRYSTAL_CHIP_PART)
                .inputFluids(Europium.getFluid(GTValues.L / 9))
                .outputItems(RAW_CRYSTAL_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(GTValues.VA[GTValues.HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(RAW_CRYSTAL_CHIP.getId() + ".3"))
                .inputItems(RAW_CRYSTAL_CHIP_PART)
                .inputFluids(Mutagen.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(GTValues.VA[GTValues.HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(new ResourceLocation(RAW_CRYSTAL_CHIP.getId() + ".4"))
                .inputItems(RAW_CRYSTAL_CHIP_PART)
                .inputFluids(BacterialSludge.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP.asStack(), 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(GTValues.VA[GTValues.HV]).save(provider);

        BLAST_RECIPES.recipeBuilder(new ResourceLocation(ENGRAVED_CRYSTAL_CHIP.getId() + ".0"))
                .inputItems(plate, Emerald)
                .inputItems(RAW_CRYSTAL_CHIP)
                .inputFluids(Helium.getFluid(1000))
                .outputItems(ENGRAVED_CRYSTAL_CHIP)
                .blastFurnaceTemp(5000)
                .duration(900).EUt(GTValues.VA[GTValues.HV]).save(provider);

        BLAST_RECIPES.recipeBuilder(new ResourceLocation(ENGRAVED_CRYSTAL_CHIP.getId() + ".1"))
                .inputItems(plate, Olivine)
                .inputItems(RAW_CRYSTAL_CHIP)
                .inputFluids(Helium.getFluid(1000))
                .outputItems(ENGRAVED_CRYSTAL_CHIP)
                .blastFurnaceTemp(5000)
                .duration(900).EUt(GTValues.VA[GTValues.HV]).save(provider);

        // Quantum Parts
        CHEMICAL_BATH_RECIPES.recipeBuilder(QUANTUM_EYE.getId())
                .inputItems(gem, EnderEye)
                .inputFluids(Radon.getFluid(250))
                .outputItems(QUANTUM_EYE)
                .duration(480).EUt(GTValues.VA[GTValues.HV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder(QUANTUM_STAR.getId())
                .inputItems(gem, NetherStar)
                .inputFluids(Radon.getFluid(1250))
                .outputItems(QUANTUM_STAR)
                .duration(1920).EUt(GTValues.VA[GTValues.HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(GRAVI_STAR.getId())
                .inputItems(QUANTUM_STAR)
                .inputFluids(Neutronium.getFluid(GTValues.L * 2))
                .outputItems(GRAVI_STAR)
                .duration(480).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    private static void boardRecipes(Consumer<FinishedRecipe> provider) {

        // Coated Board
        VanillaRecipeHelper.addShapedRecipe(provider, "coated_board", COATED_BOARD.asStack(3),
                "RRR", "PPP", "RRR",
                'R', STICKY_RESIN.get(),
                'P', new UnificationEntry(plate, Wood));

        VanillaRecipeHelper.addShapelessRecipe(provider, "coated_board_1x", COATED_BOARD.asStack(),
                new UnificationEntry(plate, Wood),
                STICKY_RESIN.get(),
                STICKY_RESIN.get());

        VanillaRecipeHelper.addShapedRecipe(provider, "basic_circuit_board", BASIC_CIRCUIT_BOARD.asStack(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Copper),
                'B', COATED_BOARD.get());

        // Basic Circuit Board
        ASSEMBLER_RECIPES.recipeBuilder(BASIC_CIRCUIT_BOARD.getId())
                .inputItems(foil, Copper, 4)
                .inputItems(plate, Wood)
                .inputFluids(Glue.getFluid(100))
                .outputItems(BASIC_CIRCUIT_BOARD)
                .duration(200).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        // Phenolic Board
        ASSEMBLER_RECIPES.recipeBuilder(PHENOLIC_BOARD.getId())
                .inputItems(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .inputFluids(Glue.getFluid(50))
                .outputItems(PHENOLIC_BOARD)
                .duration(30).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        // Good Circuit Board
        VanillaRecipeHelper.addShapedRecipe(provider, "good_circuit_board", GOOD_CIRCUIT_BOARD.asStack(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Silver),
                'B', PHENOLIC_BOARD.get());

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(GOOD_CIRCUIT_BOARD.getId() + ".0")).EUt(GTValues.VA[GTValues.LV]).duration(300)
                .inputItems(foil, Silver, 4)
                .inputItems(PHENOLIC_BOARD)
                .inputFluids(SodiumPersulfate.getFluid(200))
                .outputItems(GOOD_CIRCUIT_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(GOOD_CIRCUIT_BOARD.getId() + ".1")).EUt(GTValues.VA[GTValues.LV]).duration(300)
                .inputItems(foil, Silver, 4)
                .inputItems(PHENOLIC_BOARD)
                .inputFluids(Iron3Chloride.getFluid(100))
                .outputItems(GOOD_CIRCUIT_BOARD)
                .save(provider);

        // Plastic Board
        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(PLASTIC_BOARD.getId() + ".0")).duration(500).EUt(10)
                .inputItems(plate, Polyethylene)
                .inputItems(foil, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(PLASTIC_BOARD.getId() + ".1")).duration(500).EUt(10)
                .inputItems(plate, PolyvinylChloride)
                .inputItems(foil, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD, 2)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(PLASTIC_BOARD.getId() + ".2")).duration(500).EUt(10)
                .inputItems(plate, Polytetrafluoroethylene)
                .inputItems(foil, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD, 4)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(PLASTIC_BOARD.getId() + ".3")).duration(500).EUt(10)
                .inputItems(plate, Polybenzimidazole)
                .inputItems(foil, Copper, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(PLASTIC_BOARD, 8)
                .save(provider);

        // Plastic Circuit Board
        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(PLASTIC_CIRCUIT_BOARD.getId() + ".0")).duration(600).EUt(GTValues.VA[GTValues.LV])
                .inputItems(PLASTIC_BOARD)
                .inputItems(foil, Copper, 6)
                .inputFluids(SodiumPersulfate.getFluid(500))
                .outputItems(PLASTIC_CIRCUIT_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(PLASTIC_CIRCUIT_BOARD.getId() + ".1")).duration(600).EUt(GTValues.VA[GTValues.LV])
                .inputItems(PLASTIC_BOARD)
                .inputItems(foil, Copper, 6)
                .inputFluids(Iron3Chloride.getFluid(250))
                .outputItems(PLASTIC_CIRCUIT_BOARD)
                .save(provider);

        // Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder(EPOXY_BOARD.getId()).duration(600).EUt(GTValues.VA[GTValues.LV])
                .inputItems(plate, Epoxy)
                .inputItems(foil, Gold, 8)
                .inputFluids(SulfuricAcid.getFluid(500))
                .outputItems(EPOXY_BOARD)
                .save(provider);

        // Advanced Circuit Board
        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(ADVANCED_CIRCUIT_BOARD.getId() + ".0")).duration(900).EUt(GTValues.VA[GTValues.LV])
                .inputItems(EPOXY_BOARD)
                .inputItems(foil, Electrum, 8)
                .inputFluids(SodiumPersulfate.getFluid(1000))
                .outputItems(ADVANCED_CIRCUIT_BOARD)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(ADVANCED_CIRCUIT_BOARD.getId() + ".1")).duration(900).EUt(GTValues.VA[GTValues.LV])
                .inputItems(EPOXY_BOARD)
                .inputItems(foil, Electrum, 8)
                .inputFluids(Iron3Chloride.getFluid(500))
                .outputItems(ADVANCED_CIRCUIT_BOARD)
                .save(provider);

        // Fiber Reinforced Epoxy Board
        CHEMICAL_BATH_RECIPES.recipeBuilder("%s_%s.0".formatted(FormattingUtil.toLowerCaseUnder(plate.name), ReinforcedEpoxyResin.getName())).duration(240).EUt(16)
                .inputItems(wireFine, BorosilicateGlass)
                .inputFluids(Epoxy.getFluid(GTValues.L))
                .outputItems(plate, ReinforcedEpoxyResin)
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("%s_%s.1".formatted(FormattingUtil.toLowerCaseUnder(plate.name), ReinforcedEpoxyResin.getName())).duration(240).EUt(16)
                .inputItems(CARBON_FIBERS)
                .inputFluids(Epoxy.getFluid(GTValues.L))
                .outputItems(plate, ReinforcedEpoxyResin)
                .save(provider);

        // Borosilicate Glass Recipes
        EXTRUDER_RECIPES.recipeBuilder("%s_%s".formatted(FormattingUtil.toLowerCaseUnder(wireFine.name), BorosilicateGlass.getName())).duration(160).EUt(96)
                .inputItems(ingot, BorosilicateGlass)
                .notConsumable(SHAPE_EXTRUDER_WIRE)
                .outputItems(wireFine, BorosilicateGlass, 8)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(FIBER_BOARD.getId()).duration(500).EUt(10)
                .inputItems(plate, ReinforcedEpoxyResin)
                .inputItems(foil, AnnealedCopper, 8)
                .inputFluids(SulfuricAcid.getFluid(125))
                .outputItems(FIBER_BOARD)
                .save(provider);

        // Extreme Circuit Board
        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(EXTREME_CIRCUIT_BOARD.getId() + ".0")).duration(1200).EUt(GTValues.VA[GTValues.LV])
                .inputItems(FIBER_BOARD)
                .inputItems(foil, AnnealedCopper, 12)
                .inputFluids(SodiumPersulfate.getFluid(2000))
                .outputItems(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(EXTREME_CIRCUIT_BOARD.getId() + ".1")).duration(1200).EUt(GTValues.VA[GTValues.LV])
                .inputItems(FIBER_BOARD)
                .inputItems(foil, AnnealedCopper, 12)
                .inputFluids(Iron3Chloride.getFluid(1000))
                .outputItems(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Multi-Layer Fiber Reinforced Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder(MULTILAYER_FIBER_BOARD.getId()).duration(500).EUt(GTValues.VA[GTValues.HV])
                .inputItems(FIBER_BOARD, 2)
                .inputItems(foil, Platinum, 8)
                .inputFluids(SulfuricAcid.getFluid(500))
                .outputItems(MULTILAYER_FIBER_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Elite Circuit Board
        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(ELITE_CIRCUIT_BOARD.getId() + ".0")).duration(1500).EUt(GTValues.VA[GTValues.MV])
                .inputItems(MULTILAYER_FIBER_BOARD)
                .inputItems(foil, Platinum, 8)
                .inputFluids(SodiumPersulfate.getFluid(4000))
                .outputItems(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(ELITE_CIRCUIT_BOARD.getId() + ".1")).duration(1500).EUt(GTValues.VA[GTValues.MV])
                .inputItems(MULTILAYER_FIBER_BOARD)
                .inputItems(foil, Platinum, 8)
                .inputFluids(Iron3Chloride.getFluid(2000))
                .outputItems(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Wetware Board

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder(new ResourceLocation(PETRI_DISH.getId() + ".0")).duration(160).EUt(GTValues.VA[GTValues.HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .inputFluids(Polytetrafluoroethylene.getFluid(GTValues.L / 4))
                .outputItems(PETRI_DISH)
                .save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder(new ResourceLocation(PETRI_DISH.getId() + ".1")).duration(40).EUt(GTValues.VA[GTValues.HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .inputFluids(Polybenzimidazole.getFluid(GTValues.L / 8))
                .outputItems(PETRI_DISH, 2)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(WETWARE_BOARD.getId()).duration(1200).EUt(GTValues.VA[GTValues.LuV])
                .inputItems(MULTILAYER_FIBER_BOARD, 16)
                .inputItems(PETRI_DISH)
                .inputItems(ELECTRIC_PUMP_LuV)
                .inputItems(SENSOR_IV)
                .inputItems(circuit, Tier.IV)
                .inputItems(foil, NiobiumTitanium, 16)
                .inputFluids(SterileGrowthMedium.getFluid(4000))
                .outputItems(WETWARE_BOARD, 16)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(WETWARE_CIRCUIT_BOARD.getId() + ".0")).duration(1800).EUt(GTValues.VA[GTValues.HV])
                .inputItems(WETWARE_BOARD)
                .inputItems(foil, NiobiumTitanium, 32)
                .inputFluids(SodiumPersulfate.getFluid(10000))
                .outputItems(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(new ResourceLocation(WETWARE_CIRCUIT_BOARD.getId() + ".1")).duration(1800).EUt(GTValues.VA[GTValues.HV])
                .inputItems(WETWARE_BOARD)
                .inputItems(foil, NiobiumTitanium, 32)
                .inputFluids(Iron3Chloride.getFluid(5000))
                .outputItems(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);
    }

    private static void circuitRecipes(Consumer<FinishedRecipe> provider) {

        // T1: Electronic ==============================================================================================

        // LV
        VanillaRecipeHelper.addShapedRecipe(provider, "electronic_circuit_lv", ELECTRONIC_CIRCUIT_LV.asStack(),
                "RPR", "VBV", "CCC",
                'R', RESISTOR.get(),
                'P', new UnificationEntry(plate, Steel),
                'V', VACUUM_TUBE.get(),
                'B', BASIC_CIRCUIT_BOARD.get(),
                'C', new UnificationEntry(cableGtSingle, RedAlloy));

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(ELECTRONIC_CIRCUIT_LV.getId()).EUt(16).duration(200)
                .inputItems(BASIC_CIRCUIT_BOARD)
                .inputItems(component, Component.Resistor, 2)
                .inputItems(wireGtSingle, RedAlloy, 2)
                .inputItems(circuit, Tier.ULV, 2)
                .outputItems(ELECTRONIC_CIRCUIT_LV, 2)
                .save(provider);

        // MV
        VanillaRecipeHelper.addShapedRecipe(provider, "electronic_circuit_mv", ELECTRONIC_CIRCUIT_MV.asStack(),
                "DPD", "CBC", "WCW",
                'W', new UnificationEntry(wireGtSingle, Copper),
                'P', new UnificationEntry(plate, Steel),
                'C', ELECTRONIC_CIRCUIT_LV.get(),
                'B', GOOD_CIRCUIT_BOARD.get(),
                'D', DIODE.get());

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(ELECTRONIC_CIRCUIT_MV.getId()).EUt(GTValues.VA[GTValues.LV]).duration(300)
                .inputItems(GOOD_CIRCUIT_BOARD)
                .inputItems(circuit, Tier.LV, 2)
                .inputItems(component, Component.Diode, 2)
                .inputItems(wireGtSingle, Copper, 2)
                .outputItems(ELECTRONIC_CIRCUIT_MV)
                .save(provider);

        // T2: Integrated ==============================================================================================

        // LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(INTEGRATED_CIRCUIT_LV.getId()).EUt(16).duration(200)
                .inputItems(BASIC_CIRCUIT_BOARD)
                .inputItems(INTEGRATED_LOGIC_CIRCUIT)
                .inputItems(component, Component.Resistor, 2)
                .inputItems(component, Component.Diode, 2)
                .inputItems(wireFine, Copper, 2)
                .inputItems(bolt, Tin, 2)
                .outputItems(INTEGRATED_CIRCUIT_LV, 2)
                .save(provider);

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(INTEGRATED_CIRCUIT_MV.getId()).EUt(24).duration(400)
                .inputItems(GOOD_CIRCUIT_BOARD)
                .inputItems(INTEGRATED_CIRCUIT_LV, 2)
                .inputItems(component, Component.Resistor, 2)
                .inputItems(component, Component.Diode, 2)
                .inputItems(wireFine, Gold, 4)
                .inputItems(bolt, Silver, 4)
                .outputItems(INTEGRATED_CIRCUIT_MV, 2)
                .save(provider);

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(INTEGRATED_CIRCUIT_HV.getId()).EUt(GTValues.VA[GTValues.LV]).duration(800)
                .inputItems(INTEGRATED_CIRCUIT_MV, 2)
                .inputItems(INTEGRATED_LOGIC_CIRCUIT, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 2)
                .inputItems(component, Component.Transistor, 4)
                .inputItems(wireFine, Electrum, 8)
                .inputItems(bolt, AnnealedCopper, 8)
                .outputItems(INTEGRATED_CIRCUIT_HV)
                .save(provider);

        // T2.5: Misc ==================================================================================================

        // NAND Chip ULV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NAND_CHIP_ULV.getId() + ".0")).EUt(GTValues.VA[GTValues.MV]).duration(300)
                .inputItems(GOOD_CIRCUIT_BOARD)
                .inputItems(SIMPLE_SYSTEM_ON_CHIP)
                .inputItems(bolt, RedAlloy, 2)
                .inputItems(wireFine, Tin, 2)
                .outputItems(NAND_CHIP_ULV, 8)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NAND_CHIP_ULV.getId() + ".1")).EUt(GTValues.VA[GTValues.MV]).duration(300)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(SIMPLE_SYSTEM_ON_CHIP)
                .inputItems(bolt, RedAlloy, 2)
                .inputItems(wireFine, Tin, 2)
                .outputItems(NAND_CHIP_ULV, 12)
                .save(provider);

        // Microprocessor LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(MICROPROCESSOR_LV.getId() + ".0")).EUt(60).duration(200)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(CENTRAL_PROCESSING_UNIT)
                .inputItems(component, Component.Resistor, 2)
                .inputItems(component, Component.Capacitor, 2)
                .inputItems(component, Component.Transistor, 2)
                .inputItems(wireFine, Copper, 2)
                .outputItems(MICROPROCESSOR_LV, 3)
                .save(provider);

        // Microprocessor LV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(MICROPROCESSOR_LV.getId() + ".1")).EUt(600).duration(50)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(SYSTEM_ON_CHIP)
                .inputItems(wireFine, Copper, 2)
                .inputItems(bolt, Tin, 2)
                .outputItems(MICROPROCESSOR_LV, 6)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T3: Processor ===============================================================================================

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(PROCESSOR_MV.getId() + ".0")).EUt(60).duration(200)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(CENTRAL_PROCESSING_UNIT)
                .inputItems(component, Component.Resistor, 4)
                .inputItems(component, Component.Capacitor, 4)
                .inputItems(component, Component.Transistor, 4)
                .inputItems(wireFine, RedAlloy, 4)
                .outputItems(PROCESSOR_MV, 2)
                .save(provider);

        // MV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(PROCESSOR_MV.getId() + ".1")).EUt(2400).duration(50)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(SYSTEM_ON_CHIP)
                .inputItems(wireFine, RedAlloy, 4)
                .inputItems(bolt, AnnealedCopper, 4)
                .outputItems(PROCESSOR_MV, 4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(PROCESSOR_ASSEMBLY_HV.getId()).EUt(GTValues.VA[GTValues.MV]).duration(400)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(PROCESSOR_MV, 2)
                .inputItems(component, Component.Inductor, 4)
                .inputItems(component, Component.Capacitor, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(wireFine, RedAlloy, 8)
                .outputItems(PROCESSOR_ASSEMBLY_HV, 2)
                .solderMultiplier(2)
                .save(provider);

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(WORKSTATION_EV.getId()).EUt(GTValues.VA[GTValues.MV]).duration(400)
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(PROCESSOR_ASSEMBLY_HV, 2)
                .inputItems(component, Component.Diode, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(wireFine, Electrum, 16)
                .inputItems(bolt, BlueAlloy, 16)
                .outputItems(WORKSTATION_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(MAINFRAME_IV.getId() + ".0")).EUt(GTValues.VA[GTValues.HV]).duration(800)
                .inputItems(frameGt, Aluminium, 2)
                .inputItems(WORKSTATION_EV, 2)
                .inputItems(component, Component.Inductor, 8)
                .inputItems(component, Component.Capacitor, 16)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireGtSingle, AnnealedCopper, 16)
                .outputItems(MAINFRAME_IV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(MAINFRAME_IV.getId() + ".1")).EUt(GTValues.VA[GTValues.HV]).duration(400)
                .inputItems(frameGt, Aluminium, 2)
                .inputItems(WORKSTATION_EV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireGtSingle, AnnealedCopper, 16)
                .outputItems(MAINFRAME_IV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T4: Nano ====================================================================================================

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_PROCESSOR_HV.getId() + ".0")).EUt(600).duration(200)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(SMD_RESISTOR, 8)
                .inputItems(SMD_CAPACITOR, 8)
                .inputItems(SMD_TRANSISTOR, 8)
                .inputItems(wireFine, Electrum, 8)
                .outputItems(NANO_PROCESSOR_HV, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_PROCESSOR_HV.getId() + ".1")).EUt(600).duration(100)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(ADVANCED_SMD_RESISTOR, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 2)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 2)
                .inputItems(wireFine, Electrum, 8)
                .outputItems(NANO_PROCESSOR_HV, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // HV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_PROCESSOR_HV.getId() + ".2")).EUt(9600).duration(50)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(ADVANCED_SYSTEM_ON_CHIP)
                .inputItems(wireFine, Electrum, 4)
                .inputItems(bolt, Platinum, 4)
                .outputItems(NANO_PROCESSOR_HV, 4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_PROCESSOR_ASSEMBLY_EV.getId() + ".0")).EUt(600).duration(400)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_HV, 2)
                .inputItems(SMD_INDUCTOR, 4)
                .inputItems(SMD_CAPACITOR, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 8)
                .inputItems(wireFine, Electrum, 16)
                .outputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_PROCESSOR_ASSEMBLY_EV.getId() + ".1")).EUt(600).duration(200)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_HV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR)
                .inputItems(ADVANCED_SMD_CAPACITOR, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 8)
                .inputItems(wireFine, Electrum, 16)
                .outputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_COMPUTER_IV.getId() + ".0")).EUt(600).duration(400)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .inputItems(SMD_DIODE, 8)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireFine, Electrum, 16)
                .outputItems(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_COMPUTER_IV.getId() + ".1")).EUt(600).duration(200)
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .inputItems(ADVANCED_SMD_DIODE, 2)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireFine, Electrum, 16)
                .outputItems(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_MAINFRAME_LUV.getId() + ".0")).EUt(GTValues.VA[GTValues.EV]).duration(800)
                .inputItems(frameGt, Aluminium, 2)
                .inputItems(NANO_COMPUTER_IV, 2)
                .inputItems(SMD_INDUCTOR, 16)
                .inputItems(SMD_CAPACITOR, 32)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireGtSingle, AnnealedCopper, 32)
                .outputItems(NANO_MAINFRAME_LUV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(NANO_MAINFRAME_LUV.getId() + ".1")).EUt(GTValues.VA[GTValues.EV]).duration(400)
                .inputItems(frameGt, Aluminium, 2)
                .inputItems(NANO_COMPUTER_IV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 4)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireGtSingle, AnnealedCopper, 32)
                .outputItems(NANO_MAINFRAME_LUV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T5: Quantum =================================================================================================

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_PROCESSOR_EV.getId() + ".0")).EUt(2400).duration(200)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUBIT_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(SMD_CAPACITOR, 12)
                .inputItems(SMD_TRANSISTOR, 12)
                .inputItems(wireFine, Platinum, 12)
                .outputItems(QUANTUM_PROCESSOR_EV, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_PROCESSOR_EV.getId() + ".1")).EUt(2400).duration(100)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUBIT_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(ADVANCED_SMD_CAPACITOR, 3)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 3)
                .inputItems(wireFine, Platinum, 12)
                .outputItems(QUANTUM_PROCESSOR_EV, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // EV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_PROCESSOR_EV.getId() + ".2")).EUt(38400).duration(50)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(ADVANCED_SYSTEM_ON_CHIP)
                .inputItems(wireFine, Platinum, 12)
                .inputItems(bolt, NiobiumTitanium, 8)
                .outputItems(QUANTUM_PROCESSOR_EV, 4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_ASSEMBLY_IV.getId() + ".0")).EUt(2400).duration(400)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_PROCESSOR_EV, 2)
                .inputItems(SMD_INDUCTOR, 8)
                .inputItems(SMD_CAPACITOR, 16)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(wireFine, Platinum, 16)
                .outputItems(QUANTUM_ASSEMBLY_IV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_ASSEMBLY_IV.getId() + ".1")).EUt(2400).duration(200)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_PROCESSOR_EV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(wireFine, Platinum, 16)
                .outputItems(QUANTUM_ASSEMBLY_IV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_COMPUTER_LUV.getId() + ".0")).EUt(2400).duration(400)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_ASSEMBLY_IV, 2)
                .inputItems(SMD_DIODE, 8)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireFine, Platinum, 32)
                .outputItems(QUANTUM_COMPUTER_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_COMPUTER_LUV.getId() + ".1")).EUt(2400).duration(200)
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(QUANTUM_ASSEMBLY_IV, 2)
                .inputItems(ADVANCED_SMD_DIODE, 2)
                .inputItems(NOR_MEMORY_CHIP, 4)
                .inputItems(RANDOM_ACCESS_MEMORY, 16)
                .inputItems(wireFine, Platinum, 32)
                .outputItems(QUANTUM_COMPUTER_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_MAINFRAME_ZPM.getId() + ".0")).EUt(GTValues.VA[GTValues.IV]).duration(800)
                .inputItems(frameGt, HSSG, 2)
                .inputItems(QUANTUM_COMPUTER_LUV, 2)
                .inputItems(SMD_INDUCTOR, 24)
                .inputItems(SMD_CAPACITOR, 48)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(wireGtSingle, AnnealedCopper, 48)
                .solderMultiplier(4)
                .outputItems(QUANTUM_MAINFRAME_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(QUANTUM_MAINFRAME_ZPM.getId() + ".1")).EUt(GTValues.VA[GTValues.IV]).duration(400)
                .inputItems(frameGt, HSSG, 2)
                .inputItems(QUANTUM_COMPUTER_LUV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 6)
                .inputItems(ADVANCED_SMD_CAPACITOR, 12)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(wireGtSingle, AnnealedCopper, 48)
                .solderMultiplier(4)
                .outputItems(QUANTUM_MAINFRAME_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // T6: Crystal =================================================================================================

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CRYSTAL_PROCESSOR_IV.getId() + ".0")).EUt(9600).duration(200)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .inputItems(ADVANCED_SMD_CAPACITOR, 6)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 6)
                .inputItems(wireFine, NiobiumTitanium, 8)
                .outputItems(CRYSTAL_PROCESSOR_IV, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // IV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CRYSTAL_PROCESSOR_IV.getId() + ".1")).EUt(86000).duration(100)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_SYSTEM_ON_CHIP)
                .inputItems(wireFine, NiobiumTitanium, 8)
                .inputItems(bolt, YttriumBariumCuprate, 8)
                .outputItems(CRYSTAL_PROCESSOR_IV, 4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CRYSTAL_ASSEMBLY_LUV.getId() + ".0")).EUt(9600).duration(400)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_PROCESSOR_IV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 4)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(wireFine, NiobiumTitanium, 16)
                .outputItems(CRYSTAL_ASSEMBLY_LUV, 2)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CRYSTAL_ASSEMBLY_LUV.getId() + ".1")).EUt(9600).duration(400)
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(CRYSTAL_ASSEMBLY_LUV, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(NOR_MEMORY_CHIP, 32)
                .inputItems(NAND_MEMORY_CHIP, 64)
                .inputItems(wireFine, NiobiumTitanium, 32)
                .solderMultiplier(2)
                .outputItems(CRYSTAL_COMPUTER_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder(CRYSTAL_MAINFRAME_UV.getId()).EUt(GTValues.VA[GTValues.LuV]).duration(800)
                .inputItems(frameGt, HSSE, 2)
                .inputItems(CRYSTAL_COMPUTER_ZPM, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 32)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(wireGtSingle, NiobiumTitanium, 8)
                .inputItems(ADVANCED_SMD_INDUCTOR, 8)
                .inputItems(ADVANCED_SMD_CAPACITOR, 16)
                .inputItems(ADVANCED_SMD_DIODE, 8)
                .inputFluids(SolderingAlloy.getFluid(GTValues.L * 10))
                .outputItems(CRYSTAL_MAINFRAME_UV)
                .save(provider);

        // T7: Wetware =================================================================================================

        // Neuro Processing Unit
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(NEURO_PROCESSOR.get()).EUt(80000).duration(600)
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(STEM_CELLS, 16)
                .inputItems(pipeSmallFluid, Polybenzimidazole, 8)
                .inputItems(plate, Electrum, 8)
                .inputItems(foil, SiliconeRubber, 16)
                .inputItems(bolt, HSSE, 8)
                .inputFluids(SterileGrowthMedium.getFluid(250))
                .outputItems(NEURO_PROCESSOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(WETWARE_PROCESSOR_LUV.getId() + ".0")).EUt(38400).duration(200)
                .inputItems(NEURO_PROCESSOR)
                .inputItems(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 8)
                .inputItems(wireFine, YttriumBariumCuprate, 8)
                .outputItems(WETWARE_PROCESSOR_LUV, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // SoC LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(WETWARE_PROCESSOR_LUV.getId() + ".1")).EUt(150000).duration(100)
                .inputItems(NEURO_PROCESSOR)
                .inputItems(HIGHLY_ADVANCED_SOC)
                .inputItems(wireFine, YttriumBariumCuprate, 8)
                .inputItems(bolt, Naquadah, 8)
                .outputItems(WETWARE_PROCESSOR_LUV, 4)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(WETWARE_PROCESSOR_ASSEMBLY_ZPM.getId()).EUt(38400).duration(400)
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(WETWARE_PROCESSOR_LUV, 2)
                .inputItems(ADVANCED_SMD_INDUCTOR, 6)
                .inputItems(ADVANCED_SMD_CAPACITOR, 12)
                .inputItems(RANDOM_ACCESS_MEMORY, 24)
                .inputItems(wireFine, YttriumBariumCuprate, 16)
                .solderMultiplier(2)
                .outputItems(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder(WETWARE_SUPER_COMPUTER_UV.getId()).EUt(38400).duration(400)
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .inputItems(ADVANCED_SMD_DIODE, 8)
                .inputItems(NOR_MEMORY_CHIP, 16)
                .inputItems(RANDOM_ACCESS_MEMORY, 32)
                .inputItems(wireFine, YttriumBariumCuprate, 24)
                .inputItems(foil, Polybenzimidazole, 32)
                .inputItems(plate, Europium, 4)
                .inputFluids(SolderingAlloy.getFluid(1152))
                .outputItems(WETWARE_SUPER_COMPUTER_UV)
                .save(provider);

        // UHV
        //TODO do we really need UHV+
//        ASSEMBLY_LINE_RECIPES.recipeBuilder()
//                .inputItems(frameGt, Tritanium, 2)
//                .inputItems(WETWARE_SUPER_COMPUTER_UV, 2)
//                .inputItems(ADVANCED_SMD_DIODE, 32)
//                .inputItems(ADVANCED_SMD_CAPACITOR, 32)
//                .inputItems(ADVANCED_SMD_TRANSISTOR, 32)
//                .inputItems(ADVANCED_SMD_RESISTOR, 32)
//                .inputItems(ADVANCED_SMD_INDUCTOR, 32)
//                .inputItems(foil, Polybenzimidazole, 64)
//                .inputItems(RANDOM_ACCESS_MEMORY, 32)
//                .inputItems(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 16)
//                .inputItems(plate, Europium, 8)
//                .inputFluids(SolderingAlloy.getFluid(L * 20))
//                .inputFluids(Polybenzimidazole.getFluid(L * 8))
//                .outputItems(WETWARE_MAINFRAME_UHV)
//                .EUt(300000).duration(2000).save(provider);

        // Misc ========================================================================================================

        // Data Stick
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(TOOL_DATA_STICK.getId())
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(CENTRAL_PROCESSING_UNIT, 2)
                .inputItems(NAND_MEMORY_CHIP, 32)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(wireFine, RedAlloy, 16)
                .inputItems(plate, Polyethylene, 4)
                .outputItems(TOOL_DATA_STICK)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(90).save(provider);

        // Data Orb
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(TOOL_DATA_ORB.getId())
                .inputItems(ADVANCED_CIRCUIT_BOARD)
                .inputItems(circuit, Tier.HV, 2)
                .inputItems(RANDOM_ACCESS_MEMORY, 4)
                .inputItems(NOR_MEMORY_CHIP, 32)
                .inputItems(NAND_MEMORY_CHIP, 64)
                .inputItems(wireFine, Platinum, 32)
                .outputItems(TOOL_DATA_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(1200).save(provider);
    }
}
