package com.lowdragmc.lowdraglib.utils;

import net.minecraft.client.resources.language.I18n;

public class LocalizationUtils {

    public static String format(String key, Object... args) {
        return I18n.get(key, args);
    }

    public static boolean exist(String key) {
        return I18n.exists(key);
    }
}
