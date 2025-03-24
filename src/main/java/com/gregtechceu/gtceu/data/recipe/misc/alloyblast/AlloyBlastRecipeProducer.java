package com.gregtechceu.gtceu.data.recipe.misc.alloyblast;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingotHot;

public class AlloyBlastRecipeProducer {

    public static final AlloyBlastRecipeProducer DEFAULT_PRODUCER = new AlloyBlastRecipeProducer();

    /**
     * Generates alloy blast recipes for a material
     *
     * @param material the material to generate for
     * @param property the blast property of the material
     */
    public void produce(@NotNull Material material, @NotNull BlastProperty property,
                        Consumer<FinishedRecipe> provider) {
        // do not generate for disabled materials
        if (material.hasFlag(MaterialFlags.DISABLE_ALLOY_BLAST)) return;

        final int componentAmount = material.getMaterialComponents().size();

        // ignore non-alloys
        if (componentAmount < 2) return;

        // get the output fluid
        Fluid molten;
        if (ingotHot.doGenerateItem(material)) {
            molten = GTUtil.getMoltenFluid(material);
            addFreezerRecipes(material, molten, property.getBlastTemperature(), provider);
        } else {
            molten = material.getFluid();
        }
        if (molten == null) return;

        GTRecipeBuilder builder = createBuilder(property, material);

        int outputAmount = addInputs(material, builder);
        if (outputAmount <= 0) return;

        buildRecipes(property, molten, outputAmount, componentAmount, builder, provider);
    }

    /**
     * Creates the recipeBuilder with duration and EUt
     *
     * @param property the blast property of the material
     * @param material the material
     * @return the builder
     */
    @SuppressWarnings("MethodMayBeStatic")
    @NotNull
    protected GTRecipeBuilder createBuilder(@NotNull BlastProperty property, @NotNull Material material) {
        GTRecipeBuilder builder = GCYMRecipeTypes.ALLOY_BLAST_RECIPES.recipeBuilder(material.getName());
        // apply the duration override
        int duration = property.getDurationOverride();
        if (duration < 0) duration = Math.max(1, (int) (material.getMass() * property.getBlastTemperature() / 100L));
        builder.duration(duration);

        // apply the EUt override
        int EUt = property.getEUtOverride();
        if (EUt < 0) EUt = GTValues.VA[GTValues.MV];
        builder.EUt(EUt);

        return builder.blastFurnaceTemp(property.getBlastTemperature());
    }

    /**
     * @param material the material to start recipes for
     * @param builder  the recipe builder to append to
     * @return the outputAmount if the recipe is valid, otherwise -1
     */
    protected int addInputs(@NotNull Material material, @NotNull GTRecipeBuilder builder) {
        // calculate the output amount and add inputs
        int outputAmount = 0;
        int fluidAmount = 0;
        for (MaterialStack materialStack : material.getMaterialComponents()) {
            final Material msMat = materialStack.material();
            final int msAmount = (int) materialStack.amount();

            if (msMat.hasProperty(PropertyKey.DUST)) {
                builder.inputItems(TagPrefix.dust, msMat, msAmount);
            } else if (msMat.hasProperty(PropertyKey.FLUID)) {
                if (fluidAmount >= 2) return -1; // more than 2 fluids won't fit in the machine
                fluidAmount++;
                // assume all fluids have 1000mB/mol, since other quantities should be as an item input
                builder.inputFluids(msMat.getFluid(1000 * msAmount));
            } else return -1; // no fluid or item prop means no valid recipe
            outputAmount += msAmount;
        }
        return outputAmount;
    }

    /**
     * Builds the alloy blast recipes
     *
     * @param property        the blast property to utilize
     * @param molten          the molten fluid
     * @param outputAmount    the amount of material to output
     * @param componentAmount the amount of different components in the material
     * @param builder         the builder to continue
     */
    protected void buildRecipes(@NotNull BlastProperty property, @NotNull Fluid molten, int outputAmount,
                                int componentAmount,
                                @NotNull GTRecipeBuilder builder, Consumer<FinishedRecipe> provider) {
        // add the fluid output with the correct amount
        builder.outputFluids(new FluidStack(molten, GTValues.L * outputAmount));

        // apply alloy blast duration reduction: 3/4
        int duration = builder.duration * outputAmount * 3 / 4;

        // build the gas recipe if it exists
        if (property.getGasTier() != null) {
            GTRecipeBuilder builderGas = builder.copy(builder.id.getPath() + "_gas");
            FluidIngredient gas = property.getGasTier().getFluid();
            gas.setAmount(gas.getAmount() * outputAmount);
            builderGas.circuitMeta(getGasCircuitNum(componentAmount))
                    .inputFluids(gas)
                    .duration((int) (duration * 0.67))
                    .save(provider);
        }

        // build the non-gas recipe
        builder.circuitMeta(getCircuitNum(componentAmount))
                .duration(duration)
                .save(provider);
    }

    /**
     * @param componentAmount the amount of different components in the material
     * @return the circuit number for the regular recipe
     */
    protected int getCircuitNum(int componentAmount) {
        return componentAmount;
    }

    /**
     * @param componentAmount the amount of different components in the material
     * @return the circuit number for the gas-boosted recipe
     */
    protected int getGasCircuitNum(int componentAmount) {
        return componentAmount + 10;
    }

    /**
     * Add the freezer recipes for the material
     *
     * @param material    the material to generate for
     * @param molten      the molten fluid
     * @param temperature the temperature of the material
     */
    @SuppressWarnings("MethodMayBeStatic")
    protected void addFreezerRecipes(@NotNull Material material, @NotNull Fluid molten, int temperature,
                                     Consumer<FinishedRecipe> provider) {
        // build the freezer recipe
        GTRecipeBuilder freezerBuilder = GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(material.getName())
                .inputFluids(new FluidStack(molten, GTValues.L))
                .duration((int) material.getMass() * 3)
                .notConsumable(GTItems.SHAPE_MOLD_INGOT.asStack())
                .outputItems(TagPrefix.ingot, material);

        // helium for when >= 5000K temperature
        if (temperature >= 5000) {
            freezerBuilder.inputFluids(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 500))
                    .outputFluids(GTMaterials.Helium.getFluid(250));
        }
        freezerBuilder.save(provider);
    }
}
