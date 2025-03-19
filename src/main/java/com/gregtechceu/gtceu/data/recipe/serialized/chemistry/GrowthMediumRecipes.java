package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class GrowthMediumRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Bio Chaff
        MACERATOR_RECIPES.recipeBuilder("bio_chaff").EUt(4).duration(200)
                .inputItems(PLANT_BALL, 2)
                .outputItems(BIO_CHAFF)
                .outputItems(BIO_CHAFF)
                .chancedOutput(BIO_CHAFF.asStack(), 5000, 0)
                .chancedOutput(BIO_CHAFF.asStack(), 2500, 0)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("dirt_from_bio_chaff").EUt(4).duration(300)
                .inputItems(BIO_CHAFF)
                .outputItems(Blocks.DIRT.asItem())
                .save(provider);

        // Bacteria
        BREWING_RECIPES.recipeBuilder("bacteria").EUt(VA[HV]).duration(300)
                .inputItems(BIO_CHAFF, 4)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputFluids(Bacteria.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Bacterial Sludge
        CHEMICAL_RECIPES.recipeBuilder("bacterial_sludge").EUt(VA[EV]).duration(600)
                .inputFluids(Biomass.getFluid(1000))
                .inputFluids(Bacteria.getFluid(1000))
                .outputFluids(BacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Enriched Bacterial Sludge
        BREWING_RECIPES.recipeBuilder("enriched_bacterial_sludge_from_u238").EUt(4).duration(128)
                .inputItems(dust, Uranium238)
                .inputFluids(BacterialSludge.getFluid(1000))
                .outputFluids(EnrichedBacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        BREWING_RECIPES.recipeBuilder("enriched_bacterial_sludge_from_u235").EUt(4).duration(128)
                .inputItems(dustTiny, Uranium235)
                .inputFluids(BacterialSludge.getFluid(1000))
                .outputFluids(EnrichedBacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        BREWING_RECIPES.recipeBuilder("enriched_bacterial_sludge_from_naquadria").EUt(4).duration(128)
                .inputItems(dustTiny, Naquadria)
                .inputFluids(BacterialSludge.getFluid(1000))
                .outputFluids(EnrichedBacterialSludge.getFluid(2000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Mutagen
        DISTILLERY_RECIPES.recipeBuilder("mutagen").EUt(VA[IV]).duration(100)
                .inputFluids(EnrichedBacterialSludge.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(Mutagen.getFluid(100))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Collagen
        CHEMICAL_RECIPES.recipeBuilder("collagen_from_bone_meal").EUt(VA[HV]).duration(800)
                .inputItems(dust, Meat)
                .inputItems(Items.BONE_MEAL)
                .inputFluids(SulfuricAcid.getFluid(500))
                .outputItems(dust, Collagen)
                .outputFluids(DilutedSulfuricAcid.getFluid(500))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("collagen_from_bone").EUt(VA[HV]).duration(1600)
                .inputItems(dust, Meat, 2)
                .inputItems(Items.BONE)
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, Collagen, 2)
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Gelatin
        MIXER_RECIPES.recipeBuilder("gelatin_mixture").EUt(VA[HV]).duration(1600)
                .inputItems(dust, Collagen, 4)
                .inputFluids(PhosphoricAcid.getFluid(1000))
                .inputFluids(Water.getFluid(3000))
                .outputFluids(GelatinMixture.getFluid(4000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("gelatin").EUt(VA[HV]).duration(2400)
                .inputFluids(GelatinMixture.getFluid(6000))
                .outputItems(dust, Phosphorus)
                .outputItems(dust, Gelatin, 4)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Agar
        AUTOCLAVE_RECIPES.recipeBuilder("agar").EUt(VA[HV]).duration(600)
                .inputItems(dust, Gelatin)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputItems(dust, Agar)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Raw Growth Medium
        MIXER_RECIPES.recipeBuilder("raw_growth_medium").EUt(VA[IV]).duration(1200)
                .inputItems(dust, Meat, 4)
                .inputItems(dust, Salt, 4)
                .inputItems(dust, Calcium, 4)
                .inputItems(dust, Agar, 4)
                .inputFluids(Mutagen.getFluid(4000))
                .outputFluids(RawGrowthMedium.getFluid(4000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);

        // Sterile Growth Medium
        FLUID_HEATER_RECIPES.recipeBuilder("sterile_growth_medium").EUt(VA[IV]).duration(20)
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
                .outputItems(STEM_CELLS, 32)
                .outputFluids(BacterialSludge.getFluid(500))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .save(provider);
    }
}
