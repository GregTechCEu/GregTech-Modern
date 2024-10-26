package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TitleBarWidget extends WidgetGroup {

    private static final int BORDER_WIDTH = 3;
    private static final int HORIZONTAL_MARGIN = 8;
    private static final int HEIGHT = 16;
    private static final int BTN_WIDTH = 18;

    private static final float ROLL_SPEED = 0.7f;

    private int width;
    private boolean showBackButton = false;
    private boolean showMenuButton = false;
    private final int innerHeight;

    /**
     * The button group is rendered behind the main section and contains the back and menu buttons.
     * <p>
     * For easier texture reuse, the background is applied to the group itself, instead of the individual buttons.<br>
     * The button group therefore needs to be rendered behind the main section.
     */
    private final WidgetGroup buttonGroup;
    private final Widget backButton;
    private final Widget menuButton;

    /**
     * The main section contains the current tab's icon and title text
     */
    private final WidgetGroup mainSection;
    private final ImageWidget tabIcon;
    private final ImageWidget tabTitle;
    private TextTexture titleText;

    public TitleBarWidget(int parentWidth, Consumer<ClickData> onBackClicked, Consumer<ClickData> onMenuClicked) {
        super(HORIZONTAL_MARGIN, -HEIGHT, parentWidth, HEIGHT);
        this.innerHeight = HEIGHT - BORDER_WIDTH;
        this.width = parentWidth - (2 * HORIZONTAL_MARGIN);

        addWidget(this.buttonGroup = new WidgetGroup(0, BORDER_WIDTH, width, innerHeight));
        buttonGroup.setBackground(GuiTextures.TITLE_BAR_BACKGROUND);
        buttonGroup.addWidget(this.backButton = new ButtonWidget(0, BORDER_WIDTH, BTN_WIDTH, HEIGHT - BORDER_WIDTH,
                new TextTexture(" <").setDropShadow(false).setColor(ChatFormatting.BLACK.getColor()), onBackClicked)
                .setHoverTooltips("gtceu.gui.title_bar.back"));
        buttonGroup.addWidget(this.menuButton = new ButtonWidget(width - BTN_WIDTH, BORDER_WIDTH, BTN_WIDTH,
                HEIGHT - BORDER_WIDTH,
                new TextTexture("+").setDropShadow(false).setColor(ChatFormatting.BLACK.getColor()), onMenuClicked)
                .setHoverTooltips("gtceu.gui.title_bar.page_switcher"));

        addWidget(this.mainSection = new WidgetGroup(BTN_WIDTH, 0, width, HEIGHT));
        mainSection.setBackground(GuiTextures.TITLE_BAR_BACKGROUND);
        mainSection.addWidget(this.tabIcon = new ImageWidget(
                BORDER_WIDTH + 1, BORDER_WIDTH + 1,
                innerHeight - 2, innerHeight - 2,
                IGuiTexture.EMPTY));
        mainSection.addWidget(this.tabTitle = new ImageWidget(
                BORDER_WIDTH + innerHeight, BORDER_WIDTH, 0, 0, IGuiTexture.EMPTY));
    }

    public void updateState(IFancyUIProvider currentPage, boolean showBackButton, boolean showMenuButton) {
        this.showBackButton = showBackButton;
        this.showMenuButton = showMenuButton;

        titleText = new TextTexture(ChatFormatting.BLACK.toString() + currentPage.getTitle().copy().getString())
                .setDropShadow(false)
                .setType(TextTexture.TextType.ROLL);
        titleText.setRollSpeed(ROLL_SPEED);

        tabIcon.setImage(currentPage.getTabIcon());
        tabTitle.setImage(titleText);

        backButton.setVisible(showBackButton);
        backButton.setActive(showBackButton);

        menuButton.setVisible(showMenuButton);
        menuButton.setActive(showMenuButton);

        onSizeUpdate();
    }

    @Override
    protected void onSizeUpdate() {
        this.width = getSize().getWidth() - (2 * HORIZONTAL_MARGIN);

        var hiddenButtons = 2;
        if (showBackButton) hiddenButtons--;
        if (showMenuButton) hiddenButtons--;

        int buttonGroupWidth = this.width - (BTN_WIDTH * hiddenButtons);
        buttonGroup.setSize(new Size(buttonGroupWidth, innerHeight));
        buttonGroup.setSelfPosition(new Position(showBackButton ? 0 : BTN_WIDTH, BORDER_WIDTH));
        menuButton.setSelfPosition(new Position(buttonGroupWidth - BTN_WIDTH, BORDER_WIDTH));

        int mainSectionWidth = this.width - (BTN_WIDTH * 2);
        int titleWidth = mainSectionWidth - (2 * BORDER_WIDTH) - innerHeight;
        mainSection.setSize(new Size(mainSectionWidth, HEIGHT));
        titleText.setWidth(titleWidth);
        tabTitle.setSize(new Size(titleWidth, HEIGHT - BORDER_WIDTH));
    }
}
