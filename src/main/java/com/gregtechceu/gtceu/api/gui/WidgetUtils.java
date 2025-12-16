package com.gregtechceu.gtceu.api.gui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class WidgetUtils {

    public static List<Widget> getWidgetsById(WidgetGroup group, String regex) {
        return group.getWidgetsById(Pattern.compile(regex));
    }

    @Nullable
    public static Widget getFirstWidgetById(WidgetGroup group, String regex) {
        return group.getFirstWidgetById(Pattern.compile(regex));
    }

    public static void widgetByIdForEach(WidgetGroup group, String regex, Consumer<Widget> consumer) {
        getWidgetsById(group, regex).forEach(consumer);
    }

    public static <T extends Widget> void widgetByIdForEach(WidgetGroup group, String regex, Class<T> clazz,
                                                            Consumer<T> consumer) {
        for (Widget widget : getWidgetsById(group, regex)) {
            if (clazz.isInstance(widget)) {
                consumer.accept(clazz.cast(widget));
            }
        }
    }

    public static int widgetIdIndex(Widget widget) {
        var id = widget.getId();
        if (id == null) return -1;
        var split = id.split("_");
        if (split.length == 0) return -1;
        var end = split[split.length - 1];
        try {
            return Integer.parseInt(end);
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getInventoryHeight(boolean includeHotbar) {
        return 64 + (includeHotbar ? 22 : 0);
    }
}
