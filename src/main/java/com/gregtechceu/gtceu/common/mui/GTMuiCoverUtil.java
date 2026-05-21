package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.widgets.layout.Flow;

public class GTMuiCoverUtil {

    public static void addManualIORow(Flow column, EnumSyncValue<ManualIOMode> value) {
        Component[] manualIODesc = {
                Component.translatable("cover.universal.manual_import_export.mode.description.0"),
                Component.translatable("cover.universal.manual_import_export.mode.description.1"),
                Component.translatable("cover.universal.manual_import_export.mode.description.2"),
        };
        column.child(new GTMuiWidgets.EnumRowBuilder<>(ManualIOMode.class)
                .value(value)
                .buttonTooltipSupplier((v) -> () -> Component.translatable(v.getTooltip()))
                .overlay(16, GTGuiTextures.MANUAL_IO_OVERLAY_IN)
                .lang(Text.comp(Component.translatable(ManualIOMode.getTitle())))
                .multiLangTooltip()
                .multiLangTooltip(manualIODesc)
                .build());
    }

    public static void addDistributionModeRow(Flow column, EnumSyncValue<DistributionMode> value) {
        Component[] distributionModeDesc = {
                Component.translatable("cover.conveyor.distribution.round_robin_global.0"),
                Component.translatable("cover.conveyor.distribution.round_robin_global.1"),
                Component.translatable("cover.conveyor.distribution.round_robin_prio.0"),
                Component.translatable("cover.conveyor.distribution.round_robin_prio.1"),
                Component.translatable("cover.conveyor.distribution.round_robin_prio.2"),
                Component.translatable("cover.conveyor.distribution.insert_first.0"),
                Component.translatable("cover.conveyor.distribution.insert_first.1"),
                Component.translatable("cover.conveyor.distribution.insert_first.2"),
        };
        column.child(new GTMuiWidgets.EnumRowBuilder<>(DistributionMode.class)
                .value(value)
                .buttonTooltipSupplier((v) -> () -> Component.translatable(v.getTooltip()))
                .overlay(16, GTGuiTextures.DISTRIBUTION_MODE_OVERLAY)
                .lang(Text.comp(Component.translatable(DistributionMode.getTitle())))
                .multiLangTooltip(distributionModeDesc)
                .build());
    }

    public static void addFilterModeRow(Flow column, EnumSyncValue<FilterMode> value) {
        Component[] filterModeDesc = {
                Component.translatable("cover.universal.manual_import_export.mode.description.0"),
                Component.translatable("cover.universal.manual_import_export.mode.description.1"),
                Component.translatable("cover.universal.manual_import_export.mode.description.2"),
        };
        column.child(new GTMuiWidgets.EnumRowBuilder<>(FilterMode.class)
                .value(value)
                .buttonTooltipSupplier((v) -> () -> Component.translatable(v.getTooltip()))
                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                .lang(Text.comp(Component.translatable(FilterMode.getTitle())))
                .multiLangTooltip(filterModeDesc)
                .build());
    }

    public static void addTransferModeRow(Flow column, EnumSyncValue<TransferMode> value) {
        Component[] transferModeDesc = {
                Component.translatable("cover.robotic_arm.transfer_mode.description.0"),
                Component.translatable("cover.robotic_arm.transfer_mode.description.1"),
                Component.translatable("cover.robotic_arm.transfer_mode.description.2"),
                Component.translatable("cover.robotic_arm.transfer_mode.description.3"),
        };
        column.child(new GTMuiWidgets.EnumRowBuilder<>(TransferMode.class)
                .value(value)
                .buttonTooltipSupplier((v) -> () -> Component.translatable(v.getTooltip()))
                .overlay(16, GTGuiTextures.TRANSFER_MODE_OVERLAY)
                .lang(Text.comp(Component.translatable(TransferMode.getTitle())))
                .multiLangTooltip(transferModeDesc)
                .build());
    }
}
