package brachy.modularui.widgets.menu;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.ParentWidget;

public class Menu<W extends Menu<W>> extends ParentWidget<W> implements IMenuPart {

    private AbstractMenuButton<?> menuSource;

    void setMenuSource(AbstractMenuButton<?> source) {
        this.menuSource = source;
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose(true, true);
    }

    public void checkClose(boolean soft, boolean requireNoHover) {
        if (this.menuSource == null) return;
        if (soft || requireNoHover) {
            if (this.menuSource.isBelowMouse() || isSelfOrChildHovered()) return;
        }
        this.menuSource.closeMenu(soft);
        this.menuSource.checkClose(soft, requireNoHover);
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        if (!child.resizer().hasHeight()) {
            child.resizer().height(12);
        }
        if (!child.resizer().hasWidth()) {
            child.resizer().widthRel(1f);
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.PANEL);
    }
}
