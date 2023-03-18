package com.lowdragmc.gtceu.data.recipe.misc;

import com.lowdragmc.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.GTValues.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote SteamRecipes
 */
public class FuelRecipes {
    public static void init(Consumer<FinishedRecipe> provider) {
        
        STEAM_BOILER_RECIPES.recipeBuilder("lava")
                .inputFluids(FluidStack.create(Fluids.LAVA, 100))
                .duration(600 * 12) // why we should mul 12?????
                .save(provider);

        //semi-fluid fuels, like creosote
        LARGE_BOILER_RECIPES.recipeBuilder(Creosote.getName())
                .inputFluids(Creosote.getFluid(160))
                .duration(10)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder(Biomass.getName())
                .inputFluids(Biomass.getFluid(40))
                .duration(10)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder(Oil.getName())
                .inputFluids(Oil.getFluid(200))
                .duration(10)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder(OilHeavy.getName())
                .inputFluids(OilHeavy.getFluid(32))
                .duration(10)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder(SulfuricHeavyFuel.getName())
                .inputFluids(SulfuricHeavyFuel.getFluid(32))
                .duration(10)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder(HeavyFuel.getName())
                .inputFluids(HeavyFuel.getFluid(16))
                .duration(30)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder(FishOil.getName())
                .inputFluids(FishOil.getFluid(160))
                .duration(10)
                .save(provider);
        
        //diesel generator fuels
        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Naphtha.getName())
                .inputFluids(Naphtha.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(SulfuricLightFuel.getName())
                .inputFluids(SulfuricLightFuel.getFluid(4))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Methanol.getName())
                .inputFluids(Methanol.getFluid(4))
                .duration(8)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Ethanol.getName())
                .inputFluids(Ethanol.getFluid(1))
                .duration(6)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Octane.getName())
                .inputFluids(Octane.getFluid(2))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(BioDiesel.getName())
                .inputFluids(BioDiesel.getFluid(1))
                .duration(8)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(LightFuel.getName())
                .inputFluids(LightFuel.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Diesel.getName())
                .inputFluids(Diesel.getFluid(1))
                .duration(15)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(CetaneBoostedDiesel.getName())
                .inputFluids(CetaneBoostedDiesel.getFluid(2))
                .duration(45)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(RocketFuel.getName())
                .inputFluids(RocketFuel.getFluid(16))
                .duration(125)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Gasoline.getName())
                .inputFluids(Gasoline.getFluid(1))
                .duration(50)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(HighOctaneGasoline.getName())
                .inputFluids(HighOctaneGasoline.getFluid(1))
                .duration(100)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(Toluene.getName())
                .inputFluids(Toluene.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(OilLight.getName())
                .inputFluids(OilLight.getFluid(32))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder(RawOil.getName())
                .inputFluids(RawOil.getFluid(64))
                .duration(15)
                .EUt(-V[LV])
                .save(provider);

        //steam generator fuels
        STEAM_TURBINE_FUELS.recipeBuilder(Steam.getName())
                .inputFluids(Steam.getFluid(640))
                .outputFluids(DistilledWater.getFluid(4))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        //gas turbine fuels
        GAS_TURBINE_FUELS.recipeBuilder(NaturalGas.getName())
                .inputFluids(NaturalGas.getFluid(8))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(WoodGas.getName())
                .inputFluids(WoodGas.getFluid(8))
                .duration(6)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(SulfuricGas.getName())
                .inputFluids(SulfuricGas.getFluid(32))
                .duration(25)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(SulfuricNaphtha.getName())
                .inputFluids(SulfuricNaphtha.getFluid(4))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(CoalGas.getName())
                .inputFluids(CoalGas.getFluid(1))
                .duration(3)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Methane.getName())
                .inputFluids(Methane.getFluid(2))
                .duration(7)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Ethylene.getName())
                .inputFluids(Ethylene.getFluid(1))
                .duration(4)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(RefineryGas.getName())
                .inputFluids(RefineryGas.getFluid(1))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Ethane.getName())
                .inputFluids(Ethane.getFluid(4))
                .duration(21)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Propene.getName())
                .inputFluids(Propene.getFluid(1))
                .duration(6)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Butadiene.getName())
                .inputFluids(Butadiene.getFluid(16))
                .duration(102)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Propane.getName())
                .inputFluids(Propane.getFluid(4))
                .duration(29)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Butene.getName())
                .inputFluids(Butene.getFluid(1))
                .duration(8)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Phenol.getName())
                .inputFluids(Phenol.getFluid(1))
                .duration(9)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Benzene.getName())
                .inputFluids(Benzene.getFluid(1))
                .duration(11)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Butane.getName())
                .inputFluids(Butane.getFluid(4))
                .duration(37)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(LPG.getName())
                .inputFluids(LPG.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder(Nitrobenzene.getName()) // TODO Too OP pls nerf
                .inputFluids(Nitrobenzene.getFluid(1))
                .duration(40)
                .EUt(-V[LV])
                .save(provider);

        //plasma turbine
//        PLASMA_GENERATOR_FUELS.recipeBuilder()
//                .inputFluids(Helium.getPlasma(1))
//                .outputFluids(Helium.getFluid(1))
//                .duration(40)
//                .EUt(V[EV])
//                .save(provider);
//
//        PLASMA_GENERATOR_FUELS.recipeBuilder()
//                .inputFluids(Oxygen.getPlasma(1))
//                .outputFluids(Oxygen.getFluid(1))
//                .duration(48)
//                .EUt(V[EV])
//                .save(provider);
//
//        PLASMA_GENERATOR_FUELS.recipeBuilder()
//                .inputFluids(Nitrogen.getPlasma(1))
//                .outputFluids(Nitrogen.getFluid(1))
//                .duration(64)
//                .EUt(V[EV])
//                .save(provider);
//
//        PLASMA_GENERATOR_FUELS.recipeBuilder()
//                .inputFluids(Iron.getPlasma(1))
//                .outputFluids(Iron.getFluid(1))
//                .duration(96)
//                .EUt(V[EV])
//                .save(provider);
//
//        PLASMA_GENERATOR_FUELS.recipeBuilder()
//                .inputFluids(Nickel.getPlasma(1))
//                .outputFluids(Nickel.getFluid(1))
//                .duration(192)
//                .EUt(V[EV])
//                .save(provider);
    }

    public static void initFuel(Consumer<FinishedRecipe> provider) {
        for (Item item : Registry.ITEM) {
            var burnTime = GTUtil.getItemBurnTime(item);
            if (burnTime > 0) {
                STEAM_BOILER_RECIPES.recipeBuilder(item.getDescriptionId())
                        .inputItems(item)
                        .duration(burnTime * 12)
                        .save(provider);
            }
        }
    }
}
