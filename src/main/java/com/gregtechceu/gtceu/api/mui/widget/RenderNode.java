package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

import java.util.List;

public class RenderNode implements WidgetNode<RenderNode> {

    private IWidget linkedWidget;
    private RenderNode parent;
    private List<RenderNode> children;

    @Override
    public IWidget getWidget() {
        return linkedWidget;
    }

    @Override
    public RenderNode getParent() {
        return parent;
    }

    @Override
    public List<RenderNode> getChildren() {
        return children;
    }
}
