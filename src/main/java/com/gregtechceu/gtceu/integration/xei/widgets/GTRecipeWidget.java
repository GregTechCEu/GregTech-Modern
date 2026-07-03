package com.gregtechceu.gtceu.integration.xei.widgets;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

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
import org.apache.commons.lang3.mutable.MutableInt;
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
    private final GTRecipeDefinition recipe;
    private final int minTier;
    private int tier;

    private LabelWidget voltageTextWidget;
    private OverclockingLogic oc = OverclockingLogic.NON_PERFECT_OVERCLOCK;

    public GTRecipeWidget(GTRecipeDefinition definition) {
        super(getXOffset(definition), 0, definition.recipeType.getRecipeUI().getJEISize().width,
                definition.recipeType.getRecipeUI().getJEISize().height);
        this.recipe = definition;
        this.xOffset = getXOffset(definition);
        this.minTier = definition.tier;
        this.tier = definition.tier;
        initialize();
        setRecipeWidget();
    }

    private static int getXOffset(GTRecipeDefinition definition) {
        if (definition.recipeType.getRecipeUI().getOriginalWidth() !=
                definition.recipeType.getRecipeUI().getJEISize().width) {
            return (definition.recipeType.getRecipeUI().getJEISize().width -
                    definition.recipeType.getRecipeUI().getOriginalWidth()) / 2;
        }
        return 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void setRecipeWidget() {
        setClientSideWidget();
        ContentListMap tickInputs = recipe.tickInputs;
        ContentListMap tickOutputs = recipe.tickOutputs;
        int duration = recipe.duration;
        int ocs = tier - minTier;
        if (minTier == ULV) ocs--;
        if (ocs != 0) {
            var params = new OverclockingLogic.OCParams(RecipeHelper.getRealEUtWithIO(recipe), duration, ocs, 1);
            var ocResult = oc.runOverclockingLogic(params, V[tier]);
            duration = (int) (duration * ocResult.durationMultiplier());
            tickInputs = tickInputs.copyWithMultiplier((int) ocResult.eutMultiplier());
            tickOutputs = tickOutputs.copyWithMultiplier((int) ocResult.eutMultiplier());
        }

        var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
        ContentListMap inputContents = recipe.inputs.copyAndAppend(tickInputs);
        ContentListMap outputContents = recipe.outputs.copyAndAppend(tickOutputs);
        collectStorage(storages, IO.IN, inputContents);
        collectStorage(storages, IO.OUT, outputContents);

        WidgetGroup group = recipe.recipeType.getRecipeUI().createUITemplate(ProgressWidget.JEIProgress, storages,
                recipe.data.copy(), recipe.conditions);
        if (voltageTextWidget != null) {
            group.addWidget(voltageTextWidget);
        }

        addSlots(group, IO.IN, inputContents);
        addSlots(group, IO.OUT, outputContents);

        var size = group.getSize();

        MutableInt yOff = new MutableInt(size.height - 5);

        if (!recipe.data.getBoolean("hide_duration")) {
            Component durationText = Component.translatable("gtceu.recipe.duration",
                    FormattingUtil.formatNumbers(duration / 20f));
            group.addWidget(new LabelWidget(3 - xOffset, yOff.addAndGet(LINE_HEIGHT), durationText)
                    .setTextColor(-1).setDropShadow(true));
        }

        /// add text based on i/o's
        addXEIInfo(group, recipe.inputs, duration, false, true, yOff);
        addXEIInfo(group, recipe.outputs, duration, false, false, yOff);
        addXEIInfo(group, tickInputs, duration, true, true, yOff);
        addXEIInfo(group, tickOutputs, duration, true, false, yOff);

        for (RecipeCondition condition : recipe.conditions) {
            if (condition.getTooltips() == null) continue;
            if (condition instanceof DimensionCondition dimCondition) {
                group.addWidget(dimCondition
                        .setupDimensionMarkers(recipe.recipeType.getRecipeUI().getJEISize().width - xOffset - 44,
                                recipe.recipeType.getRecipeUI().getJEISize().height - 32)
                        .setBackgroundTexture(IGuiTexture.EMPTY));
            } else group.addWidget(
                    new LabelWidget(3 - xOffset, yOff.addAndGet(LINE_HEIGHT), condition.getTooltips().getString()));
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            group.addWidget(new LabelWidget(3 - xOffset, yOff.addAndGet(LINE_HEIGHT), dataInfo.apply(recipe.data)));
        }

        // Ensure any previous instances of the widget are removed first. This applies when changing the recipe
        // preview's voltage tier, as this recipe widget stays the same while its contents are updated.
        group.setId(RECIPE_CONTENT_GROUP_ID);
        getWidgetsById(RECIPE_CONTENT_GROUP_ID_REGEX).forEach(this::removeWidget);

        addWidget(group);

        recipe.recipeType.getRecipeUI().appendJEIUI(recipe, this);
    }

    private void addXEIInfo(WidgetGroup group, ContentListMap contents, int duration, boolean perTick, boolean isInput,
                            MutableInt yOff) {
        contents.forEachEntry(new ContentListMap.EntryConsumer() {

            @Override
            public <T> void accept(
                                   RecipeCapability<T> capability,
                                   List<T> contents) {
                capability.addXEIInfo(group, xOffset, recipe, contents, duration, perTick, isInput, yOff);
            }
        });
    }

    private void initialize() {
        var EUt = RecipeHelper.getRealEUtWithIO(recipe);
        if (tier != 0 || EUt != 0) {
            String tierText = GTValues.VNF[tier];
            if (tier != minTier) {
                tierText = tierText.formatted(ChatFormatting.ITALIC);
            }
            voltageTextWidget = new LabelWidget(getVoltageXOffset() - xOffset, getSize().height - 10,
                    tierText).setTextColor(-1).setDropShadow(false);
            if (recipe.recipeType.isOverclockable() && EUt >= 0) {// to filter generator recipes
                addWidget(new ButtonWidget(voltageTextWidget.getPositionX(), voltageTextWidget.getPositionY(),
                        voltageTextWidget.getSizeWidth(), voltageTextWidget.getSizeHeight(),
                        cd -> setRecipeOC(cd.button, cd.isShiftClick))
                        .setHoverTooltips(LangHandler.getMultiLang("gtceu.oc.tooltip", GTValues.VNF[minTier])
                                .toArray(Component[]::new)));
            }
            addWidget(new VoltageBorderWidget(-xOffset, 0, getSizeWidth(), getSizeHeight(), VCM[tier]));
        }
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

        return x;
    }

    public void setRecipeOC(int button, boolean isShiftClick) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setTier(tier + 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setTier(tier - 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            tier = minTier;
        }
        oc = isShiftClick ? OverclockingLogic.PERFECT_OVERCLOCK : OverclockingLogic.NON_PERFECT_OVERCLOCK;
        if (recipe.recipeType == GTRecipeTypes.FUSION_RECIPES) {
            oc = FusionReactorMachine.FUSION_OC;
        }
        String tierText = GTValues.VNF[tier];
        if (tier != minTier) {
            tierText = tierText.formatted(ChatFormatting.ITALIC);
        }
        voltageTextWidget = new LabelWidget(getVoltageXOffset() - xOffset, getSize().height - 10,
                tierText).setTextColor(-1).setDropShadow(false);

        setRecipeWidget();
    }

    private void setTier(int tier) {
        this.tier = Mth.clamp(tier, minTier, GTValues.MAX);
    }

    private void collectStorage(Table<IO, RecipeCapability<?>, Object> storages, IO io, ContentListMap contents) {
        contents.forEachEntry(new ContentListMap.EntryConsumer() {

            @Override
            public <T> void accept(RecipeCapability<T> capability, List<T> list) {
                List<?> xeiContents = capability.createXEIContainerContents(list, recipe, io);
                int maxContents = io == IO.IN ? recipe.recipeType.getMaxInputs(capability) :
                        recipe.recipeType.getMaxOutputs(capability);
                while (xeiContents.size() < maxContents) {
                    xeiContents.add(null);
                }
                Object container = capability.createXEIContainer(xeiContents);
                if (container != null) {
                    storages.put(io, capability, container);
                }
            }
        });
    }

    private void addSlots(WidgetGroup group, IO io, ContentListMap contents) {
        contents.forEachEntry(new ContentListMap.EntryConsumer() {

            @Override
            public <T> void accept(RecipeCapability<T> capability, List<T> list) {
                int nonTickCount = (io == IO.IN ? recipe.getInputContents(capability) :
                        recipe.getOutputContents(capability)).size();
                var widgetClass = capability.getWidgetClass();
                if (widgetClass == null) return;
                WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(capability.slotName(io)), widgetClass,
                        widget -> {
                            int index = WidgetUtils.widgetIdIndex(widget);
                            if (index < 0 || index >= list.size()) return;
                            capability.applyWidgetInfo(widget, index, true, io, null, recipe.getType(), recipe,
                                    list.get(index), null, minTier, tier);
                            widget.setOverlay(capability.createXEIOverlay(list.get(index), index >= nonTickCount));
                        });
            }
        });
    }

    public static WidgetGroup getPlaceHolder(GTRecipeDefinition definition) {
        return new WidgetGroup(getXOffset(definition), 0,
                definition.recipeType.getRecipeUI().getJEISize().width,
                definition.recipeType.getRecipeUI().getJEISize().height);
    }
}
