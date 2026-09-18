package brachy.modularui.screen;

import net.neoforged.bus.api.Event;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

/**
 * An event that is invoked after a panel has been build.
 * Sub panels can be completely replaced using the {@link SubPanel sub panel event}.
 * Main panels can not be replaced, but it's children can be by using
 * {@link brachy.modularui.api.widget.IWidget#visitTransformAllChildren(UnaryOperator) IWidget.visitTransformAllChildren(UnaryOperator)}.
 */
@ApiStatus.NonExtendable
public class BuildPanelEvent extends Event {

    @Getter private final ModularScreen screen;
    @Getter private final ModularPanel<?> mainPanel;
    @Getter protected ModularPanel<?> openingPanel;

    protected BuildPanelEvent(ModularScreen screen, ModularPanel<?> openingPanel) {
        this.screen = screen;
        this.mainPanel = screen.getMainPanel();
        this.openingPanel = openingPanel;
    }

    public String getScreenName() {
        return this.screen.getOwner();
    }

    public String getMainPanelName() {
        return this.mainPanel.getName();
    }

    public String getOpeningPanelName() {
        return this.openingPanel.getName();
    }

    public boolean matches(String screenOwner) {
        return getScreenName().equals(screenOwner);
    }

    public boolean matches(String screenOwner, String mainPanel) {
        return getScreenName().equals(screenOwner) && getMainPanelName().equals(mainPanel);
    }

    public boolean matches(String screenOwner, String mainPanel, String openingPanel) {
        return getScreenName().equals(screenOwner) && getMainPanelName().equals(mainPanel) && getOpeningPanelName().equals(openingPanel);
    }

    public static final class MainPanel extends BuildPanelEvent {

        @ApiStatus.Internal
        public MainPanel(ModularScreen screen) {
            super(screen, screen.getMainPanel());
        }
    }

    public static final class SubPanel extends BuildPanelEvent {

        @ApiStatus.Internal
        public SubPanel(ModularScreen screen, ModularPanel<?> openingPanel) {
            super(screen, openingPanel);
        }

        public void setOpeningPanel(ModularPanel<?> openingPanel) {
            this.openingPanel = openingPanel;
        }
    }
}
