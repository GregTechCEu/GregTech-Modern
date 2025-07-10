package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class FancyMachineUIWidget extends WidgetGroup {

    protected final TitleBarWidget titleBar;
    protected final VerticalTabsWidget sideTabsWidget;
    protected final WidgetGroup pageContainer;
    protected final PageSwitcher pageSwitcher;
    protected final ConfiguratorPanel configuratorPanel;
    protected final TooltipsPanel tooltipsPanel;

    @Nullable
    protected final PlayerInventoryWidget playerInventory;
    @Setter
    protected int border = 4;

    protected final IFancyUIProvider mainPage;

    /*
     * Current Page: The page visible in the UI
     * Current Home Page: The currently selected multiblock part's home page.
     */
    protected IFancyUIProvider currentPage;
    protected IFancyUIProvider currentHomePage;

    protected List<IFancyUIProvider> allPages;

    protected Deque<NavigationEntry> previousPages = new ArrayDeque<>();

    protected record NavigationEntry(IFancyUIProvider page, IFancyUIProvider homePage, Runnable onNavigation) {}

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
        this.pageSwitcher = new PageSwitcher(this::switchPage);

        setBackground(GuiTextures.BACKGROUND.copy()
                .setColor(Long.decode(ConfigHolder.INSTANCE.client.defaultUIColor).intValue() | 0xFF000000));
    }

    @Override
    public void initWidget() {
        super.initWidget();

        if (this.playerInventory != null) {
            this.playerInventory.setPlayer(gui.entityPlayer);
        }

        this.allPages = Stream.concat(Stream.of(this.mainPage), this.mainPage.getSubTabs().stream()).toList();

        performNavigation(this.mainPage, this.mainPage);
    }

    ////////////////////////////////////////
    // ********* NAVIGATION *********//
    ////////////////////////////////////////

    protected void navigate(IFancyUIProvider newPage) {
        navigate(newPage, this.currentHomePage);
    }

    protected void navigate(IFancyUIProvider nextPage, IFancyUIProvider nextHomePage) {
        if (nextPage != mainPage) {
            if (!this.previousPages.isEmpty() && this.previousPages.peek().page == nextPage) {
                // In case the user manually navigates back one step, just remove it from the navigation stack
                this.previousPages.pop();
            } else if (this.currentPage != null) {
                this.previousPages.push(new NavigationEntry(this.currentPage, this.currentHomePage, () -> {}));
            }
        } else {
            this.previousPages.clear();
        }

        performNavigation(nextPage, nextHomePage);
    }

    protected void navigateBack(ClickData clickData) {
        NavigationEntry navigationEntry = previousPages.pop();

        performNavigation(navigationEntry.page, navigationEntry.homePage);
        navigationEntry.onNavigation.run();
    }

    protected void performNavigation(IFancyUIProvider nextPage, IFancyUIProvider nextHomePage) {
        if (currentHomePage != nextHomePage)
            setupSideTabs(nextHomePage);

        this.currentPage = nextPage;
        this.currentHomePage = nextHomePage;

        if (currentPage != currentHomePage) {
            // Ensure the home page's basic layout is applied before navigating to another page:
            setupFancyUI(currentHomePage);
        }

        setupFancyUI(nextPage, nextPage.hasPlayerInventory());
    }

    ///////////////////////////////////////////////
    // *********** PAGE SWITCHER ***********//
    ///////////////////////////////////////////////

    protected void openPageSwitcher(ClickData clickData) {
        pageSwitcher.setPageList(allPages, currentHomePage);

        // If we're in another tab of the current page, ensure nav to its main tab when closing the page switcher:
        if (currentPage != currentHomePage && !previousPages.isEmpty()) {
            previousPages.pop();
        }

        this.sideTabsWidget.setVisible(false);
        this.sideTabsWidget.setActive(false);

        this.previousPages.push(new NavigationEntry(currentHomePage, currentHomePage, () -> {
            sideTabsWidget.setVisible(true);
            sideTabsWidget.setActive(true);
        }));

        this.currentPage = this.pageSwitcher;
        this.currentHomePage = this.pageSwitcher;

        setupFancyUI(this.pageSwitcher);
    }

    protected void switchPage(IFancyUIProvider nextHomePage) {
        // Ensure that the back button always leads back to the main page:
        this.currentHomePage = mainPage;
        this.currentPage = mainPage;
        this.previousPages.clear();

        sideTabsWidget.setVisible(true);
        sideTabsWidget.setActive(true);

        setupSideTabs(this.currentHomePage);
        navigate(nextHomePage, nextHomePage);
    }

    //////////////////////////////////////////////
    // *********** UI RENDERING ***********//
    //////////////////////////////////////////////

    protected void setupFancyUI(IFancyUIProvider fancyUI) {
        this.setupFancyUI(fancyUI, fancyUI.hasPlayerInventory());
    }

    protected void setupFancyUI(IFancyUIProvider fancyUI, boolean showInventory) {
        clearUI();

        sideTabsWidget.selectTab(fancyUI);
        titleBar.updateState(
                currentHomePage,
                !this.previousPages.isEmpty(),
                this.allPages.size() > 1 && this.currentPage != this.pageSwitcher);

        var page = fancyUI.createMainPage(this);

        // layout
        var size = new Size(Math.max(172, page.getSize().width + border * 2),
                Math.max(86, page.getSize().height + border * 2));
        setSize(new Size(size.width,
                size.height + (!showInventory || playerInventory == null ? 0 : playerInventory.getSize().height)));
        if (GTCEu.isClientSide() && getGui() != null) {
            getGui().setSize(getSize().width, getSize().height);
        }
        this.sideTabsWidget.setSize(new Size(24, size.height));
        this.pageContainer.setSize(size);
        this.tooltipsPanel.setSelfPosition(new Position(size.width + 2, 2));

        setupInventoryPosition(showInventory, size);

        // setup
        this.pageContainer.addWidget(page);
        page.setSelfPosition(new Position(
                (pageContainer.getSize().width - page.getSize().width) / 2,
                (pageContainer.getSize().height - page.getSize().height) / 2));
        fancyUI.attachConfigurators(configuratorPanel);
        configuratorPanel
                .setSelfPosition(new Position(-24 - 2, getGui().getHeight() - configuratorPanel.getSize().height - 4));
        fancyUI.attachTooltips(tooltipsPanel);

        titleBar.setSize(new Size(this.getSize().width, titleBar.getSize().height));
    }

    private void setupInventoryPosition(boolean showInventory, Size parentSize) {
        if (this.playerInventory == null)
            return;

        this.playerInventory.setSelfPosition(new Position(
                (parentSize.width - playerInventory.getSize().width) / 2,
                parentSize.height));

        this.playerInventory.setActive(showInventory);
        this.playerInventory.setVisible(showInventory);
    }

    protected void clearUI() {
        this.pageContainer.clearAllWidgets();
        this.configuratorPanel.clear();
        this.tooltipsPanel.clear();
    }

    protected void setupSideTabs(IFancyUIProvider currentHomePage) {
        this.sideTabsWidget.clearSubTabs();
        currentHomePage.attachSideTabs(sideTabsWidget);
    }
}
