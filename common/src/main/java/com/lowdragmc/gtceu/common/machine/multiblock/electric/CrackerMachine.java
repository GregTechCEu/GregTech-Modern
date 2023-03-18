package com.lowdragmc.gtceu.common.machine.multiblock.electric;

import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.gtceu.api.recipe.OverclockingLogic;
import com.lowdragmc.gtceu.api.recipe.RecipeHelper;
import com.lowdragmc.gtceu.common.block.variant.CoilBlock;
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
public class CrackerMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private int coilTier;

    public CrackerMachine(IMetaMachineBlockEntity holder) {
        super(holder);
    }

    //////////////////////////////////////
    //***    Multiblock LifeCycle    ***//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var type = getMultiblockState().getMatchContext().get("CoilType");
        if (type instanceof CoilBlock.CoilType coilType) {
            this.coilTier = coilType.getTier();
        } else {
            this.coilTier = 0;
        }
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
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
        if (coilTier <= 0)
            return;
        var eu = resultOverclock.firstLong() * (1 - coilTier * 0.1);
        resultOverclock.first((long) Math.max(1, eu));
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.cracking_unit.energy",100 - 10 * coilTier));
        }
    }
}
