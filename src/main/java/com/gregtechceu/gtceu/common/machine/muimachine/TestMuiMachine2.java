package com.gregtechceu.gtceu.common.machine.muimachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.drawable.graph.GraphDrawable;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.DAM;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

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

        double[] x = DAM.linspace(-25, 25, 200);
        double[] y = DAM.div(DAM.sin(x, null), x, null);
        double[] y2 = DAM.cos(x, null);

        // return buildAspectRatioUI();

        return GTGuis.createPanel(this, 176, 168)
                .child(new ButtonWidget<>()
                        .size(60, 18)
                        .overlay(IKey.dynamic(() -> Component
                                .literal("Button " + val))))
                .child(new GraphDrawable()
                        .yLim(-1.2f, 1.2f)
                        .xTickFinder(Mth.HALF_PI, 1)
                        .yTickFinder(0.2f, 1)
                        .plot(x, y)
                        .plot(x, y2)
                        .graphAspectRatio(16 / 9f)
                        .asWidget().top(50).size(200, 200));
    }

    public static @NotNull ModularPanel buildAspectRatioUI() {
        return new ModularPanel("aspect_ratio")
                .coverChildren()
                .padding(10)
                .child(new Row()
                        .childPadding(10)
                        .coverChildren()
                        .child(new Rectangle().color(Color.BLUE_ACCENT.main)
                                .asIcon().aspectRatio(4f / 3)
                                .asWidget().size(80)
                                .overlay(IKey.str("4:3 Free")))
                        .child(new Rectangle().color(Color.RED_ACCENT.main)
                                .asIcon().aspectRatio(4f / 3).width(70)
                                .asWidget().size(80)
                                .overlay(IKey.str("4:3 | width = 70")))
                        .child(new Rectangle().color(Color.LIGHT_GREEN.main)
                                .asIcon().aspectRatio(4f / 3).height(45).alignment(Alignment.BottomRight)
                                .asWidget().size(80)
                                .overlay(IKey.str("4:3 | height = 45\nBottom Right"))))
                .overlay();
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
