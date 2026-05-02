package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.config.ConfigHolder;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.toma.configuration.config.value.IConfigValue;
import dev.toma.configuration.config.value.IHierarchical;

import java.util.HashSet;
import java.util.Set;

public class ConfigurationLang {

    public static void init(final RegistrateLangProvider provider) {
        final Set<String> added = new HashSet<>();
        ConfigHolder.INTERNAL_INSTANCE.values()
                .forEach((value) -> addTranslation(provider, added, value));
    }

    private static void addTranslation(RegistrateLangProvider provider, Set<String> added, IConfigValue<?> value) {
        var id = value.getId();
        if (added.add(id)) {
            provider.add("config.gtceu.option." + id, id);
        }
        if (value instanceof IHierarchical hierarchical) {
            for (String childKey : value.getChildrenKeys()) {
                IConfigValue<?> child = hierarchical.getChildById(childKey);
                if (child != null) {
                    addTranslation(provider, added, child);
                }
            }
        }
    }
}
