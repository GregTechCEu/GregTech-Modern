package com.gregtechceu.gtceu.api.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class SoundEntry {

    protected ResourceLocation id;
    protected String subtitle;
    protected SoundSource category;
    protected int attenuationDistance;

    public SoundEntry(ResourceLocation id, String subtitle, SoundSource category, int attenuationDistance) {
        this.id = id;
        this.subtitle = subtitle;
        this.category = category;
        this.attenuationDistance = attenuationDistance;
    }

    public abstract void prepare();

    public abstract void register(Consumer<SoundEvent> registry);

    public abstract void write(JsonObject json);

    public abstract SoundEvent getMainEvent();

    public String getSubtitleKey() {
        return id.getNamespace() + ".subtitle." + id.getPath();
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean hasSubtitle() {
        return subtitle != null;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void playOnServer(Level world, Vec3i pos) {
        playOnServer(world, pos, 1, 1);
    }

    public void playOnServer(Level world, Vec3i pos, float volume, float pitch) {
        play(world, null, pos, volume, pitch);
    }

    public void play(Level world, Player entity, Vec3i pos) {
        play(world, entity, pos, 1, 1);
    }

    public void playFrom(Entity entity) {
        playFrom(entity, 1, 1);
    }

    public void playFrom(Entity entity, float volume, float pitch) {
        if (!entity.isSilent())
            play(entity.level(), null, entity.blockPosition(), volume, pitch);
    }

    public void play(Level world, Player entity, Vec3i pos, float volume, float pitch) {
        play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
    }

    public void play(Level world, Player entity, Vec3 pos, float volume, float pitch) {
        play(world, entity, pos.x(), pos.y(), pos.z(), volume, pitch);
    }

    public abstract void play(Level world, Player entity, double x, double y, double z, float volume, float pitch);

    public void playAt(Level world, Vec3i pos, float volume, float pitch, boolean fade) {
        playAt(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, fade);
    }

    public void playAt(Level world, Vec3 pos, float volume, float pitch, boolean fade) {
        playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
    }

    public abstract void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade);

    @OnlyIn(Dist.CLIENT)
    public AutoReleasedSound playAutoReleasedSound(BooleanSupplier predicate, BlockPos pos, boolean loop, int delay,
                                                   float volume, float pitch) {
        var sound = new AutoReleasedSound(this, predicate, pos, loop, delay, volume, pitch);
        Minecraft.getInstance().getSoundManager().play(sound);
        return sound;
    }
}
