package com.gregtechceu.gtceu.api.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

@OnlyIn(Dist.CLIENT)
public class AutoReleasedSound extends AbstractTickableSoundInstance {

    public final BooleanSupplier predicate;
    public final SoundEntry soundEntry;

    protected AutoReleasedSound(SoundEntry soundEntry, BooleanSupplier predicate, BlockPos pos, boolean loop, int delay,
                                float volume, float pitch) {
        super(soundEntry.getMainEvent(), soundEntry.category, Minecraft.getInstance().level.random);
        this.soundEntry = soundEntry;
        this.predicate = predicate;
        this.looping = loop;
        this.delay = delay;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuation = Attenuation.LINEAR;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    @Override
    public void tick() {
        if (!isStopped() && !predicate.getAsBoolean()) {
            release();
        }
    }

    public void release() {
        stop();
    }
}
