package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.value.IConfigValue;
import dev.toma.configuration.config.value.IHierarchical;

import java.util.*;

public class ConfigurationLang {

    public static void init(GTLangProvider provider) {
        recurseGenerateConfigLang(provider, Configuration.getConfig(GTCEu.MOD_ID).get().values());
    }

    private static void recurseGenerateConfigLang(GTLangProvider provider,
                                                  Collection<? extends IConfigValue<?>> values) {
        for (var entry : values) {
            provider.add("config.gtceu.option." + entry.getPath(), entry.getId());
            if (entry instanceof IHierarchical hierarchical) {
                var children = hierarchical.getChildrenKeys().stream()
                        .map(hierarchical::getChildById)
                        .filter(Objects::nonNull)
                        .toList();
                recurseGenerateConfigLang(provider, children);
            }
        }
    }
}
