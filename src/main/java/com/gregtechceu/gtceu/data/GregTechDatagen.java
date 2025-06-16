package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.data.tags.*;

import com.tterrag.registrate.providers.ProviderType;

public class GregTechDatagen {

    public static final ProviderType<GTBlockstateProvider> BLOCKSTATE_PROVIDER = ProviderType.register("ex_blockstate",
            (p, e) -> new GTBlockstateProvider(p,
                    e.getGenerator().getPackOutput(), e.getExistingFileHelper()));

    public static void init() {
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
