package com.gregtechceu.gtceu.api.machine.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;

public class MachineUIPanel extends ModularPanel<MachineUIPanel> {

    public static final int DEFAULT_CONTENT_WIDTH = 169;
    public static final int DEFAULT_CONTENT_HEIGHT = 77;

    @Getter
    protected final Flow leftConfiguratorPanel, rightConfiguratorPanel;
    @Getter
    protected final ParentWidget<?> mainContents;

    public MachineUIPanel(MetaMachine machine, UISettings settings, boolean attachPlayerInventory,
                          boolean addTitleBar, boolean drawGTLogo, UITexture gtLogoTexture) {
        super(machine.getDefinition().getId().getPath());

        UITexture themeBackground = (UITexture) ThemeAPI.INSTANCE.getTheme(settings.getTheme()).getPanelTheme()
                .theme().getBackground();
        if (themeBackground == null) themeBackground = GTGuiTextures.BACKGROUND;
        leftConfiguratorPanel = Flow.col()
                .coverChildren()
                .rightRel(1.0f)
                .reverseLayout(true)
                .padding(4, 2, 4, 4)
                .bottom(16)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .childPadding(2)
                .excludeAreaInRecipeViewer()
                .background(themeBackground.getSubArea(0f, 0f, 0.75f, 1.0f))
                .setEnabledIf(f -> !f.getChildren().isEmpty())
                .decoration();

        rightConfiguratorPanel = Flow.col()
                .coverChildren()
                .leftRel(1.0f)
                .reverseLayout(true)
                .padding(2, 4, 4, 4)
                .bottom(16)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .childPadding(2)
                .excludeAreaInRecipeViewer()
                .background(themeBackground.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                .setEnabledIf(f -> !f.getChildren().isEmpty())
                .decoration();

        Flow panelContents = Flow.col().coverChildren();
        panelContents.margin(4);
        mainContents = new ParentWidget<>().coverChildren(DEFAULT_CONTENT_WIDTH, DEFAULT_CONTENT_HEIGHT);

        panelContents.child(mainContents);

        if (attachPlayerInventory) {
            var inventory = SlotGroupWidget.playerInventory((index, slot) -> slot)
                    .marginTop(1)
                    .marginBottom(3);
            panelContents.child(inventory);
        }

        if (addTitleBar) {
            child(GTMuiWidgets.createTitleBar(machine.getDefinition(), 172).decoration());
        }

        if (drawGTLogo) {
            child(new IDrawable.DrawableWidget(gtLogoTexture)
                    .right(7).bottom(7 + (attachPlayerInventory ? 78 : 0)).decoration());
        }

        child(leftConfiguratorPanel);
        child(rightConfiguratorPanel);
        child(panelContents);
        coverChildren();
    }
}
