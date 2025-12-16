package com.gregtechceu.gtceu.integration.xei.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLLoader;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class GTRecipeWidget extends WidgetGroup {

    public static final String RECIPE_CONTENT_GROUP_ID = "recipeContentGroup";
    public static final Pattern RECIPE_CONTENT_GROUP_ID_REGEX = Pattern.compile("^recipeContentGroup$");

    public static final int LINE_HEIGHT = 10;

    private final int xOffset;
    private final GTRecipe recipe;
    private final List<LabelWidget> recipeParaTexts = new ArrayList<>();
    private LabelWidget recipeVoltageText = null;
    private final int minTier;
    private int tier;
    private int yOffset;
    private LabelWidget voltageTextWidget;

    public GTRecipeWidget(GTRecipe recipe) {
        super(getXOffset(recipe), 0, recipe.recipeType.getRecipeUI().getJEISize().width,
                recipe.recipeType.getRecipeUI().getJEISize().height);
        this.recipe = recipe;
        this.xOffset = getXOffset(recipe);
        this.minTier = RecipeHelper.getRecipeEUtTier(recipe);
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

        // Ensure any previous instances of the widget are removed first. This applies when changing the recipe
        // preview's voltage tier, as this recipe widget stays the same while its contents are updated.
        group.setId(RECIPE_CONTENT_GROUP_ID);
        getWidgetsById(RECIPE_CONTENT_GROUP_ID_REGEX).forEach(this::removeWidget);

        addWidget(group);

        EnergyStack EUt = RecipeHelper.getRealEUt(recipe);
        int yOffset = 5 + size.height;
        this.yOffset = yOffset;
        yOffset += !EUt.isEmpty() ? 21 : 0;
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

        for (RecipeCondition condition : recipe.conditions) {
            if (condition.getTooltips() == null) continue;
            if (condition instanceof DimensionCondition dimCondition) {
                addWidget(dimCondition
                        .setupDimensionMarkers(recipe.recipeType.getRecipeUI().getJEISize().width - xOffset - 44,
                                recipe.recipeType.getRecipeUI().getJEISize().height - 32)
                        .setBackgroundTexture(IGuiTexture.EMPTY));
            } else addWidget(new LabelWidget(3 - xOffset, yOffset += LINE_HEIGHT, condition.getTooltips().getString()));
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
        var EUt = RecipeHelper.getRealEUtWithIO(recipe);
        var minVoltageTier = GTUtil.getTierByVoltage(EUt.voltage());
        float minAmperage = (float) EUt.getTotalEU() / GTValues.V[minVoltageTier];

        List<Component> texts = getRecipeParaText(recipe, duration, EUt);
        for (Component text : texts) {
            textsY += 10;
            LabelWidget labelWidget = new LabelWidget(3 - xOffset, textsY, text).setTextColor(-1).setDropShadow(true);
            addWidget(labelWidget);
            recipeParaTexts.add(labelWidget);
        }

        if (EUt.voltage() > 0) {
            textsY += 10;
            Component text = Component.translatable(EUt.isInput() ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                    FormattingUtil.formatNumber2Places(minAmperage), GTValues.VN[minVoltageTier])
                    .withStyle(ChatFormatting.UNDERLINE);
            recipeVoltageText = new LabelWidget(3 - xOffset, textsY, text).setTextColor(-1)
                    .setDropShadow(true);
            recipeVoltageText.setHoverTooltips(
                    Component.translatable("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(EUt.getTotalEU()))
                            .withStyle(ChatFormatting.UNDERLINE));
            if (recipeVoltageText != null) {
                addWidget(recipeVoltageText);
            }
        }

        if (EUt.isInput()) {
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
                    .setHoverTooltips(LangHandler.getMultiLang("gtceu.oc.tooltip", GTValues.VNF[minTier])
                            .toArray(Component[]::new)));
            addWidget(this.voltageTextWidget = voltageTextWidget);
        }
    }

    @NotNull
    private static List<Component> getRecipeParaText(GTRecipe recipe, int duration,
                                                     EnergyStack.WithIO eu) {
        List<Component> texts = new ArrayList<>();
        if (!recipe.data.getBoolean("hide_duration")) {
            texts.add(Component.translatable("gtceu.recipe.duration", FormattingUtil.formatNumbers(duration / 20f)));
        }
        if (eu.voltage() > 0) {
            long euTotal = eu.getTotalEU() * duration;
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (recipe.data.getBoolean("duration_is_total_cwu") &&
                    recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.max(recipe.tickInputs.get(CWURecipeCapability.CAP).stream()
                        .map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                texts.add(Component.translatable("gtceu.recipe.max_eu",
                        FormattingUtil.formatNumbers(euTotal / minimumCWUt)));
            } else {
                texts.add(Component.translatable("gtceu.recipe.total", FormattingUtil.formatNumbers(euTotal)));
            }
        }

        return texts;
    }

    private void addButtons() {
        // add a recipe id getter, btw all the things can only click within the WidgetGroup while using EMI
        int x = getSize().width - xOffset - 18;
        int y = getSize().height - 30;
        addWidget(
                new PredicatedButtonWidget(x, y, 15, 15, new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ID")),
                        cd -> Minecraft.getInstance().keyboardHandler.setClipboard(recipe.id.toString()),
                        () -> !FMLLoader.isProduction(), !FMLLoader.isProduction())
                        .setHoverTooltips("click to copy: " + recipe.id));
    }

    private int getVoltageXOffset() {
        int x = getSize().width - switch (tier) {
            case ULV, LuV, ZPM, UHV, UEV, UXV -> 20;
            case OpV, MAX -> 22;
            case UIV -> 18;
            case IV -> 12;
            default -> 14;
        };
        if (!GTCEu.Mods.isEMILoaded()) {
            x -= 3;
        }
        return x;
    }

    public void setRecipeOC(int button, boolean isShiftClick) {
        OverclockingLogic oc = OverclockingLogic.NON_PERFECT_OVERCLOCK;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setTier(tier + 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setTier(tier - 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setTierToMin();
        }
        if (isShiftClick) {
            oc = OverclockingLogic.PERFECT_OVERCLOCK;
        }
        if (recipe.recipeType == GTRecipeTypes.FUSION_RECIPES) {
            oc = FusionReactorMachine.FUSION_OC;
        }
        setRecipeOverclockWidget(oc);
        setRecipeWidget();
    }

    private void setRecipeOverclockWidget(OverclockingLogic logic) {
        EnergyStack inputEUt = recipe.getInputEUt();
        int duration = recipe.duration;
        String tierText = GTValues.VNF[tier];

        if (tier > minTier && !inputEUt.isEmpty()) {
            int ocs = tier - minTier;
            if (minTier == ULV) ocs--;
            var params = new OverclockingLogic.OCParams(inputEUt.voltage(), recipe.duration, ocs, 1);
            var result = logic.runOverclockingLogic(params, V[tier]);
            duration = (int) (duration * result.durationMultiplier());
            inputEUt = inputEUt.multiplyVoltage(result.eutMultiplier());
            tierText = tierText.formatted(ChatFormatting.ITALIC);
        }
        var minVoltageTier = GTUtil.getTierByVoltage(inputEUt.voltage());
        float minAmperage = (float) inputEUt.getTotalEU() / GTValues.V[minVoltageTier];
        List<Component> texts = getRecipeParaText(recipe, duration, new EnergyStack.WithIO(inputEUt, IO.IN));
        for (int i = 0; i < texts.size(); i++) {
            recipeParaTexts.get(i).setComponent(texts.get(i));
        }
        voltageTextWidget.setText(tierText);
        voltageTextWidget.setSelfPositionX(getVoltageXOffset() - xOffset);
        if (recipeVoltageText != null) {
            recipeVoltageText.setComponent(Component.translatable("gtceu.recipe.eu",
                    FormattingUtil.formatNumber2Places(minAmperage), GTValues.VN[minVoltageTier])
                    .withStyle(ChatFormatting.UNDERLINE));
            recipeVoltageText.setHoverTooltips(
                    Component.translatable("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(inputEUt.getTotalEU()))
                            .withStyle(ChatFormatting.UNDERLINE));
        }
        detectAndSendChanges();
        updateScreen();
    }

    public static void setConsumedChance(Content content, ChanceLogic logic, List<Component> tooltips, int recipeTier,
                                         int chanceTier, ChanceBoostFunction function) {
        if (content.chance < ChanceLogic.getMaxChancedValue()) {
            int boostedChance = function.getBoostedChance(content, recipeTier, chanceTier);
            if (boostedChance == 0) {
                tooltips.add(Component.translatable("gtceu.gui.content.chance_nc"));
            } else {
                float baseChanceFloat = 100f * content.chance / content.maxChance;
                if (content.tierChanceBoost != 0) {
                    float boostedChanceFloat = 100f * boostedChance / content.maxChance;

                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltips.add(Component.translatable("gtceu.gui.content.chance_base_logic",
                                FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltips.add(
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_base",
                                        baseChanceFloat));
                    }

                    String key = "gtceu.gui.content.chance_tier_boost_" +
                            ((content.tierChanceBoost > 0) ? "plus" : "minus");
                    tooltips.add(FormattingUtil.formatPercentage2Places(key,
                            Math.abs(100f * content.tierChanceBoost / content.maxChance)));

                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltips.add(Component.translatable("gtceu.gui.content.chance_boosted_logic",
                                FormattingUtil.formatNumber2Places(boostedChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltips.add(
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_boosted",
                                        boostedChanceFloat));
                    }
                } else {
                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltips.add(Component.translatable("gtceu.gui.content.chance_no_boost_logic",
                                FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltips.add(
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_no_boost",
                                        baseChanceFloat));
                    }
                }
            }
        }
    }

    private void setTier(int tier) {
        this.tier = Mth.clamp(tier, minTier, GTValues.MAX);
    }

    private void setTierToMin() {
        setTier(minTier);
    }

    public void collectStorage(Table<IO, RecipeCapability<?>, Object> extraTable,
                               Table<IO, RecipeCapability<?>, List<Content>> extraContents, GTRecipe recipe) {
        for (var entry : recipe.inputs.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contents = entry.getValue();

            extraContents.put(IO.IN, cap, contents);
        }
        for (var entry : recipe.tickInputs.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contents = entry.getValue();

            if (extraContents.get(IO.IN, cap) == null) {
                extraContents.put(IO.IN, cap, contents);
            } else {
                ArrayList<Content> fullContents = new ArrayList<>(extraContents.get(IO.IN, cap));
                fullContents.addAll(contents);
                extraContents.put(IO.IN, cap, fullContents);
            }
        }
        if (extraContents.containsRow(IO.IN)) {
            Map<RecipeCapability<?>, List<Object>> inputCapabilities = new Object2ObjectLinkedOpenHashMap<>();
            for (var entry : extraContents.row(IO.IN).entrySet()) {
                RecipeCapability<?> cap = entry.getKey();
                inputCapabilities.put(cap, cap.createXEIContainerContents(entry.getValue(), recipe, IO.IN));
            }

            for (var entry : inputCapabilities.entrySet()) {
                while (entry.getValue().size() < recipe.recipeType.getMaxInputs(entry.getKey()))
                    entry.getValue().add(null);
                var container = entry.getKey().createXEIContainer(entry.getValue());
                if (container != null) {
                    extraTable.put(IO.IN, entry.getKey(), container);
                }
            }
        }

        for (var entry : recipe.outputs.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contents = entry.getValue();

            extraContents.put(IO.OUT, cap, contents);
        }
        for (var entry : recipe.tickOutputs.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contents = entry.getValue();

            if (extraContents.get(IO.OUT, cap) == null) {
                extraContents.put(IO.OUT, cap, contents);
            } else {
                ArrayList<Content> fullContents = new ArrayList<>(extraContents.get(IO.IN, cap));
                fullContents.addAll(contents);
                extraContents.put(IO.OUT, cap, fullContents);
            }
        }
        if (extraContents.containsRow(IO.OUT)) {
            Map<RecipeCapability<?>, List<Object>> outputCapabilities = new Object2ObjectLinkedOpenHashMap<>();
            for (var entry : extraContents.row(IO.OUT).entrySet()) {
                RecipeCapability<?> cap = entry.getKey();
                outputCapabilities.put(cap, cap.createXEIContainerContents(entry.getValue(), recipe, IO.OUT));
            }
            for (var entry : outputCapabilities.entrySet()) {
                while (entry.getValue().size() < recipe.recipeType.getMaxOutputs(entry.getKey()))
                    entry.getValue().add(null);
                var container = entry.getKey().createXEIContainer(entry.getValue());
                if (container != null) {
                    extraTable.put(IO.OUT, entry.getKey(), container);
                }
            }
        }
    }

    public void addSlots(Table<IO, RecipeCapability<?>, List<Content>> contentTable, WidgetGroup group,
                         GTRecipe recipe) {
        for (var capabilityEntry : contentTable.rowMap().entrySet()) {
            IO io = capabilityEntry.getKey();
            for (var contentsEntry : capabilityEntry.getValue().entrySet()) {
                RecipeCapability<?> cap = contentsEntry.getKey();
                int nonTickCount = (io == IO.IN ? recipe.getInputContents(cap) : recipe.getOutputContents(cap)).size();
                List<Content> contents = contentsEntry.getValue();
                // bind fluid out overlay
                var widgetClass = cap.getWidgetClass();
                if (widgetClass != null) {
                    WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(cap.slotName(io)), widgetClass,
                            widget -> {
                                var index = WidgetUtils.widgetIdIndex(widget);
                                if (index >= 0 && index < contents.size()) {
                                    var content = contents.get(index);
                                    cap.applyWidgetInfo(widget, index, true, io, null, recipe.getType(), recipe,
                                            content,
                                            null, minTier, tier);
                                    widget.setOverlay(content.createOverlay(index >= nonTickCount, minTier, tier,
                                            recipe.getType().getChanceFunction()));
                                }
                            });
                }
            }
        }
    }
}
