package brachy.modularui.api;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.Predicate;

public interface ITreeNode<T extends ITreeNode<T>> {

    T getParent();

    default boolean hasParent() {
        return getParent() != null;
    }

    List<T> getChildren();

    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    default boolean visitChildren(Predicate<T> visitor) {
        for (T w : getChildren()) {
            if (!visitor.test(w)) return false;
        }
        return true;
    }

    default boolean visitAllChildrenBFS(Predicate<T> visitor) {
        ObjectList<T> parents = new ObjectArrayList<>();
        parents.add((T) this);
        while (!parents.isEmpty()) {
            parents.removeFirst().visitChildren(child -> {
                if (child.hasChildren()) parents.addLast(child);
                return visitor.test(child);
            });
        }
        return true;
    }

    default boolean visitAllChildrenDFS(Predicate<T> visitor) {
        return visitChildren(child -> {
            if (!visitor.test(child)) return false;
            return !child.hasChildren() || child.visitAllChildrenDFS(visitor);
        });
    }
}
