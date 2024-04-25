package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class MaterialLangGenerator {

    public static void generate(RegistrateLangProvider provider, MaterialRegistry registry) {
        for (Material material : registry.getAllMaterials()) {
            if (material == GTMaterials.Iron3Chloride) {
                provider.add(material.getUnlocalizedName(), "Iron III Chloride");
            } else {
                provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
            }
        }
    }
}
