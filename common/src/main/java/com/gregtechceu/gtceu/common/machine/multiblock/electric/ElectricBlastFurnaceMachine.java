package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote ElectricBlastFurnaceMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricBlastFurnaceMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private int blastFurnaceTemperature;

    public ElectricBlastFurnaceMachine(IMachineBlockEntity holder) {
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
            this.blastFurnaceTemperature = coilType.getCoilTemperature();
        } else {
            this.blastFurnaceTemperature = CoilBlock.CoilType.CUPRONICKEL.getCoilTemperature();
        }

        this.blastFurnaceTemperature += 100 * Math.max(0, getTier() - GTValues.MV);
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        if (!recipe.data.contains("ebf_temp") || recipe.data.getInt("ebf_temp") > blastFurnaceTemperature) {
            return null;
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > getTier()) {
            return null;
        }
        return RecipeHelper.applyOverclock(new OverclockingLogic(false) {
            @Override
            protected LongIntPair runOverclockingLogic(@NotNull GTRecipe recipe, long recipeEUt, long maxVoltage, int duration, int amountOC) {
                return heatingCoilOverclockingLogic(
                        Math.abs(recipeEUt),
                        maxVoltage,
                        duration,
                        amountOC,
                        blastFurnaceTemperature,
                        recipe.data.contains("ebf_temp") ? 0 : recipe.data.getInt("ebf_temp")
                );
            }
        }, recipe, getMaxVoltage());
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                    Component.translatable(FormattingUtil.formatNumbers(blastFurnaceTemperature) + "K").setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
        }
        super.addDisplayText(textList);
    }
}
