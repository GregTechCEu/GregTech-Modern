package brachy.modularui.screen.viewport;

import it.unimi.dsi.fastutil.Hash;
import lombok.Getter;

import java.util.Objects;

public class LocatedElement<T> {

    @Getter
    private final T element;
    @Getter
    private final TransformationMatrix transformationMatrix;

    public LocatedElement(T element, TransformationMatrix transformationMatrix) {
        this.element = element;
        this.transformationMatrix = new TransformationMatrix(transformationMatrix, null);
    }

    public void applyMatrix(GuiContext context) {
        context.push(this.transformationMatrix);
    }

    public void unapplyMatrix(GuiContext context) {
        context.pop(this.transformationMatrix);
    }

    @Override
    public String toString() {
        return "LocatedElement[" + getElement() + "]";
    }

    public LocatedElementHashStrategy<T> createHashStrategy() {
        return new LocatedElementHashStrategy<>();
    }

    public static class LocatedElementHashStrategy<T> implements Hash.Strategy<LocatedElement<T>> {

        @Override
        public int hashCode(LocatedElement<T> o) {
            return Objects.hashCode(o == null ? null : o.element);
        }

        @Override
        public boolean equals(LocatedElement<T> a, LocatedElement<T> b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return Objects.equals(a.element, b.element);
        }
    }
}
