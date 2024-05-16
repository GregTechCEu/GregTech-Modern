package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.data.tags.BiomeTagsLoader;
import com.gregtechceu.gtceu.data.tags.EntityTypeTagLoader;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            // sound
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(generator, GTCEu.MOD_ID));
            // compass
            generator.addProvider(true, new CompassSection.CompassSectionProvider(generator, event.getExistingFileHelper()));
            generator.addProvider(true, new CompassNode.CompassNodeProvider(generator, event.getExistingFileHelper()));
            // biome
            generator.addProvider(true, new BiomeTagsLoader(generator, event.getExistingFileHelper()));
        }
    }
}
