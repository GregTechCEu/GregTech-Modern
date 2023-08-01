package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackType;
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
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(generator));
            // compass
            generator.addProvider(true, new CompassSection.CompassSectionProvider(generator, rl -> event.getExistingFileHelper().exists(rl, PackType.CLIENT_RESOURCES)));
            generator.addProvider(true, new CompassNode.CompassNodeProvider(generator, rl -> event.getExistingFileHelper().exists(rl, PackType.CLIENT_RESOURCES)));
            // biome
            generator.addProvider(true, new BiomeTagsProviderImpl(generator, event.getExistingFileHelper()));
        }
    }
}