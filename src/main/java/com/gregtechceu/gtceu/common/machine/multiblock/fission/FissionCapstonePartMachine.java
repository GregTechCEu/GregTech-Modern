package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FissionCapstonePartMachine extends MultiblockPartMachine {

    public FissionCapstonePartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Nullable
    protected ReactorComponent getReactorComponent() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof FissionReactorMachine reactor) {
                return reactor.getGrid().getComponent(self().getBlockPos());
            }
        }
        return null;
    }

    protected int getComponentHeat() {
        ReactorComponent comp = getReactorComponent();
        return comp != null ? comp.getHeat() : 0;
    }

    protected int getComponentMaxHeat() {
        ReactorComponent comp = getReactorComponent();
        return comp != null ? comp.getMaxHeat() : 0;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        int panelWidth = 176;
        int panelHeight = 80;

        IntSyncValue heatSync = new IntSyncValue(this::getComponentHeat, v -> {});
        IntSyncValue maxHeatSync = new IntSyncValue(this::getComponentMaxHeat, v -> {});
        syncManager.syncValue("comp_heat", heatSync);
        syncManager.syncValue("comp_max_heat", maxHeatSync);

        var panel = GTGuis.createPanel(this, panelWidth, panelHeight);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), panelWidth));
        panel.child(Flow.column()
                .coverChildren().padding(8).top(4).alignX(0.5f).childPadding(2)
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    int max = maxHeatSync.getIntValue();
                    if (max <= 0) return Component.translatable("gtceu.multiblock.fission.part.not_formed");
                    int heat = heatSync.getIntValue();
                    float pct = (float) heat / max * 100;
                    return Component.translatable("gtceu.multiblock.fission.part.heat",
                            heat, max, String.format("%.1f%%", pct));
                }))));
        return panel;
    }
}
