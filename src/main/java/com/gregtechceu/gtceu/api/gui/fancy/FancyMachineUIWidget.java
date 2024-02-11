package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

/**
 * @author KilaBash
 * @date 2023/6/27
 * @implNote FancyMachineUIWidget
 */
@Getter
public class FancyMachineUIWidget extends WidgetGroup {
    protected final TitleBarWidget titleBar;
    protected final VerticalTabsWidget sideTabsWidget;
    protected final WidgetGroup pageContainer;
    protected final PageSwitcherUIProvider pageSwitcher;
    protected final ConfiguratorPanel configuratorPanel;
    protected final TooltipsPanel tooltipsPanel;

    @Nullable
    protected final PlayerInventoryWidget playerInventory;
    @Setter
    protected int border = 4;

    protected final IFancyUIProvider mainPage;

    /*
     * Current Page:      The page visible in the UI
     * Current Home Page: The currently selected multiblock part's home page.
     */
    protected IFancyUIProvider currentPage;
    protected IFancyUIProvider currentHomePage;

    protected List<IFancyUIProvider> subPages;

    protected Stack<NavigationEntry> previousPages = new Stack<>();

    protected record NavigationEntry(IFancyUIProvider page, IFancyUIProvider homePage, Runnable onNavigation) {
    }

    public FancyMachineUIWidget(IFancyUIProvider mainPage, int width, int height) {
        super(0, 0, width, height);
        this.mainPage = mainPage;
        addWidget(this.pageContainer = new WidgetGroup(0, 0, width, height));
        if (mainPage.hasPlayerInventory()) {
            addWidget(this.playerInventory = new PlayerInventoryWidget());
            this.playerInventory.setSelfPosition(new Position(2, height - 86));
            this.playerInventory.setBackground((IGuiTexture) null);
        } else {
            playerInventory = null;
        }

        addWidget(this.titleBar = new TitleBarWidget(width, this::navigateBack, this::openPageSwitcher));
        addWidget(this.sideTabsWidget = new VerticalTabsWidget(this::navigate, -20, 0, 24, height));
        addWidget(this.tooltipsPanel = new TooltipsPanel());
        addWidget(this.configuratorPanel = new ConfiguratorPanel(-(24 + 2), height));
        pageSwitcher = new PageSwitcherUIProvider();

        setBackground(GuiTextures.BACKGROUND.copy().setColor(Long.decode(ConfigHolder.INSTANCE.client.defaultUIColor).intValue() | 0xFF000000));
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (this.playerInventory != null) {
            this.playerInventory.setPlayer(gui.entityPlayer);
        }

        this.currentHomePage = this.mainPage;
        this.subPages = this.mainPage.getSubTabs();
        this.currentPage = this.mainPage;

        setupFancyUI(this.mainPage);

        this.mainPage.attachSideTabs(sideTabsWidget);
    }

    public void setupFancyUI(IFancyUIProvider fancyUI) {
        this.setupFancyUI(fancyUI, true);
    }

    public void setupFancyUI(IFancyUIProvider fancyUI, boolean showInventory) {
        clearUI();
        sideTabsWidget.selectTab(fancyUI);
        titleBar.updateState(
            currentHomePage,
            !this.previousPages.isEmpty(),
            !this.subPages.isEmpty() && this.currentPage != this.pageSwitcher
        );

        var page = fancyUI.createMainPage(this);

        // layout
        var size = new Size(Math.max(172, page.getSize().width + border * 2), Math.max(86, page.getSize().height + border * 2));
        setSize(new Size(size.width, size.height + (!showInventory || playerInventory == null ? 0 : playerInventory.getSize().height)));
        if (LDLib.isRemote() && getGui() != null) {
            getGui().setSize(getSize().width, getSize().height);
        }
        this.sideTabsWidget.setSize(new Size(24, size.height));
        this.pageContainer.setSize(size);
        this.tooltipsPanel.setSelfPosition(new Position(-20, -20));
        if (this.playerInventory != null) {
            this.playerInventory.setSelfPosition(new Position((size.width - playerInventory.getSize().width) / 2, size.height));

            this.playerInventory.setActive(showInventory);
            this.playerInventory.setVisible(showInventory);
        }

        // setup
        this.pageContainer.addWidget(page);
        page.setSelfPosition(new Position(
                (pageContainer.getSize().width - page.getSize().width) / 2,
                (pageContainer.getSize().height - page.getSize().height) / 2));
        fancyUI.attachConfigurators(configuratorPanel);
        configuratorPanel.setSelfPosition(new Position(-24 - 2, getGui().getHeight() - configuratorPanel.getSize().height - 4));
        fancyUI.attachTooltips(tooltipsPanel);

        titleBar.setSize(new Size(this.getSize().width, titleBar.getSize().height));
    }

    protected void clearUI() {
        this.pageContainer.clearAllWidgets();
        this.configuratorPanel.clear();
        this.tooltipsPanel.clear();
    }

    private void openPageSwitcher(ClickData clickData) {
        navigate(this.pageSwitcher);

        this.sideTabsWidget.setVisible(false);
        this.sideTabsWidget.setActive(false);

        var previousNavEntry = this.previousPages.pop();
        this.previousPages.clear();
        // Modify the previous nav entry to and make the side tabs visible again and navigate to the current home page:
        this.previousPages.push(new NavigationEntry(previousNavEntry.homePage, previousNavEntry.homePage, () -> {
            sideTabsWidget.setVisible(true);
            sideTabsWidget.setActive(true);
        }));
    }

    protected void navigateToPart(IFancyUIProvider partHomePage) {
        navigate(partHomePage, partHomePage);
    }

    protected void navigate(IFancyUIProvider newPage) {
        navigate(newPage, this.currentHomePage);
    }

    protected void navigate(IFancyUIProvider nextPage, IFancyUIProvider nextHomePage) {
        if (!this.previousPages.isEmpty() && this.previousPages.peek().page == nextPage) {
            // In case the user manually navigates back one step, just remove it from the navigation stack
            this.previousPages.pop();
        } else if (this.currentPage != null) {
            this.previousPages.push(new NavigationEntry(this.currentPage, this.currentHomePage, () -> {}));
        }

        this.currentPage = nextPage;
        this.currentHomePage = nextHomePage;

        setupFancyUI(nextPage, nextPage.hasPlayerInventory());
    }

    protected void navigateBack(ClickData clickData) {
        NavigationEntry navigationEntry = previousPages.pop();

        this.currentPage = navigationEntry.page;
        this.currentHomePage = navigationEntry.homePage;

        setupFancyUI(this.currentPage, this.currentPage.hasPlayerInventory());
        navigationEntry.onNavigation.run();
    }
}
