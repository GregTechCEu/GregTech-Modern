package com.lowdragmc.gtceu.data.recipe.chemistry;

import com.lowdragmc.gtceu.common.recipe.DimensionCondition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
import static com.lowdragmc.gtceu.api.GTValues.*;

public class ChemistryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // A Few Random Recipes
        FLUID_HEATER_RECIPES.recipeBuilder(Ethenone.getName())
                .circuitMeta(1)
                .inputFluids(Acetone.getFluid(100))
                .outputFluids(Ethenone.getFluid(100))
                .duration(16).EUt(VA[LV]).save(provider);

        FLUID_HEATER_RECIPES.recipeBuilder(Acetone.getName())
                .circuitMeta(1)
                .inputFluids(DissolvedCalciumAcetate.getFluid(200))
                .outputFluids(Acetone.getFluid(200))
                .duration(16).EUt(VA[LV]).save(provider);

        VACUUM_RECIPES.recipeBuilder(Ice.getName())
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Ice.getFluid(1000))
                .duration(50).EUt(VA[LV]).save(provider);

        VACUUM_RECIPES.recipeBuilder(LiquidAir.getName())
                .inputFluids(Air.getFluid(4000))
                .outputFluids(LiquidAir.getFluid(4000))
                .duration(80).EUt(VA[HV]).save(provider);

        VACUUM_RECIPES.recipeBuilder(LiquidNetherAir.getName())
                .inputFluids(NetherAir.getFluid(4000))
                .outputFluids(LiquidNetherAir.getFluid(4000))
                .duration(80).EUt(VA[EV]).save(provider);

        VACUUM_RECIPES.recipeBuilder(LiquidEnderAir.getName())
                .inputFluids(EnderAir.getFluid(4000))
                .outputFluids(LiquidEnderAir.getFluid(4000))
                .duration(80).EUt(VA[IV]).save(provider);

        VACUUM_RECIPES.recipeBuilder(LiquidOxygen.getName())
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(LiquidOxygen.getFluid(1000))
                .duration(240).EUt(VA[EV]).save(provider);

        VACUUM_RECIPES.recipeBuilder(LiquidHelium.getName())
                .inputFluids(Helium.getFluid(1000))
                .outputFluids(LiquidHelium.getFluid(1000))
                .duration(240).EUt(VA[EV]).save(provider);

        BLAST_RECIPES.recipeBuilder("ingot_nickel_zinc_ferrite")
                .inputItems(dust, FerriteMixture)
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(ingot, NickelZincFerrite)
                .blastFurnaceTemp(1500)
                .duration(400).EUt(VA[MV]).save(provider);

        FERMENTING_RECIPES.recipeBuilder(FermentedBiomass.getName())
                .inputFluids(Biomass.getFluid(100))
                .outputFluids(FermentedBiomass.getFluid(100))
                .duration(150).EUt(2).save(provider);

        WIREMILL_RECIPES.recipeBuilder("string")
                .inputItems(ingot, Polycaprolactam)
                .outputItems(Items.STRING, 32)
                .duration(80).EUt(48).save(provider);

        GAS_COLLECTOR_RECIPES.recipeBuilder(Air.getName())
                .circuitMeta(1)
                .outputFluids(Air.getFluid(10000))
                .addCondition(new DimensionCondition(new ResourceLocation("overworld")))
                .duration(200).EUt(16).save(provider);

        GAS_COLLECTOR_RECIPES.recipeBuilder(NetherAir.getName())
                .circuitMeta(2)
                .outputFluids(NetherAir.getFluid(10000))
                .addCondition(new DimensionCondition(new ResourceLocation("nether")))
                .duration(200).EUt(64).save(provider);

        GAS_COLLECTOR_RECIPES.recipeBuilder(EnderAir.getName())
                .circuitMeta(3)
                .outputFluids(EnderAir.getFluid(10000))
                .addCondition(new DimensionCondition(new ResourceLocation("end")))
                .duration(200).EUt(256).save(provider);

        // CaCO3 + 2NaCl -> Na2CO3 + CaCl2
        BLAST_RECIPES.recipeBuilder("dust_soda_ash_dust_calcium_chloride")
                .inputItems(dust, Calcite, 5)
                .inputItems(dust, Salt, 4)
                .outputItems(dust, SodaAsh, 6)
                .outputItems(dust, CalciumChloride, 3)
                .duration(120).EUt(VA[MV]).blastFurnaceTemp(1500)
                .save(provider);

        // 2NaOH + CO2 -> Na2CO3 + H20
        CHEMICAL_RECIPES.recipeBuilder("dust_soda_ash_water")
                .inputItems(dust, SodiumHydroxide, 6)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputItems(dust, SodaAsh, 6)
                .outputFluids(Water.getFluid(1000))
                .duration(80).EUt(VA[HV])
                .save(provider);
    }
}
