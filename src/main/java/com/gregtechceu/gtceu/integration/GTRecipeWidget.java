package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.IntersectionIngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.TagOrCycleFluidTransfer;
import com.lowdragmc.lowdraglib.utils.TagOrCycleItemStackTransfer;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        super(getXOffset(recipe), 0, recipe.recipeType.getRecipeUI().getJEISize().width, recipe.recipeType.getRecipeUI().getJEISize().height);
        this.recipe = recipe;
        this.xOffset = getXOffset(recipe);
        setRecipeWidget();
        setTierToMin();
        initializeRecipeTextWidget();
        addButtons();
    }

    private static int getXOffset(GTRecipe recipe) {
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
        List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> inputStacks = inputStackContents.stream().map(content -> content.content)
            .map(ItemRecipeCapability.CAP::of)
            .map(GTRecipeWidget::mapItem)
            .collect(Collectors.toList());
        while (inputStacks.size() < recipe.recipeType.getMaxInputs(ItemRecipeCapability.CAP)) inputStacks.add(null);

        List<Content> outputStackContents = new ArrayList<>();
        outputStackContents.addAll(recipe.getOutputContents(ItemRecipeCapability.CAP));
        outputStackContents.addAll(recipe.getTickOutputContents(ItemRecipeCapability.CAP));
        List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> outputStacks = outputStackContents.stream().map(content -> content.content)
            .map(ItemRecipeCapability.CAP::of)
            .map(GTRecipeWidget::mapItem)
            .collect(Collectors.toList());
        while (outputStacks.size() < recipe.recipeType.getMaxOutputs(ItemRecipeCapability.CAP)) outputStacks.add(null);


        List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> scannerPossibilities = null;
        if (recipe.recipeType.isScanner()) {
            scannerPossibilities = new ArrayList<>();
            // Scanner Output replacing, used for cycling research outputs
            Pair<GTRecipeType, String> researchData = null;
            for (Content stack : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                researchData = ResearchManager.readResearchId(ItemRecipeCapability.CAP.of(stack.content).getItems()[0]);
                if (researchData != null) break;
            }
            if (researchData != null) {
                Collection<GTRecipe> possibleRecipes = researchData.getFirst().getDataStickEntry(researchData.getSecond());
                if (possibleRecipes != null) {
                    for (GTRecipe r : possibleRecipes) {
                        ItemStack researchItem = ItemRecipeCapability.CAP.of(r.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                        researchItem = researchItem.copy();
                        researchItem.setCount(1);
                        boolean didMatch = false;
                        for (Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> stacks : scannerPossibilities) {
                            for (ItemStack stack : stacks.map(
                                tag -> tag
                                    .stream()
                                    .flatMap(key -> BuiltInRegistries.ITEM.getTag(key.getFirst()).stream())
                                    .flatMap(holders -> holders.stream().map(holder -> new ItemStack(holder.get())))
                                    .collect(Collectors.toList()),
                                Function.identity())) {
                                if (ItemStack.isSameItem(stack, researchItem)) {
                                    didMatch = true;
                                    break;
                                }
                            }
                        }
                        if (!didMatch) scannerPossibilities.add(Either.right(List.of(researchItem)));
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
        List<Either<List<Pair<TagKey<Fluid>, Long>>, List<FluidStack>>> inputFluids = inputFluidContents.stream().map(content -> content.content)
            .map(FluidRecipeCapability.CAP::of)
            .map(GTRecipeWidget::mapFluid)
            .collect(Collectors.toList());
        while (inputFluids.size() < recipe.recipeType.getMaxInputs(FluidRecipeCapability.CAP)) inputFluids.add(null);

        List<Content> outputFluidContents = new ArrayList<>();
        outputFluidContents.addAll(recipe.getOutputContents(FluidRecipeCapability.CAP));
        outputFluidContents.addAll(recipe.getTickOutputContents(FluidRecipeCapability.CAP));
        List<Either<List<Pair<TagKey<Fluid>, Long>>, List<FluidStack>>> outputFluids = outputFluidContents.stream().map(content -> content.content)
            .map(FluidRecipeCapability.CAP::of)
            .map(GTRecipeWidget::mapFluid)
            .collect(Collectors.toList());
        while (outputFluids.size() < recipe.recipeType.getMaxOutputs(FluidRecipeCapability.CAP)) outputFluids.add(null);

        WidgetGroup group = recipe.recipeType.getRecipeUI().createUITemplate(ProgressWidget.JEIProgress,
            new TagOrCycleItemStackTransfer(inputStacks),
            new TagOrCycleItemStackTransfer(outputStacks),
            new TagOrCycleFluidTransfer(inputFluids),
            new TagOrCycleFluidTransfer(outputFluids),
            recipe.data.copy(),
            recipe.conditions
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
        if (recipe.data.getBoolean("duration_is_total_cwu")) {
            yOffset -= 10;
        }

        /// add text based on i/o's
        MutableInt yOff = new MutableInt(yOffset);
        for (var capability : recipe.inputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, capability.getValue(), false, true, yOff);
        }
        for (var capability : recipe.tickInputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, capability.getValue(), true, true, yOff);
        }
        for (var capability : recipe.outputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, capability.getValue(), false, false, yOff);
        }
        for (var capability : recipe.tickOutputs.entrySet()) {
            capability.getKey().addXEIInfo(this, xOffset, capability.getValue(), true, false, yOff);
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
            LabelWidget voltageTextWidget = new LabelWidget(getVoltageXOffset() - xOffset, getSize().height - 10, tierText).setTextColor(-1).setDropShadow(false);
            if (recipe.recipeType.isOffsetVoltageText()) {
                voltageTextWidget.setSelfPositionY(getSize().height - recipe.recipeType.getVoltageTextOffset());
            }
            // make it clickable
            // voltageTextWidget.setBackground(new GuiTextureGroup(GuiTextures.BUTTON));
            addWidget(new ButtonWidget(voltageTextWidget.getPositionX(), voltageTextWidget.getPositionY(),
                voltageTextWidget.getSizeWidth(), voltageTextWidget.getSizeHeight(), cd -> setRecipeOC(cd.button, cd.isShiftClick))
                .setHoverTooltips(
                    Component.translatable("gtceu.oc.tooltip.0", GTValues.VNF[getMinTier()]),
                    Component.translatable("gtceu.oc.tooltip.1"),
                    Component.translatable("gtceu.oc.tooltip.2"),
                    Component.translatable("gtceu.oc.tooltip.3"),
                    Component.translatable("gtceu.oc.tooltip.4")
                ));
            addWidget(this.voltageTextWidget = voltageTextWidget);
        }
    }

    @NotNull
    private static List<Component> getRecipeParaText(GTRecipe recipe, int duration, long inputEUt, long outputEUt) {
        List<Component> texts = new ArrayList<>();
        if (!recipe.data.getBoolean("hide_duration")) {
            texts.add(Component.translatable("gtceu.recipe.duration", duration / 20f));
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
            if (recipe.data.getBoolean("duration_is_total_cwu") && recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.min(recipe.tickInputs.get(CWURecipeCapability.CAP).stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                texts.add(Component.translatable("gtceu.recipe.max_eu", euTotal / minimumCWUt));
            } else {
                texts.add(Component.translatable("gtceu.recipe.total", euTotal));
            }
            texts.add(Component.translatable(!isOutput ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted", EUt));
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
        setRecipeTextWidget(oc);
    }

    private void setRecipeTextWidget(OverclockingLogic logic) {
        long inputEUt = RecipeHelper.getInputEUt(recipe);
        int duration = recipe.duration;
        String tierText = GTValues.VNF[tier];
        if (tier > getMinTier() && inputEUt != 0) {
            LongIntPair pair = logic.getLogic().runOverclockingLogic(
                recipe, inputEUt, GTValues.V[tier], duration, GTValues.MAX);
            duration = pair.rightInt();
            inputEUt = pair.firstLong();
            tierText = tierText.formatted(ChatFormatting.ITALIC);
        }
        List<Component> texts = getRecipeParaText(recipe, duration, inputEUt, 0);
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

    // Maps ingredients to Either <(Tag with count), ItemStack>s
    @SuppressWarnings("deprecation")
    private static Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> mapItem(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            final int amount = sizedIngredient.getAmount();
             if (sizedIngredient.getInner() instanceof IntersectionIngredient intersection) {
                List<Ingredient> children = ((IntersectionIngredientAccessor)intersection).getChildren();
                if (children.isEmpty()) {
                    return Either.right(null);
                }
                var childEither = mapItem(children.get(0));
                return Either.right(childEither.map(tags -> {
                    List<ItemStack> tagItems = tags.stream()
                        .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(), pair.getSecond()))
                        .flatMap(pair -> pair.getFirst().flatMap(tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
                        .collect(Collectors.toList());
                    ListIterator<ItemStack> iterator = tagItems.listIterator();
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                break;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return tagItems;
                }, items -> {
                    items = new ArrayList<>(items);
                    ListIterator<ItemStack> iterator = items.listIterator();
                    while (iterator.hasNext()) {
                        var item = iterator.next();
                        for (int i = 1; i < children.size(); ++i) {
                            if (!children.get(i).test(item)) {
                                iterator.remove();
                                break;
                            }
                        }
                        iterator.set(item.copyWithCount(amount));
                    }
                    return items;
                }));
            } else if (((IngredientAccessor)sizedIngredient.getInner()).getValues().length > 0 && ((IngredientAccessor)sizedIngredient.getInner()).getValues()[0] instanceof Ingredient.TagValue tagValue) {
                return Either.left(List.of(Pair.of(((TagValueAccessor)tagValue).getTag(), amount)));
            }
        } else if (ingredient instanceof IntersectionIngredient intersection) {
            // Map intersection ingredients to the items inside, as recipe viewers don't support them.
            List<Ingredient> children = ((IntersectionIngredientAccessor)intersection).getChildren();
            if (children.isEmpty()) {
                return Either.right(null);
            }
            var childEither = mapItem(children.get(0));
            return Either.right(childEither.map(tags -> {
                List<ItemStack> tagItems = tags.stream()
                    .map(pair -> Pair.of(BuiltInRegistries.ITEM.getTag(pair.getFirst()).stream(), pair.getSecond()))
                    .flatMap(pair -> pair.getFirst().flatMap(tag -> tag.stream().map(holder -> new ItemStack(holder.value(), pair.getSecond()))))
                    .collect(Collectors.toList());
                ListIterator<ItemStack> iterator = tagItems.listIterator();
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    for (int i = 1; i < children.size(); ++i) {
                        if (!children.get(i).test(item)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return tagItems;
            }, items -> {
                items = new ArrayList<>(items);
                ListIterator<ItemStack> iterator = items.listIterator();
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    for (int i = 1; i < children.size(); ++i) {
                        if (!children.get(i).test(item)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return items;
            }));
        } else if (((IngredientAccessor)ingredient).getValues().length > 0 && ((IngredientAccessor)ingredient).getValues()[0] instanceof Ingredient.TagValue tagValue) {
            return Either.left(List.of(Pair.of(((TagValueAccessor)tagValue).getTag(), 1)));
        }
        return Either.right(Arrays.stream(ingredient.getItems()).toList());
    }

    // Maps fluids to Either <(tag with count), ItemStack>s
    private static Either<List<Pair<TagKey<Fluid>, Long>>, List<FluidStack>> mapFluid(FluidIngredient ingredient) {
        long amount = ingredient.getAmount();
        CompoundTag tag = ingredient.getNbt();

        List<Pair<TagKey<Fluid>, Long>> tags = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.TagValue tagValue) {
                tags.add(Pair.of(tagValue.getTag(), amount));
            } else {
                fluids.addAll(value.getFluids().stream().map(fluid -> FluidStack.create(fluid, amount, tag)).toList());
            }
        }
        if (!tags.isEmpty()) {
            return Either.left(tags);
        }else {
            return Either.right(fluids);
        }
    }
}
