package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PlatGroupMetalsRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Primary Chain

        // Platinum Group Sludge Production
        CHEMICAL_RECIPES.recipeBuilder("pgs_from_chalcopyrite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Chalcopyrite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 2)
                .outputFluids(SulfuricCopperSolution.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pgs_from_chalcocite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Chalcocite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 2)
                .outputFluids(SulfuricCopperSolution.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pgs_from_bornite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Bornite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 2)
                .outputFluids(SulfuricCopperSolution.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pgs_from_tetrahedrite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Tetrahedrite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 2)
                .outputFluids(SulfuricCopperSolution.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pgs_from_pentlandite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Pentlandite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 2)
                .outputFluids(SulfuricNickelSolution.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pgs_from_cooperite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Cooperite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 4)
                .outputFluids(SulfuricNickelSolution.getFluid(1000))
                .save(provider);

        // Aqua Regia
        // HNO3 + HCl -> [HNO3 + HCl]
        MIXER_RECIPES.recipeBuilder("aqua_regia").duration(30).EUt(VA[LV])
                .inputFluids(NitricAcid.getFluid(1000))
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(AquaRegia.getFluid(3000))
                .save(provider);

        // Platinum Group Sludge Break-Down
        //
        // MODIFY THIS RECIPE TO RE-BALANCE THE LINE
        //
        // Current Losses of Materials per recipe (update this if rebalanced):
        // H: Loses
        // N: Loses
        // O: Loses
        // Cl: Perfectly Conserved
        //
        // If modified, this is how much 1 of each product will change the above losses by:
        // Pt: 266L of Cl
        //
        // These numbers are not correct:
        // Pd: 200L of N, 600L of H
        // Ru/Rh: 667L of O
        // Ir/Os: 620L of O, 100L of H
        //
        // Can also modify the PtCl2 electrolyzer recipe to keep a perfect Cl ratio.
        //
        CENTRIFUGE_RECIPES.recipeBuilder("pgs_separation").duration(500).EUt(VA[HV])
                .inputItems(dust, PlatinumGroupSludge, 6)
                .inputFluids(AquaRegia.getFluid(1200))
                .outputItems(dust, PlatinumRaw, 3) // PtCl2
                .outputItems(dust, PalladiumRaw, 3) // PdNH3
                .outputItems(dust, InertMetalMixture, 2) // RhRuO4
                .outputItems(dust, RarestMetalMixture) // IrOsO4(H2O)
                .outputItems(dust, PlatinumSludgeResidue, 2)
                .save(provider);

        // PLATINUM

        ELECTROLYZER_RECIPES.recipeBuilder("raw_platinum_separation").duration(100).EUt(VA[MV])
                .inputItems(dust, PlatinumRaw, 3)
                .outputItems(dust, Platinum)
                .outputFluids(Chlorine.getFluid(800))
                .save(provider);

        // PALLADIUM

        CHEMICAL_RECIPES.recipeBuilder("raw_palladium_separation").duration(200).EUt(VA[MV])
                .inputItems(dust, PalladiumRaw, 5)
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .outputItems(dust, Palladium)
                .outputItems(dust, AmmoniumChloride, 2)
                .save(provider);

        // RHODIUM / RUTHENIUM

        CHEMICAL_RECIPES.recipeBuilder("inert_metal_mixture_separation").duration(450).EUt(VA[EV])
                .inputItems(dust, InertMetalMixture, 6)
                .inputFluids(SulfuricAcid.getFluid(1500))
                .outputFluids(RhodiumSulfate.getFluid(500))
                .outputItems(dust, RutheniumTetroxide, 5)
                .outputFluids(Hydrogen.getFluid(3000))
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("rhodium_sulfate_separation").duration(100).EUt(VA[MV])
                .inputFluids(RhodiumSulfate.getFluid(1000))
                .outputItems(dust, Rhodium, 2)
                .outputFluids(SulfurTrioxide.getFluid(3000))
                .outputFluids(Oxygen.getFluid(3000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ruthenium_tetroxide_separation").duration(200).EUt(VA[MV])
                .inputItems(dust, RutheniumTetroxide, 5)
                .inputItems(dust, Carbon, 2)
                .outputItems(dust, Ruthenium)
                .outputFluids(CarbonDioxide.getFluid(2000))
                .save(provider);

        // OSMIUM / IRIDIUM

        LARGE_CHEMICAL_RECIPES.recipeBuilder("rarest_metal_mixture_separation").duration(400).EUt(VA[IV])
                .inputItems(dust, RarestMetalMixture, 7)
                .inputFluids(HydrochloricAcid.getFluid(4000))
                .outputItems(dust, IridiumMetalResidue, 5)
                .outputFluids(AcidicOsmiumSolution.getFluid(2000))
                .outputFluids(Hydrogen.getFluid(3000))
                .save(provider);

        DISTILLATION_RECIPES.recipeBuilder("acidic_osmium_solution_separation").duration(400).EUt(VA[MV])
                .inputFluids(AcidicOsmiumSolution.getFluid(2000))
                .outputItems(dust, OsmiumTetroxide, 5)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("osmium_tetroxide_separation").duration(200).EUt(VA[LV])
                .inputItems(dust, OsmiumTetroxide, 5)
                .inputFluids(Hydrogen.getFluid(8000))
                .outputItems(dust, Osmium)
                .outputFluids(Water.getFluid(4000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("iridium_metal_residue_separation").duration(200).EUt(VA[MV])
                .inputItems(dust, IridiumMetalResidue, 5)
                .outputItems(dust, IridiumChloride, 4)
                .outputItems(dust, PlatinumSludgeResidue)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("iridium_chloride_separation").duration(100).EUt(VA[LV])
                .inputItems(dust, IridiumChloride, 4)
                .inputFluids(Hydrogen.getFluid(3000))
                .outputItems(dust, Iridium)
                .outputFluids(HydrochloricAcid.getFluid(3000))
                .save(provider);
    }
}
