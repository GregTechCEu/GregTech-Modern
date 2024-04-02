package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote GTRecipeWidget
 */
public class GTRecipeWidget extends WidgetGroup {

    public static final int LINE_HEIGHT = 10;

    private final int xOffset;
    private final GTRecipe recipe;
    private final List<LabelWidget> recipeParaTexts = new ArrayList<>();
    @Getter
    private int tier;
    @Getter
    private int yOffset;
    private LabelWidget voltageTextWidget;

    public GTRecipeWidget(GTRecipe recipe) {
        super(getXOffset(recipe), 0, recipe.recipeType.getRecipeUI().getJEISize().width,
                recipe.recipeType.getRecipeUI().getJEISize().height);
        this.recipe = recipe;
        this.xOffset = getXOffset(recipe);
        setRecipeWidget();
        setTierToMin();
        initializeRecipeTextWidget();
        addButtons();
    }

    private static int getXOffset(GTRecipe recipe) {
        if (recipe.recipeType.getRecipeUI().getOriginalWidth() != recipe.recipeType.getRecipeUI().getJEISize().width) {
            return (recipe.recipeType.getRecipeUI().getJEISize().width -
                    recipe.recipeType.getRecipeUI().getOriginalWidth()) / 2;
        }
        return 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void setRecipeWidget() {
        setClientSideWidget();

        var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
        var contents = Tables.newCustomTable(new EnumMap<>(IO.class),
                LinkedHashMap<RecipeCapability<?>, List<Content>>::new);
        collectStorage(storages, contents, recipe);

        WidgetGroup group = recipe.recipeType.getRecipeUI().createUITemplate(ProgressWidget.JEIProgress, storages,
                recipe.data.copy(), recipe.conditions);
        addSlots(contents, group, recipe);

        var size = group.getSize();
        addWidget(group);
        var EUt = RecipeHelper.getInputEUt(recipe);
        if (EUt == 0) {
            EUt = RecipeHelper.getOutputEUt(recipe);
        }
        int yOffset = 5 + size.height;
        this.yOffset = yOffset;
        yOffset += EUt > 0 ? 20 : 0;
        if (recipe.data.getBoolean("duration_is_total_cwu")) {
            yOffset -= 10;
        }

        /// add text based on i/o's
        MutableInt yOff = new MutableInt(yOffset);
        for (var capability : recipe.inputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, recipe, capability.getValue(), false, true, yOff);
        }
        for (var capability : recipe.tickInputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, recipe, capability.getValue(), true, true, yOff);
        }
        for (var capability : recipe.outputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, recipe, capability.getValue(), false, false, yOff);
        }
        for (var capability : recipe.tickOutputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, recipe, capability.getValue(), true, false, yOff);
        }

        yOffset = yOff.getValue();
        for (RecipeCondition condition : recipe.conditions) {
            if (condition.getTooltips() == null) continue;
            addWidget(new LabelWidget(3 - xOffset, yOffset += LINE_HEIGHT, condition.getTooltips().getString()));
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            addWidget(new LabelWidget(3 - xOffset, yOffset += LINE_HEIGHT, dataInfo.apply(recipe.data)));
        }
        recipe.recipeType.getRecipeUI().appendJEIUI(recipe, this);
    }

    private void initializeRecipeTextWidget() {
        String tierText = GTValues.VNF[tier];
        int textsY = yOffset - 10;
        int duration = recipe.duration;
        long inputEUt = RecipeHelper.getInputEUt(recipe);
        long outputEUt = RecipeHelper.getOutputEUt(recipe);
        List<Component> texts = getRecipeParaText(recipe, duration, inputEUt, outputEUt);
        for (Component text : texts) {
            textsY += 10;
            LabelWidget labelWidget = new LabelWidget(3 - xOffset, textsY, text).setTextColor(-1).setDropShadow(true);
            addWidget(labelWidget);
            recipeParaTexts.add(labelWidget);
        }
        if (inputEUt > 0) {
            LabelWidget voltageTextWidget = new LabelWidget(getVoltageXOffset() - xOffset, getSize().height - 10,
                    tierText).setTextColor(-1).setDropShadow(false);
            if (recipe.recipeType.isOffsetVoltageText()) {
                voltageTextWidget.setSelfPositionY(getSize().height - recipe.recipeType.getVoltageTextOffset());
            }
            // make it clickable
            // voltageTextWidget.setBackground(new GuiTextureGroup(GuiTextures.BUTTON));
            addWidget(new ButtonWidget(voltageTextWidget.getPositionX(), voltageTextWidget.getPositionY(),
                    voltageTextWidget.getSizeWidth(), voltageTextWidget.getSizeHeight(),
                    cd -> setRecipeOC(cd.button, cd.isShiftClick))
                    .setHoverTooltips(
                            Component.translatable("gtceu.oc.tooltip.0", GTValues.VNF[getMinTier()]),
                            Component.translatable("gtceu.oc.tooltip.1"),
                            Component.translatable("gtceu.oc.tooltip.2"),
                            Component.translatable("gtceu.oc.tooltip.3"),
                            Component.translatable("gtceu.oc.tooltip.4")));
            addWidget(this.voltageTextWidget = voltageTextWidget);
        }
    }

    @NotNull
    private static List<Component> getRecipeParaText(GTRecipe recipe, int duration, long inputEUt, long outputEUt) {
        List<Component> texts = new ArrayList<>();
        if (!recipe.data.getBoolean("hide_duration")) {
            texts.add(Component.translatable("gtceu.recipe.duration", FormattingUtil.formatNumbers(duration / 20f)));
        }
        var EUt = inputEUt;
        boolean isOutput = false;
        if (EUt == 0) {
            EUt = outputEUt;
            isOutput = true;
        }
        if (EUt > 0) {
            long euTotal = EUt * recipe.duration;
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (recipe.data.getBoolean("duration_is_total_cwu") &&
                    recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.min(recipe.tickInputs.get(CWURecipeCapability.CAP).stream()
                        .map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                texts.add(Component.translatable("gtceu.recipe.max_eu",
                        FormattingUtil.formatNumbers(euTotal / minimumCWUt)));
            } else {
                texts.add(Component.translatable("gtceu.recipe.total", FormattingUtil.formatNumbers(euTotal)));
            }
            texts.add(Component.translatable(!isOutput ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                    FormattingUtil.formatNumbers(EUt)));
        }

        // add recipe id getter
//        addWidget(new PredicatedButtonWidget(getSize().width + 3,3, 15, 15, new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ID")), cd -> {
//            Minecraft.getInstance().keyboardHandler.setClipboard(recipe.id.toString());
//        }, () -> CompassManager.INSTANCE.devMode).setHoverTooltips("click to copy: " + recipe.id));
    }
}
