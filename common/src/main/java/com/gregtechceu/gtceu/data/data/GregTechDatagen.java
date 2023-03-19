package com.gregtechceu.gtceu.data.data;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
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
        GTRegistries.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
