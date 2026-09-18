package brachy.modularui.widget;

import brachy.modularui.api.widget.IWidget;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class SingleChildWidget<W extends SingleChildWidget<W>> extends Widget<W> {

    @Getter
    private IWidget child;
    private List<IWidget> list = Collections.emptyList();

    @Override
    public @NotNull List<IWidget> getChildren() {
        return this.list;
    }

    private void updateList() {
        this.list = this.child == null ? Collections.emptyList() : Collections.singletonList(this.child);
    }

    public W child(IWidget child) {
        if (child == this || this.child == child) {
            return getThis();
        }

        if (this.child != null) {
            this.child.dispose();
        }

        this.child = child;
        updateList();
        if (child != null && isValid()) {
            child.initialise(this, true);
            scheduleResize();
        }
        onChildAdd(child);
        return getThis();
    }

    protected void onChildAdd(IWidget child) {}

    @Override
    public boolean visitChildren(Predicate<IWidget> visitor) {
        return visitor.test(this.child);
    }

    @Override
    public void visitTransformChildren(UnaryOperator<IWidget> op) {
        child(op.apply(this.child));
    }
}
