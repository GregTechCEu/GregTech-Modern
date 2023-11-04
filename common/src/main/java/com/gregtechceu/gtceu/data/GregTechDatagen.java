package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.data.tags.TagsHandler;
import com.tterrag.registrate.providers.ProviderType;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote GregTechDatagen
 */
public class GregTechDatagen {
    public static void init() {
        GTRegistries.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, TagsHandler::initItem);
        GTRegistries.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, TagsHandler::initBlock);
        GTRegistries.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, TagsHandler::initFluid);
        GTRegistries.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
