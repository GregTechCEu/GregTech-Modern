package com.gregtechceu.gtceu.api.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Consumer;

public class ExistingSoundEntry extends SoundEntry {

    protected List<ResourceLocation> variants;
    protected SoundEvent event;

    public ExistingSoundEntry(SoundEvent event, SoundSource category) {
        super(event.getLocation(), "", category, 0);
        this.event = event;
    }

    @Override
    public void prepare() {
        throw new RuntimeException();
    }

    @Override
    public void register(Consumer<SoundEvent> registry) {
        throw new RuntimeException();
    }

    @Override
    public SoundEvent getMainEvent() {
        return event;
    }

    @Override
    public void write(JsonObject json) {
        throw new RuntimeException();
    }

    @Override
    public void play(Level world, Player entity, double x, double y, double z, float volume, float pitch) {
        world.playSound(entity, x, y, z, event, category, volume, pitch);
    }

    @Override
    public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
        world.playLocalSound(x, y, z, event, category, volume, pitch, fade);
    }
}
