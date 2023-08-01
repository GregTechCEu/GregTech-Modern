package com.gregtechceu.gtceu.data.fabric;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
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
        generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(generator));
        // compass
        generator.addProvider(true, new CompassSection.CompassSectionProvider(generator, rl -> helper.exists(rl, PackType.CLIENT_RESOURCES)));
        generator.addProvider(true, new CompassNode.CompassNodeProvider(generator, rl -> helper.exists(rl, PackType.CLIENT_RESOURCES)));
        // biome tags
        generator.addProvider(true, BiomeTagsProviderImpl::new);
    }
}
