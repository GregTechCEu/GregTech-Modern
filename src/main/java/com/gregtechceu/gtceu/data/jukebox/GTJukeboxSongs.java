package com.gregtechceu.gtceu.data.jukebox;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.sound.GTSoundEntries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

public class GTJukeboxSongs {

    public static void bootstrap(BootstrapContext<JukeboxSong> ctx) {
        ctx.register(ResourceKey.create(Registries.JUKEBOX_SONG, GTCEu.id("sus")), new JukeboxSong(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GTSoundEntries.SUS_RECORD.getMainEvent()),
                Component.translatable("item.gtceu.sus_record.desc"),
                820, 1));
    }
}
