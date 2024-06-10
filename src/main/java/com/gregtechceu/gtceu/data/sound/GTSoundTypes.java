package com.gregtechceu.gtceu.data.sound;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;

public class GTSoundTypes {

    public static final SoundType METAL_PIPE = new DeferredSoundType(1.0f, 1.0f,
            () -> GTSoundEntries.METAL_PIPE.getMainEvent(),
            () -> SoundEvents.STONE_STEP, () -> SoundEvents.STONE_PLACE, () -> SoundEvents.STONE_HIT,
            () -> SoundEvents.STONE_FALL);
}
