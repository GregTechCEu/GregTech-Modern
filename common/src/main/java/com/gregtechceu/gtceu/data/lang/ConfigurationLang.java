package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ConfigurationLang {

    public static void init(RegistrateLangProvider provider) {
        //todo configuration
//        dfs(provider, new HashSet<>(), Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getValueMap());
    }

//    private static void dfs(RegistrateLangProvider provider, Set<String> added, Map<String, ConfigValue<?>> map) {
//        for (var entry : map.entrySet()) {
//            var id = entry.getValue().getId();
//            if (added.add(id)) {
//                provider.add(String.format("config.%s.option.%s", GTCEu.MOD_ID, id), id);
//            }
//            if (entry.getValue() instanceof ObjectValue objectValue) {
//                dfs(provider, added, objectValue.get());
//            }
//        }
//    }
}
