package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/6/27
 * @implNote FancyMachineUIWidget
 */
@Getter
public class FancyMachineUIWidget extends WidgetGroup {
    protected final IFancyUIProvider fancyUIProvider;
    protected final TabsWidget tabsWidget;
    protected final WidgetGroup pageContainer;
    protected final ConfiguratorPanel configuratorPanel;
    protected final TooltipsPanel tooltipsPanel;
    @Nullable
    protected final PlayerInventoryWidget playerInventory;
    @Setter
    protected int border = 4;

    public FancyMachineUIWidget(IFancyUIProvider fancyUIProvider) {
        super(0, 0, 200, 100 + (fancyUIProvider.hasPlayerInventory() ? 90 : 0));
        this.fancyUIProvider = fancyUIProvider;
        addWidget(this.pageContainer = new WidgetGroup(0, 0, 200, 100));
        if (fancyUIProvider.hasPlayerInventory()) {
            addWidget(this.playerInventory = new PlayerInventoryWidget());
        } else {
            playerInventory = null;
        }
        addWidget(this.tabsWidget = new TabsWidget(this::onTabSwitch));
        addWidget(this.tooltipsPanel = new TooltipsPanel());
        addWidget(this.configuratorPanel = new ConfiguratorPanel());
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (this.playerInventory != null) {
            this.playerInventory.setPlayer(gui.entityPlayer);
        }
        setupFancyUI(this.fancyUIProvider);
        this.fancyUIProvider.setupTabs(tabsWidget);
    }

    public void setupFancyUI(IFancyUIProvider fancyUI) {
        var mainPage = fancyUI.createMainPage();

        // clear up
        this.pageContainer.clearAllWidgets();
        this.configuratorPanel.clear();
        this.tooltipsPanel.clear();

        // layout
        var size = new Size(Math.max(172, mainPage.getSize().width + border * 2), Math.max(86, mainPage.getSize().height + border * 2));
        setSize(new Size(size.width, size.height + (playerInventory == null ? 0 : playerInventory.getSize().height + 4)));
        if (LDLib.isRemote() && getGui() != null) {
            getGui().setSize(getSize().width, getSize().height);
        }
        this.tabsWidget.setSize(new Size(size.width, 24));
        this.pageContainer.setBackground(GuiTextures.BACKGROUND);
        this.pageContainer.setSize(size);
        this.tooltipsPanel.setSelfPosition(new Position(size.width + 2, 2));
        if (this.playerInventory != null) {
            this.playerInventory.setSelfPosition(new Position((size.width - playerInventory.getSize().width) / 2, size.height + 4));
        }

        // setup
        this.pageContainer.addWidget(mainPage);
        mainPage.setSelfPosition(new Position(
                (pageContainer.getSize().width - mainPage.getSize().width) / 2,
                (pageContainer.getSize().height - mainPage.getSize().height) / 2));
        fancyUI.attachConfigurators(configuratorPanel);
        fancyUI.attachTooltips(tooltipsPanel);
    }

    private void onTabSwitch(IFancyUIProvider newTab) {
        setupFancyUI(newTab);
    }
}
