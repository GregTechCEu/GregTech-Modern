package com.gregtechceu.gtceu.common.data;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public class GTSoundTypes {

    public static final SoundType METAL_PIPE = new SoundType(1.0f, 1.0f, GTSoundEntries.METAL_PIPE.getMainEvent(),
            SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);
}
