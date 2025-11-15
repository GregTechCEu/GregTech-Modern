package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;

import java.util.Objects;

public class WidgetUtil {

    public static IWidget getWidget(ParentWidget<?> parent, String name) {
        for (IWidget child : parent.getChildren()) {
            if (Objects.equals(child.getName(), name)) {
                return child;
            }
            if (child instanceof ParentWidget<?> childParent) {
                IWidget found = getWidget(childParent, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
