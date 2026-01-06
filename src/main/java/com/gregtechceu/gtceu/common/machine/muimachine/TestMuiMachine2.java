package com.gregtechceu.gtceu.common.machine.muimachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;
import net.minecraft.network.chat.Component;

public class TestMuiMachine2 extends MetaMachine implements IMuiMachine {

    private TickableSubscription sub;

    public TestMuiMachine2(IMachineBlockEntity holder) {
        super(holder);
        sub = subscribeServerTick(this::tick);
    }

    @SyncToClient
    private int val = 0;

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IntSyncValue valSync = new IntSyncValue(() -> this.val, (v) -> {});
        syncManager.syncValue("valSync", valSync);

        return GTGuis.createPanel(this, 176, 168)
                .child(new ButtonWidget<>()
                        .size(60, 18)
                        .overlay(IKey.dynamic(() -> Component
                                .literal("Button " + val))));
    }

    @Override
    public void clientTick() {
        super.clientTick();
    }

    public void tick() {
        val++;
        syncDataHolder.markClientSyncFieldDirty("val");
    }
}
