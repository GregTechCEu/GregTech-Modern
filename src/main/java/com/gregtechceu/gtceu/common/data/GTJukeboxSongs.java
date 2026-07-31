package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GTJukeboxSongs {

    public static final ResourceKey<JukeboxSong> SUS = create("sus");

    public static void bootstrap(BootstrapContext<JukeboxSong> ctx) {
        ctx.register(SUS, new JukeboxSong(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GTSoundEntries.SUS_RECORD.getMainEvent()),
                Component.translatable("item.gtceu.sus_record.desc"),
                820, 1));
    }

    private static ResourceKey<JukeboxSong> create(String path) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, GTCEu.id(path));
    }
}
