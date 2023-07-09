package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote PyrolyseOvenMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CrackerMachine extends CoilWorkableElectricMultiblockMachine {

    public CrackerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    //////////////////////////////////////
    //***    Multiblock LifeCycle    ***//
    //////////////////////////////////////
    @Override
    public @Nullable GTRecipe getRealRecipe(GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > getTier()) {
            return null;
        }
        return RecipeHelper.applyOverclock(new OverclockingLogic(false) {
            @Override
            protected LongIntPair runOverclockingLogic(@NotNull GTRecipe recipe, long recipeEUt, long maxVoltage, int duration, int amountOC) {
                var pair = super.runOverclockingLogic(recipe, recipeEUt, maxVoltage, duration, amountOC);
                performNonOverclockBonuses(pair);
                return pair;
            }
        }, recipe, getMaxVoltage());
    }

    protected void performNonOverclockBonuses(LongIntPair resultOverclock) {
        if (getCoilTier() <= 0)
            return;
        var eu = resultOverclock.firstLong() * (1 - getCoilTier() * 0.1);
        resultOverclock.first((long) Math.max(1, eu));
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.cracking_unit.energy",100 - 10 * getCoilTier()));
        }
    }
}
