package com.gregtechceu.gtceu.data.datagen.lang;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.toma.configuration.config.value.ConfigValue;
import dev.toma.configuration.config.value.ObjectValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationLang {

    public static void init(RegistrateLangProvider provider) {
        dfs(provider, new HashSet<>(), ConfigHolder.INTERNAL_INSTANCE.getValueMap());
    }

    private static void dfs(RegistrateLangProvider provider, Set<String> added, Map<String, ConfigValue<?>> map) {
        for (var entry : map.entrySet()) {
            var id = entry.getValue().getId();
            if (added.add(id)) {
                provider.add(String.format("config.%s.option.%s", GTCEu.MOD_ID, id), id);
            }
            if (entry.getValue() instanceof ObjectValue objectValue) {
                dfs(provider, added, objectValue.get());
            }
        }
    }
}
