package com.gregtechceu.gtceu.api.sound;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote ConfiguredSoundEvent
 */
public record ConfiguredSoundEvent(Supplier<SoundEvent> event, float volume, float pitch) {
}
