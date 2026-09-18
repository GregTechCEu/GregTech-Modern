package brachy.modularui.widgets;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.TabTexture;
import brachy.modularui.theme.SelectableTheme;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class PageButton extends Widget<PageButton> implements Interactable {

    @Getter private final int index;
    private final PagedWidget.Controller controller;
    private IDrawable inactiveTexture = null;
    @Getter private boolean invert = false;

    public PageButton(int index, PagedWidget.Controller controller) {
        this.index = index;
        this.controller = controller;
        disableHoverBackground();
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getToggleButtonTheme();
    }

    @Override
    protected WidgetTheme getActiveWidgetTheme(WidgetThemeEntry<?> widgetTheme, boolean hover) {
        SelectableTheme selectableTheme = widgetTheme.expectType(SelectableTheme.class).getTheme(hover);
        return isActive() ^ invertSelected() ? selectableTheme : selectableTheme.getSelected();
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        if (!isActive()) {
            this.controller.setPage(this.index);
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public IDrawable getBackground() {
        return isActive() || this.inactiveTexture == null ? super.getBackground() : this.inactiveTexture;
    }

    public boolean isActive() {
        return this.controller.getActivePageIndex() == this.index;
    }

    public PageButton background(boolean active, IDrawable... background) {
        if (active) {
            return background(background);
        }
        if (background.length == 0) {
            this.inactiveTexture = null;
        } else if (background.length == 1) {
            this.inactiveTexture = background[0];
        } else {
            this.inactiveTexture = new DrawableStack(background);
        }
        return this;
    }

    public PageButton tab(TabTexture texture, int location) {
        return background(invertSelected(), texture.get(location, invertSelected()))
                .background(!invertSelected(), texture.get(location, !invertSelected()))
                .disableHoverBackground()
                .size(texture.getWidth(), texture.getHeight());
    }

    public PageButton invertSelected(boolean invert) {
        this.invert = invert;
        return getThis();
    }

    public boolean invertSelected() {
        return this.invert;
    }
}
