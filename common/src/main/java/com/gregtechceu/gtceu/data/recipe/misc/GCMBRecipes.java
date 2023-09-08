package com.gregtechceu.gtceu.data.recipe.misc;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import org.apache.logging.log4j.LogManager;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.GTCEu.LOGGER;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.BLAST_ALLOY_CRAFTABLE;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class GCMBRecipes {

    private GCMBRecipes() {
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        //registerManualRecipes(provider);
        registerMachineRecipes(provider);
    }

    private static void registerMachineRecipes(Consumer<FinishedRecipe> provider) {
        registerMixerRecipes(provider);
        registerBlastAlloyRecipes(provider);

        BLAST_ALLOY_RECIPES.recipeBuilder("test")
                .inputFluids(Copper.getFluid(3*144),Tin.getFluid(144)).outputFluids(Bronze.getFluid(4*144))
                .duration(69).EUt(420).save(provider);

        for(Material material : GTRegistries.MATERIALS){
            LogManager.getLogger().fatal(material.getName());
            LOGGER.warn(material.getName());
            System.out.println(material.getName());
            if(!material.hasFlag(BLAST_ALLOY_CRAFTABLE)){
                System.out.println("no flag");
                return;
            }
            ImmutableList<MaterialStack> components = material.getMaterialComponents();
            if(components.size() > 6){
                System.out.println("too many components");
                return;
            }
            int ct = 0;
            GTRecipeBuilder recipe = BLAST_ALLOY_RECIPES.recipeBuilder(material.getName()+"_blast_alloy_smelting");
            for(MaterialStack component : components){
                recipe.inputFluids(component.material().getFluid(component.amount() * FluidHelper.getBucket()));
                ct += component.amount();
            }
            recipe.outputFluids(material.getFluid(ct * FluidHelper.getBucket()));
            long eut = switch (material.getBlockHarvestLevel()) {
                case 1 -> V[MV];
                case 2 -> V[HV];
                case 3 -> V[EV];
                case 4 -> V[IV];
                case 5 -> V[LuV];
                case 6 -> V[ZPM];
                default -> V[LV];
            };
            recipe.inputEU(eut).duration(material.getBlastTemperature()).save(provider);
        }
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
    }

    private static final MaterialStack[][] blastAlloyList = {
        {new MaterialStack(Copper, 3), new MaterialStack(Tin, 1), new MaterialStack(Bronze, 4L)},
        {new MaterialStack(Copper, 3L), new MaterialStack(Zinc, 1), new MaterialStack(Brass, 4L)},
        {new MaterialStack(Copper, 1), new MaterialStack(Nickel, 1), new MaterialStack(Cupronickel, 2L)},
        {new MaterialStack(Copper, 1), new MaterialStack(Redstone, 4L), new MaterialStack(RedAlloy, 1)},
        {new MaterialStack(AnnealedCopper, 3L), new MaterialStack(Tin, 1), new MaterialStack(Bronze, 4L)},
        {new MaterialStack(AnnealedCopper, 3L), new MaterialStack(Zinc, 1), new MaterialStack(Brass, 4L)},
        {new MaterialStack(AnnealedCopper, 1), new MaterialStack(Nickel, 1), new MaterialStack(Cupronickel, 2L)},
        {new MaterialStack(AnnealedCopper, 1), new MaterialStack(Redstone, 4L), new MaterialStack(RedAlloy, 1)},
        {new MaterialStack(Iron, 1), new MaterialStack(Tin, 1), new MaterialStack(TinAlloy, 2L)},
        {new MaterialStack(WroughtIron, 1), new MaterialStack(Tin, 1), new MaterialStack(TinAlloy, 2L)},
        {new MaterialStack(Iron, 2L), new MaterialStack(Nickel, 1), new MaterialStack(Invar, 3L)},
        {new MaterialStack(WroughtIron, 2L), new MaterialStack(Nickel, 1), new MaterialStack(Invar, 3L)},
        {new MaterialStack(Lead, 4L), new MaterialStack(Antimony, 1), new MaterialStack(BatteryAlloy, 5L)},
        {new MaterialStack(Gold, 1), new MaterialStack(Silver, 1), new MaterialStack(Electrum, 2L)},
        {new MaterialStack(Magnesium, 1), new MaterialStack(Aluminium, 2L), new MaterialStack(Magnalium, 3L)},
        {new MaterialStack(Silver, 1), new MaterialStack(Electrotine, 4), new MaterialStack(BlueAlloy, 1)}
    };

    private static void registerBlastAlloyRecipes(Consumer<FinishedRecipe> provider) {
        for (MaterialStack[] stack : blastAlloyList) {
            String recipeNape = stack[0].material().getName() + "_%s_and_" + stack[1].material().getName() + "_%s_into_" + stack[2].material().getName();
            if (stack[0].material().hasProperty(PropertyKey.INGOT)) {
                BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "ingot", "dust"))
                        .duration((int) stack[2].amount() * 25).EUt((long) Math.pow(2,stack[2].amount()))
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                        .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                        .blastFurnaceTemp(calculateTemp(stack))
                        .save(provider);
                BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "ingot", "fluid"))
                        .duration((int) stack[2].amount() * 25).EUt((long) Math.pow(2,stack[2].amount()))
                        .inputFluids(stack[1].material().getFluid((int) stack[1].amount()))
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                        .blastFurnaceTemp(calculateTemp(stack))
                        .save(provider);
            }
            if (stack[1].material().hasProperty(PropertyKey.INGOT)) {
                BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "dust", "ingot"))
                        .duration((int) stack[2].amount() * 25).EUt((long) Math.pow(2,stack[2].amount()))
                        .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                        .inputFluids(stack[1].material().getFluid((int) stack[1].amount()))
                        .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                        .blastFurnaceTemp(calculateTemp(stack))
                        .save(provider);
                BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "fluid", "ingot"))
                        .duration((int) stack[2].amount() * 25).EUt((long) Math.pow(2,stack[2].amount()))
                        .inputItems(ingot, stack[1].material(), (int) stack[1].amount())
                        .inputFluids(stack[0].material().getFluid(stack[0].amount()))
                        .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                        .blastFurnaceTemp(calculateTemp(stack))
                        .save(provider);
            }
            if (stack[0].material().hasProperty(PropertyKey.INGOT) && stack[1].material().hasProperty(PropertyKey.INGOT)) {
                BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "ingot", "ingot"))
                        .duration((int) stack[2].amount() * 25).EUt((long) Math.pow(2,stack[2].amount()))
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .inputItems(ingot, stack[1].material(), (int) stack[1].amount())
                        .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                        .blastFurnaceTemp(calculateTemp(stack))
                        .save(provider);
            }
            BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "dust", "dust"))
                    .duration((int) stack[2].amount() * 50).EUt(16)
                    .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                    .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                    .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                    .blastFurnaceTemp(calculateTemp(stack))
                    .save(provider);
            BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "dust", "fluid"))
                    .duration((int) stack[2].amount() * 50).EUt(16)
                    .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                    .inputFluids(stack[1].material().getFluid(stack[1].amount()))
                    .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                    .blastFurnaceTemp(calculateTemp(stack))
                    .save(provider);
            BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "fluid", "dust"))
                    .duration((int) stack[2].amount() * 50).EUt(16)
                    .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                    .inputFluids(stack[0].material().getFluid(stack[0].amount()))
                    .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                    .blastFurnaceTemp(calculateTemp(stack))
                    .save(provider);
            BLAST_ALLOY_RECIPES.recipeBuilder(String.format(recipeNape, "fluid", "fluid"))
                    .duration((int) stack[2].amount() * 25).EUt((long) Math.pow(2,stack[2].amount()))
                    .inputFluids(stack[0].material().getFluid(stack[0].amount()))
                    .inputFluids(stack[1].material().getFluid(stack[1].amount()))
                    .outputFluids(stack[2].material().getFluid(stack[2].amount() * 144L))
                    .blastFurnaceTemp(calculateTemp(stack))
                    .save(provider);
        }
    }

    private static int calculateTemp(MaterialStack[] stack) {
        return Math.max(Math.max(5000,stack[2].material().getBlastTemperature()),Math.max(stack[0].material().getBlastTemperature(),stack[1].material().getBlastTemperature()));
    }
}
