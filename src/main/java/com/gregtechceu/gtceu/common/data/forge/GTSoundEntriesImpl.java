package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.simibubi.create.AllSoundEvents;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote GTSoundsImpl
 */
public class GTSoundEntriesImpl {
    public static void registerSounds() {
        for (SoundEntry entry : GTRegistries.SOUNDS) {
            entry.register(soundEvent -> ForgeRegistries.SOUND_EVENTS.register(soundEvent.getLocation(), soundEvent));
        }
    }

}
