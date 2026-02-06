package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.ITreeNode;
import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ResizeNode implements IResizeable, ITreeNode<ResizeNode> {

    private ResizeNode defaultParent;
    private ResizeNode parentOverride;
    private final List<ResizeNode> children = new ArrayList<>();
    private boolean defaultParentIsDelegating = false;
    private boolean requiresResize = true;

    @ApiStatus.Internal
    @Override
    public List<ResizeNode> getChildren() {
        return children;
    }

    @Override
    public ResizeNode getParent() {
        return parentOverride != null ? parentOverride : defaultParent;
    }

    @ApiStatus.Internal
    public void replacementOf(ResizeNode node) {
        if (this == node) return;
        // GTCEu.LOGGER.info("Replacing resizer node {} with node {}", node, this);
        // remove this node from the tree by removing itself from the parents and removing the children
        if (this.defaultParent != null) this.defaultParent.children.remove(this);
        if (this.parentOverride != null) this.parentOverride.children.remove(this);
        for (ResizeNode n : this.children) {
            if (n.parentOverride == this) {
                n.setParentOverride(null);
            }
            if (n.defaultParent == this) {
                n.setDefaultParent(null);
            }
        }
        this.children.clear();

        // remove the node to replace from its tree and remember the exact position
        int defI = -1;
        int ovrI = -1;
        if (node.defaultParent != null) {
            defI = node.defaultParent.children.indexOf(node);
            if (defI >= 0) node.defaultParent.children.remove(defI);
        }
        if (node.parentOverride != null) {
            ovrI = node.parentOverride.children.indexOf(node);
            if (ovrI >= 0) node.parentOverride.children.remove(ovrI);
        }
        // take over the parent and ourselves to the new parents with the remembered position
        this.defaultParent = node.defaultParent;
        this.parentOverride = node.parentOverride;
        if (this.parentOverride != null) {
            if (ovrI < 0) throw new IllegalStateException();
            this.parentOverride.children.add(ovrI, this);
        }
        if (this.defaultParent != null) {
            if (defI < 0) throw new IllegalStateException();
            this.defaultParent.children.add(ovrI, this);
        }
        // take all children and update their parent to ourselves
        this.children.addAll(node.children);
        for (ResizeNode n : this.children) {
            if (n.parentOverride == node) n.parentOverride = this;
            if (n.defaultParent == node) n.defaultParent = this;
        }
        // finally invalidate replaced node
        node.dispose();
    }

    public void dispose() {
        if (getParent() != null) getParent().children.remove(this);
        this.defaultParent = null;
        this.parentOverride = null;
        this.children.clear();
    }

    private boolean removeFromParent(ResizeNode parent, ResizeNode parent2, ResizeNode replacement) {
        if (parent != null) {
            if (parent == replacement) return true;
            parent.children.remove(this);
        } else if (parent2 != null) {
            if (parent2 == replacement) return true;
            parent2.children.remove(this);
        }
        return false;
    }

    @ApiStatus.Internal
    public void initialize(ResizeNode defaultParent, ResizeNode root) {
        setDefaultParent(defaultParent);
    }

    @ApiStatus.Internal
    public void setDefaultParent(ResizeNode resizeNode) {
        // GTCEu.LOGGER.info("Set default parent of {} to {}. Current: default: {}, override: {}", this, resizeNode,
        // this.defaultParent, this.parentOverride);
        if (resizeNode == this) throw new IllegalArgumentException("Tried to set itself as default parent in " + this);
        if (removeFromParent(this.defaultParent, null, resizeNode)) return;
        this.defaultParent = resizeNode;
        if (this.parentOverride == null && resizeNode != null) {
            resizeNode.children.add(this);
        }
    }

    protected void setParentOverride(ResizeNode resizeNode) {
        // GTCEu.LOGGER.info("Set override parent of {} to {}. Current: default: {}, override: {}", this, resizeNode,
        // this.defaultParent, this.parentOverride);
        if (resizeNode == this) throw new IllegalArgumentException("Tried to set itself as parent override in " + this);
        if (removeFromParent(this.parentOverride, this.defaultParent, resizeNode)) return;
        this.parentOverride = resizeNode;
        if (this.parentOverride != null) {
            this.parentOverride.children.add(this);
        } else if (this.defaultParent != null) {
            this.defaultParent.children.add(this);
        }
    }

    @ApiStatus.Internal
    public void setDefaultParentIsDelegating(boolean defaultParentIsDelegating) {
        this.defaultParentIsDelegating = defaultParentIsDelegating;
    }

    public boolean hasParentOverride() {
        return this.parentOverride != null;
    }

    @Override
    public void initResizing(boolean onOpen) {
        if (this.defaultParentIsDelegating && this.parentOverride != null) {
            this.defaultParent.initResizing(onOpen);
        }
    }

    public void reset() {}

    public void markDirty() {
        this.requiresResize = true;
    }

    public void onResized() {
        this.requiresResize = false;
        if (this.defaultParentIsDelegating && this.parentOverride != null) {
            this.defaultParent.onResized();
        }
    }

    public void postFullResize() {
        if (this.defaultParentIsDelegating && this.parentOverride != null) {
            this.defaultParent.postFullResize();
        }
    }

    public boolean requiresResize() {
        return this.requiresResize;
    }

    public boolean dependsOnParentX() {
        return false;
    }

    public boolean dependsOnParentY() {
        return false;
    }

    public boolean dependsOnParent() {
        return dependsOnParentX() || dependsOnParentY();
    }

    public boolean dependsOnParent(GuiAxis axis) {
        return axis.isHorizontal() ? dependsOnParentX() : dependsOnParentY();
    }

    public boolean dependsOnChildrenX() {
        return false;
    }

    public boolean dependsOnChildrenY() {
        return false;
    }

    public boolean dependsOnChildren() {
        return dependsOnChildrenX() || dependsOnChildrenY();
    }

    public boolean dependsOnChildren(GuiAxis axis) {
        return axis.isHorizontal() ? dependsOnChildrenX() : dependsOnChildrenY();
    }

    public boolean isSameResizer(ResizeNode node) {
        return node == this;
    }

    public boolean isLayout() {
        return false;
    }

    public boolean layoutChildren() {
        return true;
    }

    public boolean postLayoutChildren() {
        return true;
    }

    @ApiStatus.Internal
    public void checkExpanded(@Nullable GuiAxis axis) {}

    public abstract boolean hasYPos();

    public abstract boolean hasXPos();

    public abstract boolean hasHeight();

    public abstract boolean hasWidth();

    public abstract boolean hasStartPos(GuiAxis axis);

    public abstract boolean hasEndPos(GuiAxis axis);

    public boolean hasPos(GuiAxis axis) {
        return axis.isHorizontal() ? hasXPos() : hasYPos();
    }

    public boolean hasSize(GuiAxis axis) {
        return axis.isHorizontal() ? hasWidth() : hasHeight();
    }

    public boolean isExpanded() {
        return false;
    }

    public abstract boolean isFullSize();

    public abstract boolean hasFixedSize();

    public abstract String getDebugDisplayName();

    @Override
    public abstract String toString();
}
