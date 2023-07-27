package com.gregtechceu.gtceu.utils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextFormattingUtil {
    public static List<Component> getTranslatableMultilineText(String key) {
        return getMultilineKeys(key).stream()
                .map(Component::translatable)
                .collect(Collectors.toList());
    }

    private static List<String> getMultilineKeys(String key) {
        if (!I18n.exists(key + ".0")) {
            return List.of(key);
        }

        List<String> keys = new ArrayList<>();

        int index = 0;
        while (I18n.exists(key + '.' + index)) {
            keys.add(key + '.' + index);
            index++;
        }

        return keys;
    }
}
