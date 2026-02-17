package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.FluidDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.serialization.network.ByteBufAdapters;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

public class GTMultiblockTextUtil {

    public static TextWidget<?> addEnergyUsageLine(WorkableElectricMultiblockMachine weMachine,
                                                   PanelSyncManager syncManager) {
        LongSyncValue energyUsage = syncManager.getOrCreateSyncHandler("energyUsage", LongSyncValue.class,
                () -> new LongSyncValue(() -> {
                    var energyList = weMachine.getEnergyContainer();
                    return Math.max(energyList.getInputVoltage(), energyList.getOutputVoltage());
                }));
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(weMachine::isFormed));
        BooleanSyncValue isActive = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> weMachine.getRecipeLogic().isActive()));

        return IKey.dynamic(() -> {
            String energyFormatted = FormattingUtil.formatNumbers(energyUsage.getLongValue());

            byte voltageTier = GTUtil.getFloorTierByVoltage(energyUsage.getLongValue());
            Component voltageName = Component.literal(
                    GTValues.VNF[voltageTier]);

            MutableComponent bodyText = Component.translatable("gtceu.multiblock.max_energy_per_tick",
                    energyFormatted, voltageName).withStyle(ChatFormatting.GRAY);
            Component hoverText = Component.translatable("gtceu.multiblock.max_energy_per_tick_hover")
                    .withStyle(ChatFormatting.GRAY);
            return bodyText
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
        })
                .color(Color.WHITE.main)
                .asWidget()
                .setEnabledIf(widget -> isFormed.getBoolValue() && isActive.getBoolValue());
    }

    public static IKey addEnergyTierLine(boolean formed, int tier) {
        if (!formed || tier < GTValues.ULV || tier > GTValues.MAX)
            return IKey.EMPTY;

        Component voltageName = Component.literal(GTValues.VNF[tier]);
        MutableComponent bodyText = Component.translatable(
                "gtceu.multiblock.max_recipe_tier",
                voltageName).withStyle(ChatFormatting.GRAY);
        Component hoverText = Component.translatable("gtceu.multiblock.max_recipe_tier_hover")
                .withStyle(ChatFormatting.GRAY);
        return IKey.dynamic(() -> bodyText
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));
    }

    public static TextWidget<?> addProgressLine(WorkableMultiblockMachine rlMachine, PanelSyncManager syncManager) {
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(rlMachine::isFormed));

        BooleanSyncValue isActive = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> rlMachine.getRecipeLogic().isActive()));
        IntSyncValue currentProgress = syncManager.getOrCreateSyncHandler("currentProgress", IntSyncValue.class,
                () -> new IntSyncValue(() -> rlMachine.getRecipeLogic().getProgress()));
        IntSyncValue maxProgress = syncManager.getOrCreateSyncHandler("maxProgress", IntSyncValue.class,
                () -> new IntSyncValue(() -> rlMachine.getRecipeLogic().getMaxProgress()));
        DoubleSyncValue progressPercent = syncManager.getOrCreateSyncHandler("progressPercent", DoubleSyncValue.class,
                () -> new DoubleSyncValue(() -> rlMachine.getRecipeLogic().getProgressPercent()));

        return IKey.dynamic(() -> {
            int progress = (int) (progressPercent.getDoubleValue() * 100.f);
            float current = (float) currentProgress.getDoubleValue() / 20.f;
            float max = (float) maxProgress.getDoubleValue() / 20.f;
            return Component.translatable("gtceu.multiblock.progress",
                    String.format("%.2f", current), String.format("%.2f", max), progress);
        })
                .color(Color.WHITE.main)
                .asWidget()
                .setEnabledIf(widget -> isFormed.getBoolValue() && isActive.getBoolValue());
    }

    public static TextWidget<?> addEnergyTierLine(WorkableElectricMultiblockMachine rlMachine,
                                                  PanelSyncManager syncManager) {
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(rlMachine::isFormed));

        IntSyncValue tier = syncManager.getOrCreateSyncHandler("energyTier", IntSyncValue.class,
                () -> new IntSyncValue(rlMachine::getTier));

        return IKey.dynamic(() -> {
            Component voltageName = Component.literal(GTValues.VNF[tier.getIntValue()]);
            return Component.translatable(
                    "gtceu.multiblock.max_recipe_tier",
                    voltageName).withStyle(ChatFormatting.GRAY);
        })
                .asWidget()
                .tooltip(new RichTooltip().add(Component.translatable("gtceu.multiblock.max_recipe_tier_hover")
                        .withStyle(ChatFormatting.GRAY)))
                .setEnabledIf(widget -> isFormed.getBoolValue());
    }

    public static TextWidget<?> addParallelLine(WorkableMultiblockMachine rlMachine, PanelSyncManager syncManager) {
        IntSyncValue parallelAmount = syncManager.getOrCreateSyncHandler("parallelAmount", IntSyncValue.class,
                () -> new IntSyncValue(() -> {
                    if (rlMachine.getRecipeLogic().getLastRecipe() == null) return 0;
                    return rlMachine.getRecipeLogic().getLastRecipe().parallels;
                }));

        return IKey.dynamic(() -> {
            Component runs = Component.literal(FormattingUtil.formatNumbers(parallelAmount.getIntValue()))
                    .withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.parallel";
            return Component.translatable(key, runs)
                    .withStyle(ChatFormatting.GRAY);
        }).asWidget()
                .setEnabledIf(widget -> parallelAmount.getIntValue() > 1);
    }

    public static TextWidget<?> addBatchModeLine(WorkableMultiblockMachine rlMachine, PanelSyncManager syncManager) {
        BooleanSyncValue batchEnabled = syncManager.getOrCreateSyncHandler("batchEnabled", BooleanSyncValue.class,
                () -> new BooleanSyncValue(rlMachine::isBatchEnabled));
        IntSyncValue batchAmount = syncManager.getOrCreateSyncHandler("batchAmount", IntSyncValue.class,
                () -> new IntSyncValue(() -> {
                    if (rlMachine.getRecipeLogic().getLastRecipe() == null) return 0;
                    return rlMachine.getRecipeLogic().getLastRecipe().batchParallels;
                }));

        return IKey.dynamic(() -> {
            Component runs = Component.literal(FormattingUtil.formatNumbers(batchAmount.getIntValue()))
                    .withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.batch_enabled";
            return Component.translatable(key, runs)
                    .withStyle(ChatFormatting.GRAY);
        }).asWidget()
                .setEnabledIf(widget -> batchEnabled.getBoolValue() && batchAmount.getIntValue() > 1);
    }

    public static TextWidget<?> addSubtickParallelsLine(WorkableMultiblockMachine rlMachine,
                                                        PanelSyncManager syncManager) {
        IntSyncValue subtickAmount = syncManager.getOrCreateSyncHandler("subtickAmount", IntSyncValue.class,
                () -> new IntSyncValue(() -> {
                    if (rlMachine.getRecipeLogic().getLastRecipe() == null) return 0;
                    return rlMachine.getRecipeLogic().getLastRecipe().subtickParallels;
                }));

        return IKey.dynamic(() -> {
            Component runs = Component.literal(FormattingUtil.formatNumbers(subtickAmount.getIntValue()))
                    .withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.subtick_parallels";
            return Component.translatable(key, runs)
                    .withStyle(ChatFormatting.GRAY);
        }).asWidget()
                .setEnabledIf(widget -> subtickAmount.getIntValue() > 1);
    }

    public static TextWidget<?> addTotalRunsLine(WorkableMultiblockMachine rlMachine, PanelSyncManager syncManager) {
        IntSyncValue totalRunAmount = syncManager.getOrCreateSyncHandler("totalRunAmount", IntSyncValue.class,
                () -> new IntSyncValue(() -> {
                    if (rlMachine.getRecipeLogic().getLastRecipe() == null) return 0;
                    return rlMachine.getRecipeLogic().getLastRecipe().getTotalRuns();
                }));

        return IKey.dynamic(() -> {
            Component runs = Component.literal(FormattingUtil.formatNumbers(totalRunAmount.getIntValue()))
                    .withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.total_runs";
            return Component.translatable(key, runs)
                    .withStyle(ChatFormatting.GRAY);
        }).asWidget()
                .setEnabledIf(widget -> totalRunAmount.getIntValue() > 1);
    }

    public static TextWidget<?> addSteamUsageLine(SteamEnergyRecipeHandler steamRH, PanelSyncManager syncManager) {
        IntSyncValue steamAmount = syncManager.getOrCreateSyncHandler("steamTank", IntSyncValue.class,
                () -> new IntSyncValue(() -> {
                    if (steamRH == null) return 0;
                    return steamRH.getSteamTank().getFluidInTank(0).getAmount();
                }));
        IntSyncValue steamCapacity = syncManager.getOrCreateSyncHandler("steamCapacity", IntSyncValue.class,
                () -> new IntSyncValue(() -> {
                    if (steamRH == null) return 0;
                    return steamRH.getSteamTank().getTankCapacity(0);
                }));

        BooleanSyncValue hasSteamHandler = syncManager.getOrCreateSyncHandler("hasSteam", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> steamRH != null));

        return IKey
                .dynamic(() -> Component.translatable("gtceu.multiblock.steam.steam_stored",
                        FormattingUtil.formatNumbers(steamAmount.getIntValue()),
                        FormattingUtil.formatNumbers(steamCapacity.getIntValue())))
                .color(Color.WHITE.main)
                .asWidget()
                .setEnabledIf((w) -> hasSteamHandler.getBoolValue());
    }

    public static DynamicSyncedWidget<?> addOutputLines(WorkableMultiblockMachine rlmachine,
                                                        PanelSyncManager syncManager) {
        GenericSyncValue<GTRecipe> recipeSyncValue = syncManager.getOrCreateSyncHandler("GTRecipe",
                GenericSyncValue.class,
                () -> new GenericSyncValue.Builder<>(GTRecipe.class)
                        .getter(() -> rlmachine.getRecipeLogic().getLastRecipe())
                        .setter((newRecipe) -> {})
                        .adapter(ByteBufAdapters.GTRECIPE)
                        .copy((toCopy) -> {
                            if (toCopy == null) return null;
                            return toCopy.copy();
                        })
                        .build());

        DynamicLinkedSyncHandler<GenericSyncValue<GTRecipe>> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(
                recipeSyncValue)
                .widgetProvider((syncManager1, recipeSyncHandler) -> {
                    var list = Flow.column()
                            .widthRel(1)
                            .coverChildrenHeight()
                            .crossAxisAlignment(Alignment.CrossAxis.START);
                    GTRecipe recipe = recipeSyncHandler.getValue();
                    if (recipe == null) return list;

                    for (var output : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                        var widget = createItemLineForOutput(output, recipe);
                        if (widget.isEmpty()) continue;
                        list.child(widget.get().width(187 - 3 - 3 - 2 - 2));
                    }

                    for (var output : recipe.getOutputContents(FluidRecipeCapability.CAP)) {
                        var widget = createFluidLineForOutput(output, recipe);
                        if (widget.isEmpty()) continue;
                        list.child(widget.get().width(187 - 3 - 3 - 2 - 2));
                    }

                    return list;
                });

        return new DynamicSyncedWidget<>()
                .widthRel(1)
                .coverChildrenHeight()
                .syncHandler(dynamicLinkedSyncHandler);
    }

    public static Optional<Widget<?>> createItemLineForOutput(Content itemOutput, GTRecipe recipe) {
        int runs = recipe.getTotalRuns();
        var function = recipe.getType().getChanceFunction();

        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        double maxDurationSec = (double) recipe.duration / 20.0;

        boolean rounded = false;
        ItemStack stack;
        // number of items output by a non-ranged ingredient
        int count = 0;
        // number of items output, but stored as a double. Used for accurate items/second display.
        double countD = 1;
        // number of items output which is actually displayed. Can be either a number, or a range.
        Component displaycount;
        if (itemOutput.content instanceof IntProviderIngredient provider) {
            rounded = true;
            stack = provider.getMaxSizeStack();
            displaycount = Component.translatable("gtceu.gui.content.range",
                    provider.getCountProvider().getMinValue(),
                    provider.getCountProvider().getMaxValue());
            if (itemOutput.chance < itemOutput.maxChance) {
                countD = countD * runs * function.getBoostedChance(itemOutput, recipeTier, chanceTier) /
                        itemOutput.maxChance;
            }
            countD = countD * provider.getMidRoll();
        } else {
            var stacks = ItemRecipeCapability.CAP.of(itemOutput.content).getItems();
            if (stacks.length == 0) return Optional.empty();
            stack = stacks[0];
            count = stack.getCount();
            countD *= count;
            if (itemOutput.chance < itemOutput.maxChance) {
                rounded = true;
                countD = countD * runs * function.getBoostedChance(itemOutput, recipeTier, chanceTier) /
                        itemOutput.maxChance;
            }
            count = Math.max(1, (int) Math.round(countD));
            displaycount = Component.literal(String.valueOf(count));
        }
        if (countD < maxDurationSec) {
            String key = "gtceu.multiblock.output_line." + (rounded ? "2" : "0");
            return Optional.of(
                    Flow.row()
                            .coverChildren()
                            .childPadding(2)
                            .child(new ItemDrawable(stack).asWidget())
                            .child(
                                    IKey.lang(
                                            Component.translatable(key, stack.getHoverName(), displaycount,
                                                    FormattingUtil.formatNumber2Places(maxDurationSec / countD)))
                                            .asWidget()));
        } else {
            String key = "gtceu.multiblock.output_line." + (rounded ? "3" : "1");
            return Optional.of(
                    Flow.row()
                            .coverChildren()
                            .childPadding(2)
                            .child(new ItemDrawable(stack).asWidget())
                            .child(
                                    IKey.lang(
                                            Component.translatable(key, stack.getHoverName(), displaycount,
                                                    FormattingUtil.formatNumber2Places(countD / maxDurationSec)))
                                            .asWidget()));
        }
    }

    public static Optional<Widget<?>> createFluidLineForOutput(Content fluidOutput, GTRecipe recipe) {
        int runs = recipe.getTotalRuns();
        var function = recipe.getType().getChanceFunction();

        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        double maxDurationSec = (double) recipe.duration / 20.0;

        boolean rounded = false;
        FluidStack stack;
        // amount of fluid output by a non-ranged ingredient
        int amount = 0;
        // amount of fluid output, but stored as a double. Used for accurate fluid/second display.
        double amountD = 1;
        // amount of fluid output which is actually displayed. Can be either a number, or a range.
        Component displaycount;
        if (fluidOutput.content instanceof IntProviderFluidIngredient provider) {
            rounded = true;
            stack = provider.getMaxSizeStack();
            displaycount = Component.translatable("gtceu.gui.content.range",
                    provider.getCountProvider().getMinValue(),
                    provider.getCountProvider().getMaxValue());
            if (fluidOutput.chance < fluidOutput.maxChance) {
                amountD = amountD * runs * function.getBoostedChance(fluidOutput, recipeTier, chanceTier) /
                        fluidOutput.maxChance;
            }
            amountD = amountD * provider.getMidRoll();
        } else {
            var stacks = FluidRecipeCapability.CAP.of(fluidOutput.content).getStacks();
            if (stacks.length == 0) return Optional.empty();
            stack = stacks[0];
            amount = stack.getAmount();
            amountD *= amount;
            if (fluidOutput.chance < fluidOutput.maxChance) {
                rounded = true;
                amountD = amountD * runs * function.getBoostedChance(fluidOutput, recipeTier, chanceTier) /
                        fluidOutput.maxChance;
            }
            amount = Math.max(1, (int) Math.round(amountD));
            displaycount = Component.literal(String.valueOf(amount));
        }
        if (amountD < maxDurationSec) {
            String key = "gtceu.multiblock.output_line." + (rounded ? "2" : "0");
            return Optional.of(
                    Flow.row()
                            .coverChildren()
                            .childPadding(2)
                            .child(new FluidDrawable(stack).asWidget())
                            .child(
                                    IKey.lang(
                                            Component.translatable(key, stack.getDisplayName(), displaycount,
                                                    FormattingUtil.formatNumber2Places(maxDurationSec / amountD)))
                                            .asWidget()));
        } else {
            String key = "gtceu.multiblock.output_line." + (rounded ? "3" : "1");
            return Optional.of(
                    Flow.row()
                            .coverChildren()
                            .childPadding(2)
                            .child(new FluidDrawable(stack).asWidget())
                            .child(
                                    IKey.lang(
                                            Component.translatable(key, stack.getDisplayName(), displaycount,
                                                    FormattingUtil.formatNumber2Places(amountD / maxDurationSec)))
                                            .asWidget()));
        }
    }
}
