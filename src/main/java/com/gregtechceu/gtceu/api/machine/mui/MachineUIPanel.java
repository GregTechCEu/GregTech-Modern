package com.gregtechceu.gtceu.api.machine.mui;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
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
import org.jetbrains.annotations.Nullable;

public class MachineUIPanel extends ModularPanel<MachineUIPanel> {

    public static final int DEFAULT_CONTENT_WIDTH = 169;
    public static final int DEFAULT_CONTENT_HEIGHT = 77;

    private final MetaMachine machine;
    @Getter
    protected final Flow leftConfiguratorPanel, rightConfiguratorPanel;
    @Getter
    protected final @Nullable Flow tabTogglePanel;
    @Getter
    protected final ParentWidget<?> mainContents;

    protected final @Nullable IPanelHandler previewWidgetPanelHandler;

    public MachineUIPanel(MetaMachine machine, PanelSyncManager syncManager, UISettings settings, boolean attachPlayerInventory,
                          boolean addTitleBar, boolean drawGTLogo, UITexture gtLogoTexture, boolean displayEIOWidget) {
        super(machine.getDefinition().getId().getPath());
        this.machine = machine;

        UITexture themeBackground = null;
        if (!machine.getDefinition().getThemeId().equals(ThemeAPI.DEFAULT_ID)) {
            themeBackground = (UITexture) ThemeAPI.INSTANCE.getTheme(machine.getDefinition().getThemeId())
                    .getPanelTheme()
                    .theme().getBackground();
        }

        if (themeBackground == null) {
            themeBackground = (UITexture) ThemeAPI.INSTANCE.getTheme(settings.getTheme()).getPanelTheme()
                    .theme().getBackground();
        }

        if (themeBackground == null) themeBackground = GTGuiTextures.BACKGROUND;

        leftConfiguratorPanel = createLeftConfiguratorPanel(themeBackground);
        rightConfiguratorPanel = createRightConfiguratorBackground(themeBackground);

        if (displayEIOWidget) {
            tabTogglePanel = createTabButtonPanel(themeBackground);
            previewWidgetPanelHandler = syncManager.syncedPanel("world_preview_panel", true,
                    (psm, sync) -> new MachineInWorldPreview(machine, psm));
        } else {
            tabTogglePanel = null;
            previewWidgetPanelHandler = null;
        }

        Flow panelContents = Flow.col().coverChildren().collapseDisabledChildren();
        panelContents.margin(4);
        mainContents = new ParentWidget<>()
                .coverChildren(DEFAULT_CONTENT_WIDTH, DEFAULT_CONTENT_HEIGHT);

        panelContents.child(mainContents);

        if (attachPlayerInventory) {
            panelContents.childPadding(2);
            var inventory = SlotGroupWidget.playerInventory((index, slot) -> slot)
                    .marginTop(1)
                    .marginBottom(3);
            panelContents.child(inventory);
        }

        if (addTitleBar) {
            child(GTMuiWidgets.createTitleBar(machine.getDefinition(), 172).decoration());
        }

        if (drawGTLogo) {
            panelContents.child(new IDrawable.DrawableWidget(gtLogoTexture)
                    .right(7).bottom(7 + (attachPlayerInventory ? 78 : 0)).decoration());
        }

        coverChildren();
        child(leftConfiguratorPanel);
        if (tabTogglePanel != null) child(tabTogglePanel);
        child(rightConfiguratorPanel);
        child(panelContents);
    }

    private Flow createTabButtonPanel(UITexture themeBackground) {
        return Flow.col()
                .coverChildren()
                .rightRel(1.0f)
                .padding(4, 2, 4, 4)
                .top(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .childPadding(2)
                .excludeAreaInRecipeViewer()
                .background(themeBackground.getSubArea(0f, 0f, 0.75f, 1.0f))
                .child(new ToggleButton()
                        .value(new BoolValue.Dynamic(
                                () -> previewWidgetPanelHandler != null && previewWidgetPanelHandler.isPanelOpen(),
                                (b) -> {
                                    if (previewWidgetPanelHandler != null) previewWidgetPanelHandler.togglePanel();
                                }))
                        .size(16)
                        .overlay(GuiTextures.GEAR))
                .decoration();
    }

    private Flow createLeftConfiguratorPanel(UITexture themeBackground) {
        return Flow.col()
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
    }

    private Flow createRightConfiguratorBackground(UITexture themeBackground) {
        return Flow.col()
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
    }
}
