package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.CycleFluidStorage;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.IV;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote GTRecipeWidget
 */
public class GTRecipeWidget extends WidgetGroup {
    private final int xOffset;
    private final GTRecipe recipe;
    private final List<LabelWidget> recipeParaTexts = new ArrayList<>();
    @Getter
    private int tier;
    @Getter
    private int yOffset;
    private LabelWidget voltageTextWidget;

    public GTRecipeWidget(GTRecipe recipe) {
        super(getXOffSet(recipe), 0, recipe.recipeType.getRecipeUI().getJEISize().width, recipe.recipeType.getRecipeUI().getJEISize().height);
        this.recipe = recipe;
        this.xOffset = getXOffSet(recipe);
        setRecipeWidget();
        setTierToMin();
        initializeRecipeTextWidget();
        addButtons();
    }

    private static int getXOffSet(GTRecipe recipe) {
        if (recipe.recipeType.getRecipeUI().getOriginalWidth() != recipe.recipeType.getRecipeUI().getJEISize().width) {
            return (recipe.recipeType.getRecipeUI().getJEISize().width - recipe.recipeType.getRecipeUI().getOriginalWidth()) / 2;
        }
        return 0;
    }

    private void setRecipeWidget() {
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
            new CycleFluidStorage(outputFluids)
        );
        // bind item in overlay
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.IN)), SlotWidget.class, slot -> {
            var index = WidgetUtils.widgetIdIndex(slot);
            if (index >= 0 && index < inputStackContents.size()) {
                var content = inputStackContents.get(index);
                slot.setXEIChance(content.chance);
                slot.setOverlay(content.createOverlay(index >= recipe.getInputContents(ItemRecipeCapability.CAP).size()));
                slot.setOnAddedTooltips((w, tooltips) -> {
                    setConsumedChance(content, tooltips);
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
                    setConsumedChance(content, tooltips);
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
                    setConsumedChance(content, tooltips);
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
                    setConsumedChance(content, tooltips);
                    if (index >= recipe.getOutputContents(FluidRecipeCapability.CAP).size()) {
                        tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
                    }
                });
            }
        });
        var size = group.getSize();
        addWidget(group);
        var EUt = RecipeHelper.getInputEUt(recipe);
        if (EUt == 0) {
            EUt = RecipeHelper.getOutputEUt(recipe);
        }
        int yOffset = 5 + size.height;
        this.yOffset = yOffset;
        yOffset += EUt > 0 ? 20 : 0;
        for (RecipeCondition condition : recipe.conditions) {
            if (condition.getTooltips() == null) continue;
            addWidget(new LabelWidget(3 - xOffset, yOffset += 10, condition.getTooltips().getString()));
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            addWidget(new LabelWidget(3 - xOffset, yOffset += 10, dataInfo.apply(recipe.data)));
        }
        recipe.recipeType.getRecipeUI().appendJEIUI(recipe, this);
    }

    private void initializeRecipeTextWidget() {
        String tierText = GTValues.VNF[tier];
        int textsY = yOffset - 10;
        int duration = recipe.duration;
        long inputEUt = RecipeHelper.getInputEUt(recipe);
        long outputEUt = RecipeHelper.getOutputEUt(recipe);
        List<Component> texts = getRecipeParaText(duration, inputEUt, outputEUt);
        for (Component text : texts) {
            textsY += 10;
            LabelWidget labelWidget = new LabelWidget(3 - xOffset, textsY, text).setTextColor(-1).setDropShadow(true);
            addWidget(labelWidget);
            recipeParaTexts.add(labelWidget);
        }
        if (inputEUt != 0) {
            LabelWidget voltageTextWidget = new LabelWidget(getVoltageXOffset() - xOffset, getSize().height - 10, tierText).setTextColor(-1).setDropShadow(false);
            if (recipe.recipeType == GTRecipeTypes.FUSION_RECIPES || recipe.recipeType == GTRecipeTypes.GAS_COLLECTOR_RECIPES) {
                voltageTextWidget.setSelfPositionY(getSize().height - 20);
            }
            // make it clickable
            // voltageTextWidget.setBackground(new GuiTextureGroup(GuiTextures.BUTTON));
            addWidget(new ButtonWidget(voltageTextWidget.getPositionX(), voltageTextWidget.getPositionY(),
                voltageTextWidget.getSizeWidth(), voltageTextWidget.getSizeHeight(), cd -> setRecipeOC(cd.button))
                .setHoverTooltips(
                    Component.translatable("gtceu.oc.tooltip.0", GTValues.VNF[getMinTier()]),
                    Component.translatable("gtceu.oc.tooltip.1"),
                    Component.translatable("gtceu.oc.tooltip.2"),
                    Component.translatable("gtceu.oc.tooltip.3")
                ));
            addWidget(this.voltageTextWidget = voltageTextWidget);
        }
    }

    @NotNull
    private static List<Component> getRecipeParaText(int duration, long inputEUt, long outputEUt) {
        List<Component> texts = new ArrayList<>();
        texts.add(Component.literal(LocalizationUtils.format("gtceu.recipe.duration", duration / 20f)));
        if (inputEUt != 0) {
            texts.add(Component.literal(LocalizationUtils.format("gtceu.recipe.eu", inputEUt)));
            texts.add(Component.literal(LocalizationUtils.format("gtceu.recipe.total", (inputEUt * duration))));
        }
        if (outputEUt != 0) {
            texts.add(Component.literal(LocalizationUtils.format("gtceu.recipe.eu_inverted", outputEUt)));
            texts.add(Component.literal(LocalizationUtils.format("gtceu.recipe.total", (outputEUt * duration))));
        }
        return texts;
    }

    private void addButtons() {
        // add a recipe id getter, btw all the things can only click within the WidgetGroup while using EMI
        int x = getSize().width + 3 - this.xOffset, y = 3;
        if (LDLib.isEmiLoaded()) {
            x = getSize().width - xOffset - 18;
            y = getSize().height - 30;
        }
        addWidget(new PredicatedButtonWidget(x, y, 15, 15, new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ID")), cd ->
            Minecraft.getInstance().keyboardHandler.setClipboard(recipe.id.toString()), () -> CompassManager.INSTANCE.devMode, CompassManager.INSTANCE.devMode).setHoverTooltips("click to copy: " + recipe.id));
    }

    private int getVoltageXOffset() {
        int x = getSize().width - switch (tier) {
            case ULV, LuV, ZPM, UHV, UEV, UXV -> 20;
            case OpV, MAX -> 22;
            case UIV -> 18;
            case IV -> 12;
            default -> 14;
        };
        if (!LDLib.isEmiLoaded()) {
            x -= 3;
        }
        return x;
    }

    public void setRecipeOC(int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setTier(tier + 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setTier(tier - 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setTierToMin();
        }
        setRecipeTextWidget();
    }

    private void setRecipeTextWidget() {
        long inputEUt = RecipeHelper.getInputEUt(recipe);
        int duration = recipe.duration;
        String tierText = GTValues.VNF[tier];
        if (tier > getMinTier() && inputEUt != 0) {
            LongIntPair pair = OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic().runOverclockingLogic(
                recipe, inputEUt, GTValues.V[tier], duration, GTValues.MAX);
            duration = pair.rightInt();
            inputEUt = pair.firstLong();
            tierText = tierText.formatted(ChatFormatting.ITALIC);
        }
        List<Component> texts = getRecipeParaText(duration, inputEUt, 0);
        for (int i = 0; i < texts.size(); i++) {
            recipeParaTexts.get(i).setComponent(texts.get(i));
        }
        voltageTextWidget.setText(tierText);
        voltageTextWidget.setSelfPositionX(getVoltageXOffset() - xOffset);
        detectAndSendChanges();
        updateScreen();
    }

    private void setConsumedChance(Content content, List<Component> tooltips) {
        var chance = content.chance;
        if (chance < 1) {
            tooltips.add(chance == 0 ?
                Component.translatable("gtceu.gui.content.chance_0") :
                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_1", chance * 100));
            if (content.tierChanceBoost != 0) {
                tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.tier_boost", content.tierChanceBoost * 100));
            }
        }
    }

    private int getMinTier() {
        return RecipeHelper.getRecipeEUtTier(recipe);
    }

    private void setTier(int tier) {
        this.tier = Mth.clamp(tier, getMinTier(), GTValues.MAX);
    }

    private void setTierToMin() {
        setTier(getMinTier());
    }
}
