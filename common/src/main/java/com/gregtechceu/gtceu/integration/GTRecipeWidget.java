package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.msic.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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

        List<List<ItemStack>> inputStacks = recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                .map(content -> content.content)
                .map(ItemRecipeCapability.CAP::of)
                .map(Ingredient::getItems)
                .map(Arrays::stream)
                .map(Stream::toList)
                .collect(Collectors.toList());
        while (inputStacks.size() < recipe.recipeType.getMaxInputs(ItemRecipeCapability.CAP)) inputStacks.add(null);

        List<List<ItemStack>> outputStacks = recipe.getOutputContents(ItemRecipeCapability.CAP).stream()
                .map(content -> content.content)
                .map(ItemRecipeCapability.CAP::of)
                .map(Ingredient::getItems)
                .map(Arrays::stream)
                .map(Stream::toList)
                .collect(Collectors.toList());
        while (outputStacks.size() < recipe.recipeType.getMaxOutputs(ItemRecipeCapability.CAP)) outputStacks.add(null);

        IFluidStorage[] inputFluids = new IFluidStorage[recipe.recipeType.getMaxInputs(FluidRecipeCapability.CAP)];
        List<Content> inputFluidContents = recipe.getInputContents(FluidRecipeCapability.CAP);
        for (int i = 0; i < inputFluidContents.size(); i++) {
            inputFluids[i] = new FluidStorage(FluidRecipeCapability.CAP.of(inputFluidContents.get(i).content));
        }

        IFluidStorage[] outputFluids = new IFluidStorage[recipe.recipeType.getMaxOutputs(FluidRecipeCapability.CAP)];
        List<Content> outputFluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);
        for (int i = 0; i < outputFluidContents.size(); i++) {
            outputFluids[i] = new FluidStorage(FluidRecipeCapability.CAP.of(outputFluidContents.get(i).content));
        }

        var group = recipe.recipeType.createUITemplate(ProgressWidget.JEIProgress,
                new CycleItemStackHandler(inputStacks),
                new CycleItemStackHandler(outputStacks),
                inputFluids,
                outputFluids
        );
        var size = group.getSize();
        group.setSelfPosition(new Position((176 - size.width) / 2, 0));
        addWidget(group);

        int yOffset = 87 - (recipe.conditions.size() - recipe.recipeType.getMaxConditions() + recipe.recipeType.getDataInfos().size()) * 10 + (size.height - 83);
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
        for (RecipeCondition condition : recipe.conditions) {
            addWidget(new LabelWidget(3, yOffset += 10, condition.getTooltips().getString()));
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            addWidget(new LabelWidget(3, yOffset += 10, dataInfo.apply(recipe.data)));
        }
        recipe.recipeType.appendJEIUI(recipe, this);
    }
}
