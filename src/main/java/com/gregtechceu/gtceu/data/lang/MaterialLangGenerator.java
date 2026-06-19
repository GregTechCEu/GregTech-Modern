package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.GTCEuAPI;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLangGenerator {

    public static void generate(RegistrateLangProvider provider, final String modId) {
        GTCEuAPI.materialManager.stream()
                .filter(mat -> mat.getModid().equals(modId))
                .forEach(material -> {
                    provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
                });
    }
}
