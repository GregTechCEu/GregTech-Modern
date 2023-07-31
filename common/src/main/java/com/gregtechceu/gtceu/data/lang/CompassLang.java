package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.tterrag.registrate.providers.RegistrateLangProvider;

/**
 * @author KilaBash
 * @date 2023/7/31
 * @implNote CompassLang
 */
public class CompassLang {
    public static void init(RegistrateLangProvider provider) {
        for (CompassSection section : GTRegistries.COMPASS_SECTIONS) {
            provider.add(section.getUnlocalizedKey(), section.lang());
        }
    }
}
