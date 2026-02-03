package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.serialization.network.ByteBufAdapters;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

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

    public static TextWidget<?> addProgressLine(IWorkableMultiController rlMachine, PanelSyncManager syncManager) {
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

    public static TextWidget<?> addParallelLine(IWorkableMultiController rlMachine, PanelSyncManager syncManager) {
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
                .setEnabledIf(widget -> parallelAmount.getIntValue() != 0);
    }

    public static TextWidget<?> addBatchModeLine(IWorkableMultiController rlMachine, PanelSyncManager syncManager) {
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
                .setEnabledIf(widget -> batchEnabled.getBoolValue() && batchAmount.getIntValue() != 0);
    }

    public static TextWidget<?> addSubtickParallelsLine(IWorkableMultiController rlMachine,
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
                .setEnabledIf(widget -> subtickAmount.getIntValue() != 0);
    }

    public static TextWidget<?> addTotalRunsLine(IWorkableMultiController rlMachine, PanelSyncManager syncManager) {
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
                .setEnabledIf(widget -> totalRunAmount.getIntValue() != 0);
    }

    public static DynamicSyncedWidget<?> addOutputLines(IWorkableMultiController rlmachine,
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
                            .coverChildrenHeight();
                    GTRecipe recipe = recipeSyncHandler.getValue();
                    if (recipe == null) return list;
                    for (var outputCap : recipe.outputs.keySet()) {
                        // Maybe do checking per output capability?
                        // Render items and fluids some way?
                        for (var output : recipe.outputs.get(outputCap)) {
                            list.child(IKey.str(output.toString()).asWidget().width(187 - 3 - 3 - 2 - 2));
                        }
                    }

                    for (var outputCap : recipe.tickOutputs.keySet()) {
                        for (var output : recipe.tickOutputs.get(outputCap)) {
                            list.child(IKey.str(output.toString()).asWidget());
                        }
                    }
                    return list;
                });

        return new DynamicSyncedWidget<>()
                .widthRel(1)
                .coverChildrenHeight()
                .syncHandler(dynamicLinkedSyncHandler);
    }
}
