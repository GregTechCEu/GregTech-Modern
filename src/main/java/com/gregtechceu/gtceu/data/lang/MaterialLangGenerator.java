package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLangGenerator {

    public static void generate(RegistrateLangProvider provider, String modId) {
        for (var material : GTRegistries.MATERIALS) {
            if (material.getModid().equals(modId)) {
                provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
            }
        }
    }
}
