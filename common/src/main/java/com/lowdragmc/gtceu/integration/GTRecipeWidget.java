package com.lowdragmc.gtceu.integration;

import com.lowdragmc.gtceu.api.GTValues;
import com.lowdragmc.gtceu.api.capability.recipe.EURecipeCapability;
import com.lowdragmc.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.lowdragmc.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.lowdragmc.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.gtceu.api.recipe.RecipeHelper;
import com.lowdragmc.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.msic.FluidStorage;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote GTRecipeWidget
 */
public class GTRecipeWidget extends WidgetGroup {
    public GTRecipeWidget(GTRecipe recipe) {
        super(0, 0, recipe.recipeType.getJEISize().width, recipe.recipeType.getJEISize().height);
        setClientSideWidget();
        addWidget(recipe.recipeType.createUITemplate(ProgressWidget.JEIProgress,
                new CycleItemStackHandler(
                        recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                                .map(content -> content.content)
                                .map(ItemRecipeCapability.CAP::of)
                                .map(Ingredient::getItems)
                                .map(Arrays::stream)
                                .map(Stream::toList)
                                .toList()),
                new CycleItemStackHandler(
                        recipe.getOutputContents(ItemRecipeCapability.CAP).stream()
                                .map(content -> content.content)
                                .map(ItemRecipeCapability.CAP::of)
                                .map(Ingredient::getItems)
                                .map(Arrays::stream)
                                .map(Stream::toList)
                                .toList()),
                recipe.getInputContents(FluidRecipeCapability.CAP).stream()
                        .map(content -> content.content)
                        .map(FluidRecipeCapability.CAP::of)
                        .map(FluidStorage::new)
                        .toArray(FluidStorage[]::new),
                recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                        .map(content -> content.content)
                        .map(FluidRecipeCapability.CAP::of)
                        .map(FluidStorage::new)
                        .toArray(FluidStorage[]::new)));

        int yOffset = 60;
        addWidget(new LabelWidget(3, yOffset,
                LocalizationUtils.format("gtceu.recipe.duration", recipe.duration / 20f)));
        var EUt = RecipeHelper.getInputEUt(recipe);
        boolean isOutput = false;
        if (EUt == 0) {
            EUt = RecipeHelper.getOutputEUt(recipe);
            isOutput = true;
        }
        if (EUt > 0) {
            addWidget(new LabelWidget(3, yOffset += 10,
                    LocalizationUtils.format("gtceu.recipe.total", EUt * recipe.duration)));
            addWidget(new LabelWidget(3, yOffset += 10,
                    LocalizationUtils.format(!isOutput ? "gtceu.recipe.eu" : "gregtech.recipe.eu_inverted", EUt, GTValues.VN[GTUtil.getTierByVoltage(EUt)])));
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            addWidget(new LabelWidget(3, yOffset += 10, dataInfo.apply(recipe.data)));
        }
        recipe.recipeType.appendJEIUI(recipe, this);
    }
}
