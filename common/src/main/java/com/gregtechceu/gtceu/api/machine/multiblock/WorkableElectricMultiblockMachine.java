package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/6
 * @implNote WorkableElectricMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkableElectricMultiblockMachine extends WorkableMultiblockMachine implements IDisplayUIMachine, ITieredMachine {

    public WorkableElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            var maxVoltage = getMaxVoltage();
            if (maxVoltage > 0) {
                String voltageName = GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

//            if (canBeDistinct() && inputInventory.getSlots() > 0) {
//                var buttonText = Component.translatable("gtceu.multiblock.universal.distinct");
//                buttonText.appendText(" ");
//                var button = AdvancedTextWidget.withButton(isDistinct() ?
//                        Component.translatable("gtceu.multiblock.universal.distinct.yes").setStyle(Style.EMPTY.setColor(TextFormatting.GREEN)) :
//                        Component.translatable("gtceu.multiblock.universal.distinct.no").setStyle(Style.EMPTY.setColor(TextFormatting.RED)), "distinct");
//                AdvancedTextWidget.withHoverTextTranslate(button, "gtceu.multiblock.universal.distinct.info");
//                buttonText.appendSibling(button);
//                textList.add(buttonText);
//            }

            textList.add(Component.translatable("gtceu.multiblock.multiple_recipemaps.header")
                    .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("gtceu.multiblock.multiple_recipemaps.tooltip")))));

            textList.add(Component.translatable(recipeType.registryName.toLanguageKey())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.multiblock.multiple_recipemaps.tooltip")))));

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
//                if (this.recipeMapWorkable.getParallelLimit() != 1) {
//                    textList.add(Component.translatable("gtceu.multiblock.parallel", this.recipeMapWorkable.getParallelLimit()));
//                }
                textList.add(Component.translatable("gtceu.multiblock.progress", currentProgress));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isHasNotEnoughEnergy()) {
                textList.add(Component.translatable("gtceu.multiblock.not_enough_energy").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }


    //////////////////////////////////////
    //******     RECIPE LOGIC    *******//
    //////////////////////////////////////

    /**
     * Get energy tier.
     */
    @Override
    public int getTier() {
        return GTUtil.getFloorTierByVoltage(getMaxVoltage());
    }

    public long getMaxVoltage() {
        long maxVoltage = 0L;
        var capabilities = capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP);
        if (capabilities != null) {
            for (IRecipeHandler<?> handler : capabilities) {
                if (handler instanceof IEnergyContainer container) {
                    maxVoltage += container.getInputVoltage() * container.getInputAmperage();
                }
            }
        } else {
            capabilities = capabilitiesProxy.get(IO.OUT, EURecipeCapability.CAP);
            if (capabilities != null) {
                for (IRecipeHandler<?> handler : capabilities) {
                    if (handler instanceof IEnergyContainer container) {
                        maxVoltage += container.getOutputVoltage() * container.getOutputAmperage();
                    }
                }
            }
        }
        return maxVoltage;
    }

    @Nullable
    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > getTier()) {
            return null;
        }
        return RecipeHelper.applyOverclock(getDefinition().getOverclockingLogic(), recipe, getMaxVoltage());
    }
}
