package com.gregtechceu.gtceu.api.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Consumer;

public class CustomSoundEntry extends SoundEntry {

    protected List<ResourceLocation> variants;
    protected SoundEvent event;

    public CustomSoundEntry(ResourceLocation id, List<ResourceLocation> variants, String subtitle,
                            SoundSource category, int attenuationDistance) {
        super(id, subtitle, category, attenuationDistance);
        this.variants = variants;
    }

    @Override
    public void prepare() {
        event = SoundEvent.createVariableRangeEvent(id);
    }

    @Override
    public void register(Consumer<SoundEvent> registry) {
        registry.accept(event);
    }

    @Override
    public SoundEvent getMainEvent() {
        return event;
    }

    @Override
    public void write(JsonObject json) {
        JsonObject entry = new JsonObject();
        JsonArray list = new JsonArray();

        JsonObject s = new JsonObject();
        s.addProperty("name", id.toString());
        s.addProperty("type", "file");
        if (attenuationDistance != 0)
            s.addProperty("attenuation_distance", attenuationDistance);
        list.add(s);

        for (ResourceLocation variant : variants) {
            s = new JsonObject();
            s.addProperty("name", variant.toString());
            s.addProperty("type", "file");
            if (attenuationDistance != 0)
                s.addProperty("attenuation_distance", attenuationDistance);
            list.add(s);
        }

        entry.add("sounds", list);
        if (hasSubtitle())
            entry.addProperty("subtitle", getSubtitleKey());
        json.add(id.getPath(), entry);
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
