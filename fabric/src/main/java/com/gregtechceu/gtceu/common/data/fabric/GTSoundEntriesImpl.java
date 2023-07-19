package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote GTSoundsImpl
 */
public class GTSoundEntriesImpl {
    public static void registerSounds() {
        for (SoundEntry entry : GTRegistries.SOUNDS) {
            entry.register(soundEvent -> Registry.register(BuiltInRegistries.SOUND_EVENT, soundEvent.getLocation(), soundEvent));
        }
    }
}
