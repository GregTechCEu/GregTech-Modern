package com.gregtechceu.gtceu.data.lang;

public class LangUtils {

    public static String enabledBoolean(String baseKey, boolean bool) {
        return baseKey + (bool ? ".enabled" : ".disabled");
    }
}
