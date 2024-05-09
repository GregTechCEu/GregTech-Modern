package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.materials.material.Material;
import com.gregtechceu.gtceu.api.materials.material.registry.MaterialRegistry;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLangGenerator {

    public static void generate(RegistrateLangProvider provider, MaterialRegistry registry) {
        for (Material material : registry.getAllMaterials())
            provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
    }
}
