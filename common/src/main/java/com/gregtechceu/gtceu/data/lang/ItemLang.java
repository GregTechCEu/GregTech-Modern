package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class ItemLang {

    public static void init(RegistrateLangProvider provider) {
        initGeneratedNames(provider);
        initItemNames(provider);
        initItemTooltips(provider);
    }

    private static void initGeneratedNames(RegistrateLangProvider provider) {
        // Materials
        for (Material material : GTRegistries.MATERIALS) {
            provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
        }
        // RecipeTypes
        for (var recipeType : GTRegistries.RECIPE_TYPES) {
            provider.add(recipeType.registryName.toLanguageKey(), toEnglishName(recipeType.registryName.getPath()));
        }
        // TagPrefix
        for (TagPrefix tagPrefix : TagPrefix.values()) {
            provider.add(tagPrefix.getUnlocalizedName(), tagPrefix.langValue);
        }
        // GTToolType
        for (GTToolType toolType : GTToolType.values()) {
            provider.add(toolType.getUnlocalizedName(), toEnglishName(toolType));
        }
    }

    private static void initItemNames(RegistrateLangProvider provider) {

    }

    private static void initItemTooltips(RegistrateLangProvider provider) {

    }
}
