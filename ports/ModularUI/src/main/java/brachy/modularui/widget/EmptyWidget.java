package brachy.modularui.widget;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widget.sizer.StandardResizer;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class EmptyWidget implements IWidget {

    @Getter
    private final Area area = new Area();
    @Getter
    private final StandardResizer resizer = new StandardResizer(this);
    private boolean requiresResize = false;
    @Setter
    @Getter
    public boolean enabled = true;
    @Getter
    private IWidget parent;

    @Override
    public ModularScreen getScreen() {
        return null;
    }

    @Override
    public void initialise(@NotNull IWidget parent, boolean late) {
        this.parent = parent;
        getArea().z(parent.getArea().z() + 1);
    }

    @Override
    public void dispose() {
        this.parent = null;
    }

    @Override
    public boolean isValid() {
        return this.parent != null;
    }

    @Override
    public @NotNull ModularPanel<?> getPanel() {
        return this.parent.getPanel();
    }

    @Override
    public boolean visitChildren(Predicate<IWidget> visitor) {
        return true;
    }

    @Override
    public void scheduleResize() {
        this.requiresResize = true;
    }

    @Override
    public boolean requiresResize() {
        return this.requiresResize;
    }

    @Override
    public void onResized() {
        this.requiresResize = false;
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return false;
    }

    @Override
    public boolean canHover() {
        return false;
    }

    @Override
    public boolean canHoverThrough() {
        return true;
    }

    @Override
    public ModularGuiContext getContext() {
        return this.parent.getContext();
    }

    @Override
    public @NotNull StandardResizer resizer() {
        return this.resizer;
    }

    @Nullable
    @Override
    public String getName() {
        return null;
    }
}
