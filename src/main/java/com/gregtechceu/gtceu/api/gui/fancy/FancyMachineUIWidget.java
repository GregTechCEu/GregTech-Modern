package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
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
    protected final VerticalTabsWidget sideTabsWidget;
    protected final WidgetGroup pageContainer;
    protected final ConfiguratorPanel configuratorPanel;
    protected final TooltipsPanel tooltipsPanel;
    @Nullable
    protected final PlayerInventoryWidget playerInventory;
    @Setter
    protected int border = 4;

    public FancyMachineUIWidget(IFancyUIProvider fancyUIProvider, int width, int height) {
        super(0, 0, width, height);
        this.fancyUIProvider = fancyUIProvider;
        addWidget(this.pageContainer = new WidgetGroup(0, 0, width, height));
        if (fancyUIProvider.hasPlayerInventory()) {
            addWidget(this.playerInventory = new PlayerInventoryWidget());
            this.playerInventory.setSelfPosition(new Position(2, height - 86));
            this.playerInventory.setBackground((IGuiTexture) null);
        } else {
            playerInventory = null;
        }
        addWidget(this.tabsWidget = new TabsWidget(this::onTabSwitch));
        addWidget(this.sideTabsWidget = new VerticalTabsWidget(this::onTabSwitch, -20, 0, 24, height));
        addWidget(this.tooltipsPanel = new TooltipsPanel());
        addWidget(this.configuratorPanel = new ConfiguratorPanel(-(24 + 2), 102));

        setBackground(GuiTextures.BACKGROUND.copy().setColor(Long.decode(ConfigHolder.INSTANCE.client.defaultUIColor).intValue() | 0xFF000000));
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (this.playerInventory != null) {
            this.playerInventory.setPlayer(gui.entityPlayer);
        }
        setupFancyUI(this.fancyUIProvider);
        this.fancyUIProvider.setupTabs(tabsWidget);
        this.fancyUIProvider.attachSideTabs(sideTabsWidget);
    }

    public void setupFancyUI(IFancyUIProvider fancyUI) {
        var mainPage = fancyUI.createMainPage(this);

        // clear up
        this.pageContainer.clearAllWidgets();
        this.configuratorPanel.clear();
        this.tooltipsPanel.clear();

        // layout
        var size = new Size(Math.max(172, mainPage.getSize().width + border * 2), Math.max(86, mainPage.getSize().height + border * 2));
        if (LDLib.isRemote() && getGui() != null) {
            getGui().setSize(getSize().width, getSize().height);
        }
        this.tabsWidget.setSize(new Size(size.width, 24));
        this.sideTabsWidget.setSize(new Size(24, size.height));
        this.pageContainer.setSize(size);
        this.tooltipsPanel.setSelfPosition(new Position(size.width + 2, 2));

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
