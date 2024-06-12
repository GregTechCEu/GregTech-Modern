package com.gregtechceu.gtceu.api.data.damagesource;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * An extension of {@link DamageType} that contains extra data, functionality, and utility.
 * 
 * @see DamageType
 * @see DamageSource
 * @see DamageSources
 */
public class DamageTypeData {

    private static final List<DamageTypeData> ALL = new ArrayList<>();

    public final ResourceKey<DamageType> key;
    public final ResourceLocation id;
    public final DamageType type;
    public final Collection<TagKey<DamageType>> tags;

    private Holder<DamageType> holder;

    protected DamageTypeData(ResourceKey<DamageType> key, DamageType type, Collection<TagKey<DamageType>> tags) {
        this.key = key;
        this.id = key.location();
        this.type = type;
        this.tags = tags;
    }

    public DamageSource source(LevelAccessor level) {
        return new DamageSource(getHolder(level));
    }

    public DamageSource source(LevelAccessor level, @Nullable Entity entity) {
        return new DamageSource(getHolder(level), entity);
    }

    public DamageSource source(LevelAccessor level, @Nullable Entity cause, @Nullable Entity direct) {
        return new DamageSource(getHolder(level), cause, direct);
    }

    private Holder<DamageType> getHolder(LevelAccessor level) {
        if (this.holder == null) {
            Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
            this.holder = registry.getHolderOrThrow(key);
        }
        return holder;
    }

    public boolean is(@Nullable DamageSource source) {
        return source != null && is(source.type());
    }

    public boolean is(DamageType type) {
        return this.type.equals(type);
    }

    public void register(BootstapContext<DamageType> ctx) {
        ctx.register(this.key, this.type);
    }

    public static Stream<DamageTypeData> allInNamespace(String namespace) {
        return ALL.stream().filter(data -> data.id.getNamespace().equals(namespace));
    }

    public static DamageTypeData.Builder builder() {
        return new DamageTypeData.Builder();
    }

    public static class Builder {

        // required
        private String msgId;
        private ResourceLocation location;
        // defaulted
        private float exhaustion = 0;
        private DamageScaling scaling = DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
        private DamageEffects effects = DamageEffects.HURT;
        private DeathMessageType deathMessageType = DeathMessageType.DEFAULT;
        private final List<TagKey<DamageType>> tags = new ArrayList<>();

        /**
         * Set the ResourceLocation or ID of this type. This is a required field.
         */
        public DamageTypeData.Builder location(ResourceLocation location) {
            this.location = location;
            return this;
        }

        public DamageTypeData.Builder location(String path) {
            return location(GTCEu.id(path));
        }

        /**
         * Set both the location and msgId of this type from one ResourceLocation.
         * The msgId is set to "namespace.path".
         */
        public DamageTypeData.Builder simpleId(ResourceLocation location) {
            location(location);
            return msgId(location.toLanguageKey());
        }

        public DamageTypeData.Builder simpleId(String path) {
            return simpleId(GTCEu.id(path));
        }

        /**
         * Set the message ID. this is used for death message lang keys. This is a required field.
         *
         * @see #deathMessageType(DeathMessageType)
         */
        public DamageTypeData.Builder msgId(String msgId) {
            this.msgId = msgId;
            return this;
        }

        /**
         * Set the exhaustion of this type. This is the amount of hunger that will be consumed when an entity is
         * damaged.
         */
        public DamageTypeData.Builder exhaustion(float exhaustion) {
            this.exhaustion = exhaustion;
            return this;
        }

        /**
         * Set the scaling of this type. This determines whether damage is increased based on difficulty or not.
         */
        public DamageTypeData.Builder scaling(DamageScaling scaling) {
            this.scaling = scaling;
            return this;
        }

        /**
         * Set the effects of this type. This determines the sound that plays when damaged.
         */
        public DamageTypeData.Builder effects(DamageEffects effects) {
            this.effects = effects;
            return this;
        }

        /**
         * Set the death message type of this damage type. This determines how a death message lang key is assembled.
         * <ul>
         * <li>{@link DeathMessageType#DEFAULT}: {@link DamageSource#getLocalizedDeathMessage}</li>
         * <li>{@link DeathMessageType#FALL_VARIANTS}: {@link CombatTracker#getFallMessage(CombatEntry, Entity)}</li>
         * <li>{@link DeathMessageType#INTENTIONAL_GAME_DESIGN}: "death.attack." + msgId, wrapped in brackets, linking
         * to MCPE-28723</li>
         * </ul>
         */
        @SuppressWarnings("JavadocReference")
        public DamageTypeData.Builder deathMessageType(DeathMessageType type) {
            this.deathMessageType = type;
            return this;
        }

        @SafeVarargs
        public final DamageTypeData.Builder tag(TagKey<DamageType>... tags) {
            Collections.addAll(this.tags, tags);
            return this;
        }

        public DamageTypeData build() {
            if (location == null) {
                throw new IllegalArgumentException("location is required");
            }
            if (msgId == null) {
                throw new IllegalArgumentException("msgId is required");
            }

            DamageTypeData data = new DamageTypeData(
                    ResourceKey.create(Registries.DAMAGE_TYPE, location),
                    new DamageType(msgId, scaling, exhaustion, effects, deathMessageType),
                    tags);
            ALL.add(data);
            return data;
        }
    }
}
