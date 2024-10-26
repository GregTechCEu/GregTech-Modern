package com.gregtechceu.gtceu.data.recipe.misc.alloyblast;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class CustomAlloyBlastRecipeProducer extends AlloyBlastRecipeProducer {

    private final int circuitNum;
    private final int gasCircuitNum;
    private final int outputAmount;

    /**
     * @param circuitNum    the custom circuit number to use
     * @param gasCircuitNum the custom gas circuit number to use
     * @param outputAmount  the custom output amount in quantities of
     *                      {@link com.gregtechceu.gtceu.api.data.tag.TagPrefix#ingot}
     *                      / {@link com.gregtechceu.gtceu.api.GTValues#M}) to use
     */
    public CustomAlloyBlastRecipeProducer(int circuitNum, int gasCircuitNum, int outputAmount) {
        this.circuitNum = circuitNum;
        this.gasCircuitNum = gasCircuitNum;
        Preconditions.checkArgument(outputAmount != 0, "output amount cannot be zero");
        this.outputAmount = outputAmount;
    }

    @Override
    protected int addInputs(@NotNull Material material, @NotNull GTRecipeBuilder builder) {
        int amount = super.addInputs(material, builder); // always must be called
        return this.outputAmount < 0 ? amount : this.outputAmount;
    }

    @Override
    protected int getCircuitNum(int componentAmount) {
        return this.circuitNum < 0 ? super.getCircuitNum(componentAmount) : this.circuitNum;
    }

    @Override
    protected int getGasCircuitNum(int componentAmount) {
        return this.gasCircuitNum < 0 ? super.getGasCircuitNum(componentAmount) : this.gasCircuitNum;
    }
}
