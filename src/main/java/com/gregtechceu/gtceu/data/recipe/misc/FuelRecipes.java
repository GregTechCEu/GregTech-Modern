package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class FuelRecipes {

    public static void addBoilerFuel(Consumer<FinishedRecipe> provider, Set<Item> added,
                                     Item item, int burnTime) {
        if (added.contains(item) || burnTime <= 0) {
            return;
        }
        added.add(item);

        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(item.getDefaultInstance());
        if (containedFluid.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            STEAM_BOILER_RECIPES.recipeBuilder(GTCEu.id(id.getNamespace() + "_" + id.getPath()))
                    .inputItems(item)
                    .duration(burnTime)
                    .save(provider);
        } else {
            FluidStack fluid = new FluidStack(containedFluid.get(), 250);
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());

            // the lava recipe's duration is 4/9 of the bucket's burn time
            // the creosote recipe's duration is 7/32 of the bucket's burn time
            // as such, the mean ratio is 191/576, or approximately 1/3.
            STEAM_BOILER_RECIPES.recipeBuilder(id.getNamespace() + "_" + id.getPath())
                    .inputFluids(fluid)
                    .duration(burnTime / 3)
                    .save(provider);
        }
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        // TODO this all needs to be cleaned up, but this will make it somewhat work for now
        // do these first because for some reason vanilla fuels are not set up yet at this phase?
        Set<Item> addedItems = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            int burnTime = GTUtil.getItemBurnTime(item);
            addBoilerFuel(provider, addedItems, item, burnTime);
        }

        // override the default fluid recipes for lava and creosote
        STEAM_BOILER_RECIPES.recipeBuilder("minecraft_lava")
                .inputFluids(new FluidStack(Fluids.LAVA, 100))
                .duration(900) // 60s -> 45s Might still be too good with drip stone farming.
                .save(provider);

        STEAM_BOILER_RECIPES.recipeBuilder("gtceu_creosote")
                .inputFluids(Creosote.getFluid(250))
                .duration(350) // 150s -> 17.5s
                .save(provider);

        // semi-fluid fuels, like creosote - these are awful and need to be scrutinized heavily...
        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_creosote")
                .inputFluids(Creosote.getFluid(250))
                .duration(35)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_biomass")
                .inputFluids(Biomass.getFluid(40))
                .duration(85)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_oil")
                .inputFluids(Oil.getFluid(200))
                .duration(50)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_oil_heavy")
                .inputFluids(OilHeavy.getFluid(32))
                .duration(50)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_sulfuric_heavy_fuel")
                .inputFluids(SulfuricHeavyFuel.getFluid(32))
                .duration(50)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_heavy_fuel")
                .inputFluids(HeavyFuel.getFluid(16))
                .duration(90)
                .save(provider);

        LARGE_BOILER_RECIPES.recipeBuilder("gtceu_fish_oil")
                .inputFluids(FishOil.getFluid(160))
                .duration(50)
                .save(provider);

        // diesel generator fuels
        COMBUSTION_GENERATOR_FUELS.recipeBuilder("naphtha")
                .inputFluids(Naphtha.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("sulfuric_light_fuel")
                .inputFluids(SulfuricLightFuel.getFluid(4))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("methanol")
                .inputFluids(Methanol.getFluid(4))
                .duration(8)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("ethanol")
                .inputFluids(Ethanol.getFluid(1))
                .duration(6)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("octane")
                .inputFluids(Octane.getFluid(2))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("biodiesel")
                .inputFluids(BioDiesel.getFluid(1))
                .duration(8)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("light_fuel")
                .inputFluids(LightFuel.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("diesel")
                .inputFluids(Diesel.getFluid(1))
                .duration(15)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("cetane_diesel")
                .inputFluids(CetaneBoostedDiesel.getFluid(2))
                .duration(45)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("rocket_fuel")
                .inputFluids(RocketFuel.getFluid(16))
                .duration(125)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("gasoline")
                .inputFluids(Gasoline.getFluid(1))
                .duration(50)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("high_octane_gasoline")
                .inputFluids(HighOctaneGasoline.getFluid(1))
                .duration(100)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("toluene")
                .inputFluids(Toluene.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("light_oil")
                .inputFluids(OilLight.getFluid(32))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        COMBUSTION_GENERATOR_FUELS.recipeBuilder("raw_oil")
                .inputFluids(RawOil.getFluid(64))
                .duration(15)
                .EUt(-V[LV])
                .save(provider);

        // steam generator fuels
        STEAM_TURBINE_FUELS.recipeBuilder("steam")
                .inputFluids(Steam.getFluid(640))
                .outputFluids(DistilledWater.getFluid(4))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        // gas turbine fuels
        GAS_TURBINE_FUELS.recipeBuilder("natural_gas")
                .inputFluids(NaturalGas.getFluid(8))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("wood_gas")
                .inputFluids(WoodGas.getFluid(8))
                .duration(6)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("sulfuric_gas")
                .inputFluids(SulfuricGas.getFluid(32))
                .duration(25)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("sulfuric_naphtha")
                .inputFluids(SulfuricNaphtha.getFluid(4))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("coal_gas")
                .inputFluids(CoalGas.getFluid(1))
                .duration(3)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("methane")
                .inputFluids(Methane.getFluid(2))
                .duration(7)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("ethylene")
                .inputFluids(Ethylene.getFluid(1))
                .duration(4)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("refinery_gas")
                .inputFluids(RefineryGas.getFluid(1))
                .duration(5)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("ethane")
                .inputFluids(Ethane.getFluid(4))
                .duration(21)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("propene")
                .inputFluids(Propene.getFluid(1))
                .duration(6)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("butadiene")
                .inputFluids(Butadiene.getFluid(16))
                .duration(102)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("propane")
                .inputFluids(Propane.getFluid(4))
                .duration(29)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("butene")
                .inputFluids(Butene.getFluid(1))
                .duration(8)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("phenol")
                .inputFluids(Phenol.getFluid(1))
                .duration(9)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("benzene")
                .inputFluids(Benzene.getFluid(1))
                .duration(11)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("butane")
                .inputFluids(Butane.getFluid(4))
                .duration(37)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("lpg")
                .inputFluids(LPG.getFluid(1))
                .duration(10)
                .EUt(-V[LV])
                .save(provider);

        GAS_TURBINE_FUELS.recipeBuilder("nitrobenzene") // TODO Too OP pls nerf
                .inputFluids(Nitrobenzene.getFluid(1))
                .duration(40)
                .EUt(-V[LV])
                .save(provider);

        // plasma turbine
        PLASMA_GENERATOR_FUELS.recipeBuilder("helium")
                .inputFluids(Helium.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Helium.getFluid(1))
                .duration(40)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("oxygen")
                .inputFluids(Oxygen.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Oxygen.getFluid(1))
                .duration(48)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("nitrogen")
                .inputFluids(Nitrogen.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Nitrogen.getFluid(1))
                .duration(64)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("argon")
                .inputFluids(Argon.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Argon.getFluid(1))
                .duration(96)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("iron")
                .inputFluids(Iron.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Iron.getFluid(1))
                .duration(112)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("tin")
                .inputFluids(Tin.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Tin.getFluid(1))
                .duration(128)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("nickel")
                .inputFluids(Nickel.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Nickel.getFluid(1))
                .duration(192)
                .EUt(-V[EV])
                .save(provider);

        PLASMA_GENERATOR_FUELS.recipeBuilder("americium")
                .inputFluids(Americium.getFluid(FluidStorageKeys.PLASMA, 1))
                .outputFluids(Americium.getFluid(1))
                .duration(320)
                .EUt(-V[EV])
                .save(provider);
    }
}
