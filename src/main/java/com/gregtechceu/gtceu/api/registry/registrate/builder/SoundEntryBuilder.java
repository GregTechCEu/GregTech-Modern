package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.ConfiguredSoundEvent;
import com.gregtechceu.gtceu.api.sound.CustomSoundEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.api.sound.WrappedSoundEntry;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class SoundEntryBuilder<P> extends AbstractBuilder<SoundEntry, SoundEntry, P, SoundEntryBuilder<P>> {

    public static class SoundEntryProvider implements DataProvider {

        private final PackOutput output;
        private final String modId;

        public SoundEntryProvider(PackOutput output, String modId) {
            this.output = output;
            this.modId = modId;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return generate(output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(modId), cache);
        }

        @Override
        public String getName() {
            return modId + "'s Custom Sounds";
        }

        public CompletableFuture<?> generate(Path path, CachedOutput cache) {
            JsonObject json = new JsonObject();
            try {
                for (SoundEntry sound : GTRegistries.SOUNDS) {
                    if (sound.getId().getNamespace().equals(modId)) sound.write(json);
                }
            } catch (Exception ignored) {}
            return DataProvider.saveStable(cache, json, path.resolve("sounds.json"));
        }
    }

    @Nullable
    protected String subtitle = "unregistered";
    protected SoundSource category = SoundSource.BLOCKS;
    protected List<ConfiguredSoundEvent> wrappedEvents;
    protected List<ResourceLocation> variants;
    protected int attenuationDistance;

    public SoundEntryBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback) {
        super(owner, parent, name, callback, GTRegistries.Keys.SOUND);
        wrappedEvents = new ArrayList<>();
        variants = new ArrayList<>();
    }

    public SoundEntryBuilder<P> subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public SoundEntryBuilder<P> attenuationDistance(int distance) {
        this.attenuationDistance = distance;
        return this;
    }

    public SoundEntryBuilder<P> noSubtitle() {
        this.subtitle = null;
        return this;
    }

    public SoundEntryBuilder<P> category(SoundSource category) {
        this.category = category;
        return this;
    }

    public SoundEntryBuilder<P> addVariant(String name) {
        return addVariant(GTCEu.id(name));
    }

    public SoundEntryBuilder<P> addVariant(ResourceLocation id) {
        variants.add(id);
        return this;
    }

    public SoundEntryBuilder<P> playExisting(Supplier<SoundEvent> event, float volume, float pitch) {
        wrappedEvents.add(new ConfiguredSoundEvent(event, volume, pitch));
        return this;
    }

    public SoundEntryBuilder<P> playExisting(SoundEvent event, float volume, float pitch) {
        return playExisting(() -> event, volume, pitch);
    }

    public SoundEntryBuilder<P> playExisting(SoundEvent event) {
        return playExisting(event, 1, 1);
    }

    @Override
    protected SoundEntry createEntry() {
        SoundEntry entry = wrappedEvents.isEmpty() ?
                new CustomSoundEntry(ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()), variants, subtitle, category, attenuationDistance) :
                new WrappedSoundEntry(ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()), subtitle, wrappedEvents, category, attenuationDistance);
        entry.prepare();
        entry.register(soundEvent -> Registry.register(BuiltInRegistries.SOUND_EVENT, soundEvent.getLocation(), soundEvent));
        return entry;
    }

    @Override
    public RegistryEntry<SoundEntry, SoundEntry> register() {
        return super.register();
    }
}
