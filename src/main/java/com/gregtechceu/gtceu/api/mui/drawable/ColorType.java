package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Map;
import java.util.function.ToIntFunction;

public class ColorType {

    private static final Map<String, ColorType> COLOR_TYPES = new Object2ObjectOpenHashMap<>();

    public static ColorType get(String name) {
        return COLOR_TYPES.getOrDefault(name, DEFAULT);
    }

    public static final ColorType DEFAULT = new ColorType("default", WidgetTheme::getColor);
    public static final ColorType TEXT = new ColorType("text", WidgetTheme::getTextColor);
    public static final ColorType ICON = new ColorType("default", WidgetTheme::getIconColor);

    @Getter
    private final String name;
    private final ToIntFunction<WidgetTheme> colorGetter;

    public ColorType(String name, ToIntFunction<WidgetTheme> colorGetter) {
        this.name = name;
        this.colorGetter = colorGetter;
        COLOR_TYPES.put(name, this);
    }

    public int getColor(WidgetTheme theme) {
        return colorGetter.applyAsInt(theme);
    }
}
