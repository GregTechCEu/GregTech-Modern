package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.core.mixins.RegistrateDataProviderAccessor;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.data.model.BlockstateModelLoader;
import com.gregtechceu.gtceu.data.tags.*;

import com.tterrag.registrate.providers.ProviderType;

public class GregTechDatagen {

    public static final ProviderType<GTBlockstateProvider> BLOCKSTATE_PROVIDER = ProviderType.register("ex_blockstate",
            (registrate, event, existing) -> new GTBlockstateProvider(registrate,
                    event.getGenerator().getPackOutput(), event.getExistingFileHelper(), existing));

    public static void initPre() {
        // replace the default blockstate provider with ours
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("blockstate", BLOCKSTATE_PROVIDER);

        GTRegistration.REGISTRATE.addDataGenerator(BLOCKSTATE_PROVIDER, BlockstateModelLoader::init);
    }

    public static void initPost() {

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
