package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FuelRodPortPartMachine extends FissionCapstonePartMachine {

    @Getter
    @SaveField
    private final NotifiableItemStackHandler inventory;

    public FuelRodPortPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.BOTH);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        int panelWidth = 176;
        int panelHeight = 166;

        IntSyncValue heatSync = new IntSyncValue(this::getComponentHeat, v -> {});
        IntSyncValue maxHeatSync = new IntSyncValue(this::getComponentMaxHeat, v -> {});
        syncManager.syncValue("comp_heat", heatSync);
        syncManager.syncValue("comp_max_heat", maxHeatSync);

        var panel = GTGuis.createPanel(this, panelWidth, panelHeight);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), panelWidth));
        panel.child(Flow.column()
                .coverChildren().padding(8).top(4).alignX(0.5f).childPadding(4)
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(inventory, 0)))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    int max = maxHeatSync.getIntValue();
                    if (max <= 0) return Component.translatable("gtceu.multiblock.fission.part.not_formed");
                    int heat = heatSync.getIntValue();
                    float pct = (float) heat / max * 100;
                    return Component.translatable("gtceu.multiblock.fission.part.heat",
                            heat, max, String.format("%.1f%%", pct));
                }))));
        panel.bindPlayerInventory();
        return panel;
    }
}
