package com.gregtechceu.gtceu.api.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WrappedSoundEntry extends SoundEntry {

    private final List<ConfiguredSoundEvent> wrappedEvents;
    private final List<WrappedSoundEntry.CompiledSoundEvent> compiledEvents;

    public WrappedSoundEntry(ResourceLocation id, String subtitle,
                             List<ConfiguredSoundEvent> wrappedEvents, SoundSource category, int attenuationDistance) {
        super(id, subtitle, category, attenuationDistance);
        this.wrappedEvents = wrappedEvents;
        compiledEvents = new ArrayList<>();
    }

    @Override
    public void prepare() {
        for (int i = 0; i < wrappedEvents.size(); i++) {
            ConfiguredSoundEvent wrapped = wrappedEvents.get(i);
            ResourceLocation location = getIdOf(i);
            compiledEvents.add(new WrappedSoundEntry.CompiledSoundEvent(SoundEvent.createVariableRangeEvent(location),
                    wrapped.volume(), wrapped.pitch()));
        }
    }

    @Override
    public void register(Consumer<SoundEvent> registry) {
        for (WrappedSoundEntry.CompiledSoundEvent compiledEvent : compiledEvents) {
            registry.accept(compiledEvent.event());
        }
    }

    @Override
    public SoundEvent getMainEvent() {
        return compiledEvents.get(0).event();
    }

    protected ResourceLocation getIdOf(int i) {
        return new ResourceLocation(id.getNamespace(), i == 0 ? id.getPath() : id.getPath() + "_compounded_" + i);
    }

    @Override
    public void write(JsonObject json) {
        for (int i = 0; i < wrappedEvents.size(); i++) {
            ConfiguredSoundEvent event = wrappedEvents.get(i);
            JsonObject entry = new JsonObject();
            JsonArray list = new JsonArray();
            JsonObject s = new JsonObject();
            s.addProperty("name", event.event()
                    .get()
                    .getLocation()
                    .toString());
            s.addProperty("type", "event");
            if (attenuationDistance != 0)
                s.addProperty("attenuation_distance", attenuationDistance);
            list.add(s);
            entry.add("sounds", list);
            if (i == 0 && hasSubtitle())
                entry.addProperty("subtitle", getSubtitleKey());
            json.add(getIdOf(i).getPath(), entry);
        }
    }

    @Override
    public void play(Level world, Player entity, double x, double y, double z, float volume, float pitch) {
        for (WrappedSoundEntry.CompiledSoundEvent event : compiledEvents) {
            world.playSound(entity, x, y, z, event.event(), category, event.volume() * volume,
                    event.pitch() * pitch);
        }
    }

    @Override
    public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
        for (WrappedSoundEntry.CompiledSoundEvent event : compiledEvents) {
            world.playLocalSound(x, y, z, event.event(), category, event.volume() * volume,
                    event.pitch() * pitch, fade);
        }
    }

    private record CompiledSoundEvent(SoundEvent event, float volume, float pitch) {}
}
