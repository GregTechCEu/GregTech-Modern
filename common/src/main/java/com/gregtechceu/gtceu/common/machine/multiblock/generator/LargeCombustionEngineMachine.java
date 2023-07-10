package com.gregtechceu.gtceu.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import lombok.val;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote LargeCombustionEngineMachine
 */
public class LargeCombustionEngineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {
    private static final FluidStack OXYGEN_STACK = GTMaterials.Oxygen.getFluid(20 * FluidHelper.getBucket() / 1000);
    private static final FluidStack LIQUID_OXYGEN_STACK = GTMaterials.LiquidOxygen.getFluid(80 * FluidHelper.getBucket() / 1000);
    private static final FluidStack LUBRICANT_STACK = GTMaterials.Lubricant.getFluid(FluidHelper.getBucket() / 1000);

    @Getter
    private final int tier;
    // runtime
    private boolean isOxygenBoosted = false;

    public LargeCombustionEngineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    private boolean checkIntakesObstructed() {
        var facing = this.getFrontFacing();
        boolean permuteXZ = facing.getAxis() == Direction.Axis.Z;
        var centerPos = this.getPos().relative(facing);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                //Skip the controller block itself
                if (x == 0 && y == 0)
                    continue;
                var blockPos = centerPos.offset(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    private boolean isExtreme() {
        return getTier() > GTValues.EV;
    }

    //////////////////////////////////////
    //******     Recipe Logic    *******//
    //////////////////////////////////////

    @Override
    public long getOverclockVoltage() {
        if (isOxygenBoosted)
            return GTValues.V[tier] * 2;
        else
            return GTValues.V[tier];
    }

    protected GTRecipe getLubricantRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(LUBRICANT_STACK).buildRawRecipe();
    }

    protected GTRecipe getBoostRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(isExtreme() ? LIQUID_OXYGEN_STACK : OXYGEN_STACK).buildRawRecipe();
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof LargeCombustionEngineMachine engineMachine) {
            var EUt = RecipeHelper.getOutputEUt(recipe);
            // has lubricant
            if (EUt > 0 && engineMachine.getLubricantRecipe().matchRecipe(engineMachine).isSuccess() && !engineMachine.checkIntakesObstructed()) {
                var maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt); // get maximum parallel
                var parallelResult = GTRecipeModifiers.fastParallel(engineMachine, recipe, maxParallel, false);
                if (engineMachine.isOxygenBoosted) { // boost production
                    recipe = parallelResult.getA() == recipe ? recipe.copy() : parallelResult.getA();
                    long eut = (long) (EUt * parallelResult.getB() * (engineMachine.isExtreme() ? 2 : 1.5));
                    recipe.tickOutputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 1.0f, null, null)));
                } else {
                    recipe = parallelResult.getA();
                }
                return recipe;
            }
        }
        return null;
    }

    @Override
    public void onWorking() {
        super.onWorking();
        // check lubricant
        val totalContinuousRunningTime = recipeLogic.getTotalContinuousRunningTime();
        if ((totalContinuousRunningTime == 1 || totalContinuousRunningTime % 72 == 0)) {
            // insufficient lubricant
            if (!getLubricantRecipe().handleRecipeIO(IO.IN, this)) {
                recipeLogic.interruptRecipe();
            }
        }
        // check boost fluid
        if ((totalContinuousRunningTime == 1 || totalContinuousRunningTime % 20 == 0) && getMaxVoltage() >= GTValues.V[getTier() + 1]) {
            var boosterRecipe = getBoostRecipe();
            this.isOxygenBoosted = boosterRecipe.matchRecipe(this).isSuccess() && boosterRecipe.handleRecipeIO(IO.IN, this);
        }
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }
}
