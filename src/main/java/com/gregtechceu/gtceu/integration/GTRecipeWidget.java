package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.AssemblyLineManager;
import com.gregtechceu.gtceu.utils.CycleFluidStorage;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    public static final int LINE_HEIGHT = 10;

    public GTRecipeWidget(GTRecipe recipe) {
        super(0, 0, recipe.recipeType.getRecipeUI().getJEISize().width, recipe.recipeType.getRecipeUI().getJEISize().height);
        setClientSideWidget();
        List<Content> inputStackContents = new ArrayList<>();
        inputStackContents.addAll(recipe.getInputContents(ItemRecipeCapability.CAP));
        inputStackContents.addAll(recipe.getTickInputContents(ItemRecipeCapability.CAP));
        List<List<ItemStack>> inputStacks = inputStackContents.stream().map(content -> content.content)
                .map(ItemRecipeCapability.CAP::of)
                .map(Ingredient::getItems)
                .map(Arrays::stream)
                .map(Stream::toList)
                .collect(Collectors.toList());
        while (inputStacks.size() < recipe.recipeType.getMaxInputs(ItemRecipeCapability.CAP)) inputStacks.add(null);

        List<Content> outputStackContents = new ArrayList<>();
        outputStackContents.addAll(recipe.getOutputContents(ItemRecipeCapability.CAP));
        outputStackContents.addAll(recipe.getTickOutputContents(ItemRecipeCapability.CAP));
        List<List<ItemStack>> outputStacks = outputStackContents.stream().map(content -> content.content)
                .map(ItemRecipeCapability.CAP::of)
                .map(Ingredient::getItems)
                .map(Arrays::stream)
                .map(Stream::toList)
                .collect(Collectors.toList());

        List<List<ItemStack>> scannerPossibilities = null;
        if (recipe.recipeType.isScanner()) {
            scannerPossibilities = new ArrayList<>();
            // Scanner Output replacing, used for cycling research outputs
            String researchId = null;
            for (Content stack : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                researchId = AssemblyLineManager.readResearchId(ItemRecipeCapability.CAP.of(stack.content).getItems()[0]);
                if (researchId != null) break;
            }
            if (researchId != null) {
                Collection<GTRecipe> possibleRecipes = GTRecipeTypes.ASSEMBLY_LINE_RECIPES.getDataStickEntry(researchId);
                if (possibleRecipes != null) {
                    for (GTRecipe r : possibleRecipes) {
                        ItemStack researchItem = ItemRecipeCapability.CAP.of(r.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                        researchItem = researchItem.copy();
                        researchItem.setCount(1);
                        boolean didMatch = false;
                        for (List<ItemStack> stacks : scannerPossibilities) {
                            for (ItemStack stack : stacks) {
                                if (ItemStack.isSameItem(stack, researchItem)) {
                                    didMatch = true;
                                    break;
                                }
                            }
                        }
                        if (!didMatch) scannerPossibilities.add(List.of(researchItem));
                    }
                }
                scannerPossibilities.add(outputStacks.get(0));
            }
        }

        if (scannerPossibilities != null && !scannerPossibilities.isEmpty()) {
            outputStacks = scannerPossibilities;
        }
        while (outputStacks.size() < recipe.recipeType.getMaxOutputs(ItemRecipeCapability.CAP)) outputStacks.add(null);

        List<Content> inputFluidContents = new ArrayList<>();
        inputFluidContents.addAll(recipe.getInputContents(FluidRecipeCapability.CAP));
        inputFluidContents.addAll(recipe.getTickInputContents(FluidRecipeCapability.CAP));
        List<List<FluidStack>> inputFluids = inputFluidContents.stream().map(content -> content.content)
                .map(FluidRecipeCapability.CAP::of)
                .map(FluidIngredient::getStacks)
                .map(Arrays::stream)
                .map(Stream::toList)
                .collect(Collectors.toList());
        while (inputFluids.size() < recipe.recipeType.getMaxInputs(FluidRecipeCapability.CAP)) inputFluids.add(null);

        List<Content> outputFluidContents = new ArrayList<>();
        outputFluidContents.addAll(recipe.getOutputContents(FluidRecipeCapability.CAP));
        outputFluidContents.addAll(recipe.getTickOutputContents(FluidRecipeCapability.CAP));
        List<List<FluidStack>> outputFluids = outputFluidContents.stream().map(content -> content.content)
                .map(FluidRecipeCapability.CAP::of)
                .map(FluidIngredient::getStacks)
                .map(Arrays::stream)
                .map(Stream::toList)
                .collect(Collectors.toList());
        while (outputFluids.size() < recipe.recipeType.getMaxOutputs(FluidRecipeCapability.CAP)) outputFluids.add(null);

        WidgetGroup group = recipe.recipeType.getRecipeUI().createUITemplate(ProgressWidget.JEIProgress,
                new CycleItemStackHandler(inputStacks),
                new CycleItemStackHandler(outputStacks),
                new CycleFluidStorage(inputFluids),
                new CycleFluidStorage(outputFluids),
                recipe.data.copy()
        );
        // bind item in overlay
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.IN)), SlotWidget.class, slot -> {
            var index = WidgetUtils.widgetIdIndex(slot);
            if (index >= 0 && index < inputStackContents.size()) {
                var content = inputStackContents.get(index);
                slot.setXEIChance(content.chance);
                slot.setOverlay(content.createOverlay(index >= recipe.getInputContents(ItemRecipeCapability.CAP).size()));
                slot.setOnAddedTooltips((w, tooltips) -> {
                    var chance = content.chance;
                    if (chance < 1) {
                        tooltips.add(chance == 0 ?
                                Component.translatable("gtceu.gui.content.chance_0") :
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_1", chance * 100));
                        if (content.tierChanceBoost > 0) {
                            tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.tier_boost", content.tierChanceBoost * 100));
                        }
                    }
                    if (index >= recipe.getInputContents(ItemRecipeCapability.CAP).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        });
        // bind item out overlay
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.OUT)), SlotWidget.class, slot -> {
            var index = WidgetUtils.widgetIdIndex(slot);
            if (index >= 0 && index < outputStackContents.size()) {
                var content = outputStackContents.get(index);
                slot.setXEIChance(content.chance);
                slot.setOverlay(content.createOverlay(index >= recipe.getOutputContents(ItemRecipeCapability.CAP).size()));
                slot.setOnAddedTooltips((w, tooltips) -> {
                    var chance = content.chance;
                    if (chance < 1) {
                        tooltips.add(chance == 0 ?
                                Component.translatable("gtceu.gui.content.chance_0") : 
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_1", chance * 100));
                        if (content.tierChanceBoost > 0) {
                            tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.tier_boost", content.tierChanceBoost * 100));
                        }
                    }
                    if (index >= recipe.getOutputContents(ItemRecipeCapability.CAP).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        });
        // bind fluid in overlay
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(IO.IN)), TankWidget.class, tank -> {
            var index = WidgetUtils.widgetIdIndex(tank);
            if (index >= 0 && index < inputFluidContents.size()) {
                var content = inputFluidContents.get(index);
                tank.setXEIChance(content.chance);
                tank.setOverlay(content.createOverlay(index >= recipe.getInputContents(FluidRecipeCapability.CAP).size()));
                tank.setOnAddedTooltips((w, tooltips) -> {
                    var chance = content.chance;
                    if (chance < 1) {
                        tooltips.add(chance == 0 ?
                                Component.translatable("gtceu.gui.content.chance_0") :
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_1", chance * 100));
                        if (content.tierChanceBoost > 0) {
                            tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.tier_boost", content.tierChanceBoost * 100));
                        }
                    }
                    if (index >= recipe.getInputContents(FluidRecipeCapability.CAP).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        });
        // bind fluid out overlay
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(IO.OUT)), TankWidget.class, tank -> {
            var index = WidgetUtils.widgetIdIndex(tank);
            if (index >= 0 && index < outputFluidContents.size()) {
                var content = outputFluidContents.get(index);
                tank.setXEIChance(content.chance);
                tank.setOverlay(content.createOverlay(index >= recipe.getOutputContents(FluidRecipeCapability.CAP).size()));
                tank.setOnAddedTooltips((w, tooltips) -> {
                    var chance = content.chance;
                    if (chance < 1) {
                        tooltips.add(chance == 0 ?
                                Component.translatable("gtceu.gui.content.chance_0") :
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_1", chance * 100));
                        if (content.tierChanceBoost > 0) {
                            tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.tier_boost", content.tierChanceBoost * 100));
                        }
                    }
                    if (index >= recipe.getOutputContents(FluidRecipeCapability.CAP).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        });
        var size = group.getSize();
        addWidget(group);

        int yOffset = 5 + size.height;
        addWidget(new LabelWidget(3, yOffset,
                LocalizationUtils.format("gtceu.recipe.duration", recipe.duration / 20f)));
        var EUt = RecipeHelper.getInputEUt(recipe);
        boolean isOutput = false;
        if (EUt == 0) {
            EUt = RecipeHelper.getOutputEUt(recipe);
            isOutput = true;
        }
        if (EUt > 0) {
            long euTotal = EUt * recipe.duration;
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (recipe.inputs.containsKey(CWURecipeCapability.CAP) && recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.min(recipe.inputs.get(CWURecipeCapability.CAP).stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                addWidget(new LabelWidget(3, yOffset += LINE_HEIGHT, LocalizationUtils.format("gtceu.recipe.max_eu", euTotal / minimumCWUt)));
            } else {
                addWidget(new LabelWidget(3, yOffset += LINE_HEIGHT,
                    LocalizationUtils.format("gtceu.recipe.total", euTotal)));
            }

            addWidget(new LabelWidget(3, yOffset += LINE_HEIGHT,
                LocalizationUtils.format(!isOutput ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted", EUt, GTValues.VN[GTUtil.getTierByVoltage(EUt)])));
        }
        /// add text based on i/o's
        MutableInt yOff = new MutableInt(yOffset);
        for (var capability : recipe.inputs.entrySet()) {
            capability.getKey().addXEIInfo(this, capability.getValue(), false, true, yOff);
        }
        for (var capability : recipe.tickInputs.entrySet()) {
            capability.getKey().addXEIInfo(this, capability.getValue(), true, true, yOff);
        }
        for (var capability : recipe.outputs.entrySet()) {
            capability.getKey().addXEIInfo(this, capability.getValue(), false, false, yOff);
        }
        for (var capability : recipe.tickOutputs.entrySet()) {
            capability.getKey().addXEIInfo(this, capability.getValue(), true, false, yOff);
        }

        yOffset = yOff.getValue();
        for (RecipeCondition condition : recipe.conditions) {
            if (condition.getTooltips() == null) continue;
            addWidget(new LabelWidget(3, yOffset += LINE_HEIGHT, condition.getTooltips().getString()));
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            addWidget(new LabelWidget(3, yOffset += LINE_HEIGHT, dataInfo.apply(recipe.data)));
        }
        recipe.recipeType.getRecipeUI().appendJEIUI(recipe, this);

        // add recipe id getter
        addWidget(new PredicatedButtonWidget(getSize().width + 3,3, 15, 15, new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ID")), cd -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(recipe.id.toString());
        }, () -> CompassManager.INSTANCE.devMode).setHoverTooltips("click to copy: " + recipe.id));
    }
}
