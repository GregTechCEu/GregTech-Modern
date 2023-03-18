package com.lowdragmc.gtceu.data.recipe.chemistry;


import com.lowdragmc.gtceu.api.machine.multiblock.CleanroomType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.api.GTValues.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.dust;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.dustTiny;
import static com.lowdragmc.gtceu.common.libs.GTItems.*;
import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
public class GrowthMediumRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Bio Chaff
        MACERATOR_RECIPES.recipeBuilder(PLANT_BALL.getId()).duration(200)
                .inputItems(PLANT_BALL.get(), 2)
                .outputItems(BIO_CHAFF.asStack())
                .outputItems(BIO_CHAFF.asStack())
                .chancedOutput(BIO_CHAFF.asStack(), 5000, 0)
                .chancedOutput(BIO_CHAFF.asStack(), 2500, 0)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(BIO_CHAFF.getId()).duration(300)
                .inputItems(BIO_CHAFF.asStack())
                .outputItems(new ItemStack(Blocks.DIRT))
                .save(provider);

        // Bacteria
        BREWING_RECIPES.recipeBuilder(BIO_CHAFF.getId()).EUt(VA[HV]).duration(300)
                .inputItems(BIO_CHAFF.get(), 4)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputFluids(Bacteria.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Bacterial Sludge
        CHEMICAL_RECIPES.recipeBuilder(BacterialSludge.getName()).EUt(VA[EV]).duration(600)
                .inputFluids(Biomass.getFluid(1000))
                .inputFluids(Bacteria.getFluid(1000))
                .outputFluids(BacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Enriched Bacterial Sludge
        BREWING_RECIPES.recipeBuilder(EnrichedBacterialSludge.getName() + ".0").EUt(4).duration(128)
                .inputItems(dust, Uranium238)
                .inputFluids(BacterialSludge.getFluid(1000))
                .outputFluids(EnrichedBacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        BREWING_RECIPES.recipeBuilder(EnrichedBacterialSludge.getName() + ".1").EUt(4).duration(128)
                .inputItems(dustTiny, Uranium235)
                .inputFluids(BacterialSludge.getFluid(1000))
                .outputFluids(EnrichedBacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        BREWING_RECIPES.recipeBuilder(EnrichedBacterialSludge.getName() + ".3").EUt(4).duration(128)
                .inputItems(dustTiny, Naquadria)
                .inputFluids(BacterialSludge.getFluid(1000))
                .outputFluids(EnrichedBacterialSludge.getFluid(2000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Mutagen
        DISTILLERY_RECIPES.recipeBuilder(Mutagen.getName()).EUt(VA[IV]).duration(100)
                .inputFluids(EnrichedBacterialSludge.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(Mutagen.getFluid(100))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Collagen
        CHEMICAL_RECIPES.recipeBuilder("dust_collagen.0").EUt(VA[HV]).duration(800)
                .inputItems(dust, Meat)
                .inputItems(Items.BONE_MEAL.getDefaultInstance())
                .inputFluids(SulfuricAcid.getFluid(500))
                .outputItems(dust, Collagen)
                .outputFluids(DilutedSulfuricAcid.getFluid(500))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_collagen.1").EUt(VA[HV]).duration(1600)
                .inputItems(dust, Meat, 2)
                .inputItems(new ItemStack(Items.BONE))
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, Collagen, 2)
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Gelatin
        MIXER_RECIPES.recipeBuilder(GelatinMixture.getName()).EUt(VA[HV]).duration(1600)
                .inputItems(dust, Collagen, 4)
                .inputFluids(PhosphoricAcid.getFluid(1000))
                .inputFluids(Water.getFluid(3000))
                .outputFluids(GelatinMixture.getFluid(4000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("dust_gelatin").EUt(VA[HV]).duration(2400)
                .inputFluids(GelatinMixture.getFluid(6000))
                .outputItems(dust, Phosphorus)
                .outputItems(dust, Gelatin, 4)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Agar
        AUTOCLAVE_RECIPES.recipeBuilder("dust_agar").EUt(VA[HV]).duration(600)
                .inputItems(dust, Gelatin)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputItems(dust, Agar)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Raw Growth Medium
        MIXER_RECIPES.recipeBuilder(RawGrowthMedium.getName()).EUt(VA[IV]).duration(1200)
                .inputItems(dust, Meat, 4)
                .inputItems(dust, Salt, 4)
                .inputItems(dust, Calcium, 4)
                .inputItems(dust, Agar, 4)
                .inputFluids(Mutagen.getFluid(4000))
                .outputFluids(RawGrowthMedium.getFluid(4000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Sterile Growth Medium
        FLUID_HEATER_RECIPES.recipeBuilder(SterileGrowthMedium.getName()).EUt(VA[IV]).duration(20)
                .circuitMeta(1)
                .inputFluids(RawGrowthMedium.getFluid(100))
                .outputFluids(SterileGrowthMedium.getFluid(100))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Stem Cells
        CHEMICAL_RECIPES.recipeBuilder("stem_cells").EUt(VA[LuV]).duration(300)
                .inputItems(dust, Osmiridium)
                .inputFluids(Bacteria.getFluid(500))
                .inputFluids(SterileGrowthMedium.getFluid(500))
                .outputItems(STEM_CELLS.get(), 32)
                .outputFluids(BacterialSludge.getFluid(500))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);
    }
}
