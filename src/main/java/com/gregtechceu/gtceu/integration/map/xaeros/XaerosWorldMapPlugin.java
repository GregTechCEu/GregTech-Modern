package com.gregtechceu.gtceu.integration.map.xaeros;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class XaerosWorldMapPlugin {

    public static boolean isActive = false;

    public static final Object2BooleanMap<String> OPTIONS = new Object2BooleanOpenHashMap<>();

    public static void init() {
        isActive = true;
    }

    public static void toggleOption(String name, boolean active) {
        OPTIONS.put(name, active);
    }

    public static boolean getOptionValue(String name) {
        return OPTIONS.getBoolean(name);
    }
}
