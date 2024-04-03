package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLangGenerator {

    public static void generate(RegistrateLangProvider provider, MaterialRegistry registry) {
        for (Material material : registry.getAllMaterials()){
            provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
            provider.add("block.gtceu."+material.getName()+"_indicator", toEnglishName(material.getName())+" Surface Rock");
        }
    }
}
