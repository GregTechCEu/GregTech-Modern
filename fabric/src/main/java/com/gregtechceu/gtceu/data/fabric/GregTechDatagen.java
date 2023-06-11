package com.gregtechceu.gtceu.data.fabric;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote GregTechDatagen
 */
public class GregTechDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        // registrate
        var rootPath = FabricLoader.getInstance().getGameDir().normalize().getParent().getParent();
        ExistingFileHelper helper = ExistingFileHelper.withResources(
                rootPath.resolve("common").resolve("src").resolve("main").resolve("resources"),
                rootPath.resolve("fabric").resolve("src").resolve("main").resolve("resources"));
        GTRegistries.REGISTRATE.setupDatagen(generator, helper);
        // sound
        var provider = new SoundEntryBuilder.SoundEntryProvider(generator);
        generator.addProvider(true, provider);
        // biome tags
        generator.addProvider(true, BiomeTagsProviderImpl::new);
    }
}
