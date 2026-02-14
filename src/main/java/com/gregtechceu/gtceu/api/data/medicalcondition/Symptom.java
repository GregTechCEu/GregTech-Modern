package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.common.data.GTMobEffects;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Supplier;

public class Symptom {

    // spotless:off
    public static final UUID SYMPTOM_HEALTH_DEBUFF_UUID = UUID.fromString("607aa6d9-a7e4-4919-9962-f007104c4be8");
    public static final UUID SYMPTOM_MINING_FATIGUE_UUID = UUID.fromString("f2378ee6-3427-45b5-8440-4b797f7b664a");
    public static final UUID SYMPTOM_WEAKNESS_UUID = UUID.fromString("482e64e0-de77-49cd-b9bc-96b7e7eb16db");
    public static final UUID SYMPTOM_SLOWNESS_UUID = UUID.fromString("b3ac6b40-2d30-419f-9cac-5b2cf998ad72");

    public static final Symptom DEATH = new Symptom(defaultKey("death"), 1, 1.0f, 1.0f,
            (tracker, condition, configuredSymptom, baseSymptom, stage) -> {
                if (stage > 0) {
                    Player player = tracker.getPlayer();
                    // this should replicate the logic in LivingEntity#kill, but
                    // with the medical condition's damage type instead of `generic_kill`.
                    player.hurt(condition.getDamageSource(tracker), Float.MAX_VALUE);
                }
            });
    public static final Symptom RANDOM_DAMAGE = new Symptom(defaultKey("random_damage"), 10, 0.2f, 1.0f,
            Effect.NO_OP,
            (tracker, condition, configuredSymptom, baseSymptom, stage) -> {
                int stages = configuredSymptom.getStages();
                if (stage > 0 && tracker.getPlayer().getRandom().nextInt(stages * 500 / stage) == 0) {
                    tracker.getPlayer().hurt(condition.getDamageSource(tracker), 0.5f);
                }
            });
    // default is 20, stage 10 result will be 10
    // the health debuff stage is a special case because it has to resync the player's current health to the client
    public static final Symptom HEALTH_DEBUFF = new Symptom(defaultKey("health_debuff"), 10, 0.0f, 1.0f,
            (tracker, $1, $2, symptom, stage) -> {
                Player player = tracker.getPlayer();
                AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
                if (instance == null) {
                    return;
                }
                instance.removeModifier(SYMPTOM_HEALTH_DEBUFF_UUID);

                if (stage != 0) {
                    instance.addPermanentModifier(new AttributeModifier(SYMPTOM_HEALTH_DEBUFF_UUID, symptom.name,
                            -stage, AttributeModifier.Operation.ADDITION));
                }
                // reset the health data value so the max health change is applied immediately
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getHealth());
                }
            });
    // default is 4, stage 10 result will be 1.6
    public static final Symptom MINING_FATIGUE = Symptom.ofAttributeModifier(defaultKey("mining_fatigue"), 10, 0.0f, 1.0f,
            0.04f, Attributes.ATTACK_SPEED, SYMPTOM_MINING_FATIGUE_UUID);
    // default is 2, stage 10 result will be 0.5
    public static final Symptom WEAKNESS = Symptom.ofAttributeModifier(defaultKey("weakness"), 10, 0.0f, 1.0f,
            0.025f, Attributes.ATTACK_DAMAGE, SYMPTOM_WEAKNESS_UUID);
    // default is 0.1, stage 7 result will be 0.065
    // REMEMBER TO UPDATE TESTS IF YOU CHANGE THIS
    public static final Symptom SLOWNESS = Symptom.ofAttributeModifier(defaultKey("slowness"), 7, 0.0f, 1.0f,
            0.05f, Attributes.MOVEMENT_SPEED, SYMPTOM_SLOWNESS_UUID);
    // default is 300, stage 10 result will be 200
    public static final Symptom AIR_SUPPLY_DEBUFF = new Symptom(defaultKey("air_supply_debuff"), 10, 0.0f, 1.0f,
            (tracker, condition, configuredSymptom, baseSymptom, stage) -> {
                if (stage != 0) {
                    tracker.getPlayer().gtceu$setMaxAirSupply(tracker.getPlayer().gtceu$getOriginalMaxAirSupply() - 10 * stage);
                } else {
                    tracker.getPlayer().gtceu$setMaxAirSupply(-1);
                }
            });

    public static final Symptom BLINDNESS = Symptom.ofEffect(defaultKey("blindness"), 10, 0.0f, 1.0f, MobEffects.BLINDNESS);
    public static final Symptom DARKNESS = Symptom.ofEffect(defaultKey("darkness"), 10, 0.0f, 1.0f, MobEffects.DARKNESS);
    public static final Symptom NAUSEA = Symptom.ofEffect(defaultKey("nausea"), 1, 0.95f, 1.0f, MobEffects.CONFUSION);
    public static final Symptom WITHER = Symptom.ofEffect(defaultKey("wither"), 1, 1.0f, 1.0f, MobEffects.WITHER);
    public static final Symptom WEAK_POISONING = Symptom.ofEffect(defaultKey("weak_poisoning"), 10, 0.0f, 1.0f, GTMobEffects.WEAK_POISON);
    public static final Symptom POISONING = Symptom.ofEffect(defaultKey("poisoning"), 10, 0.0f, 1.0f, MobEffects.POISON);
    public static final Symptom HUNGER = Symptom.ofEffect(defaultKey("hunger"), 5, 0.0f, 1.0f, MobEffects.HUNGER);
    // spotless:on

    public final String name;
    public final int defaultStages;
    /**
     * The (relative) threshold this symptom will start occurring at.<br>
     * The range is [0.0,1.0], with 0.0 meaning "as soon as the player gains the condition"
     * and 1.0 meaning "at the condition's max progress value".
     * <p>
     * If this symptom's {@link #defaultStages} is >0, the symptom will start occurring at
     * {@link #minThreshold} and the maximum stage will be reached at {@link #maxThreshold}.
     * </p>
     * <p>
     * For example: The relative minimum threshold of this symptom is 0.5 and
     * the condition's maximum progress is 200 seconds.
     * This symptom will start occurring when the player has had the condition for 100 seconds.
     * </p>
     */
    public final float minThreshold;
    /**
     * The (relative) threshold at which this symptom will be reach its maximum potential.<br>
     * The range is [0.0,1.0], with 0.0 meaning "as soon as the player gains the condition"
     * and 1.0 meaning "at the condition's max progress value".
     * <p>
     * If this symptom's {@link #defaultStages} is >0, the symptom will start occurring at
     * {@link #minThreshold} and the maximum stage will be reached at {@link #maxThreshold}.
     * </p>
     * <p>
     * For example: tThe relative maximum threshold of this symptom is 0.75 and the
     * condition's maximum progress is 200 seconds.
     * This symptom will reach its peak when the player has had the condition for 150 seconds.
     * </p>
     */
    public final float maxThreshold;

    private final Effect progressionEffect;
    private final Effect tickEffect;

    public Symptom(String name, int defaultStages, float minThreshold, float maxThreshold,
                   Effect progressionEffect, Effect tickEffect) {
        this.name = name;
        this.defaultStages = defaultStages;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.progressionEffect = progressionEffect;
        this.tickEffect = tickEffect;
    }

    public Symptom(String name, int defaultStages, float minThreshold, float maxThreshold, Effect progressionEffect) {
        this(name, defaultStages, minThreshold, maxThreshold, progressionEffect, Effect.NO_OP);
    }

    /**
     * @param multiplier multiplier for Attribute modification
     * @param attribute  Attribute to modify
     * @param uuid       AttributeModifier UUID
     */
    public static Symptom ofAttributeModifier(String name, int defaultStages, float minThreshold, float maxThreshold,
                                              float multiplier, Attribute attribute, UUID uuid) {
        return new Symptom(name, defaultStages, minThreshold, maxThreshold,
                (tracker, condition, symptom, baseSymptom, stage) -> {
                    Player player = tracker.getPlayer();
                    AttributeInstance instance = player.getAttribute(attribute);
                    if (instance == null) {
                        return;
                    }
                    instance.removeModifier(uuid);

                    if (stage != 0) {
                        instance.addPermanentModifier(new AttributeModifier(uuid, name,
                                -stage * multiplier, AttributeModifier.Operation.MULTIPLY_BASE));
                    }
                });
    }

    /**
     * @param mobEffect           effect to apply
     * @param amplifierMultiplier amplifier added to effect every progression tick
     */
    public static Symptom ofEffect(String name, int defaultStages, float minThreshold, float maxThreshold,
                                   Supplier<MobEffect> mobEffect, int amplifierMultiplier) {
        return new Symptom(name, defaultStages, minThreshold, maxThreshold,
                (tracker, $1, $2, $3, stage) -> {
                    MobEffect effect = mobEffect.get();
                    tracker.setMobEffect(effect, amplifierMultiplier * stage);
                    if (stage == 0) {
                        tracker.getPlayer().removeEffect(effect);
                    }
                });
    }

    /**
     * @param mobEffect           MobEffect to apply
     * @param amplifierMultiplier amplifier added to MobEffect every progression
     */
    public static Symptom ofEffect(String name, int defaultStages, float minThreshold, float maxThreshold,
                                   MobEffect mobEffect, int amplifierMultiplier) {
        return ofEffect(name, defaultStages, minThreshold, maxThreshold,
                () -> mobEffect, amplifierMultiplier);
    }

    /**
     * @param mobEffect effect to apply
     */
    public static Symptom ofEffect(String name, int defaultStages, float minThreshold, float maxThreshold,
                                   Supplier<MobEffect> mobEffect) {
        return ofEffect(name, defaultStages, minThreshold, maxThreshold, mobEffect, 1);
    }

    /**
     * @param mobEffect effect to apply
     */
    public static Symptom ofEffect(String name, int defaultStages, float minThreshold, float maxThreshold,
                                   MobEffect mobEffect) {
        return ofEffect(name, defaultStages, minThreshold, maxThreshold, () -> mobEffect);
    }

    public void applyProgression(MedicalConditionTracker subject, MedicalCondition condition,
                                 ConfiguredSymptom symptom, int stage) {
        progressionEffect.apply(subject, condition, symptom, this, stage);
    }

    public void tick(MedicalConditionTracker subject, MedicalCondition condition,
                     ConfiguredSymptom symptom, int stage) {
        tickEffect.apply(subject, condition, symptom, this, stage);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static class ConfiguredSymptom {

        @Getter
        private final Symptom symptom;
        @Getter
        private final int stages;
        @Getter
        private final float relativeHarshness;
        /**
         * The threshold this symptom will start occurring at.
         * <p>
         * If this symptom's {@link #stages} is >0, the symptom will start occurring at {@link #minThreshold}
         * and the maximum stage will be reached at {@link #maxThreshold}.
         * </p>
         * <p>
         * For example: The minimum threshold of this symptom is 100 and
         * the condition's maximum progress is 200 seconds.
         * This symptom will start occurring when the player has had the condition for 100 seconds.
         * </p>
         */
        @Getter
        private float minThreshold;
        /**
         * The threshold at which this symptom will be reach its maximum potential.
         * <p>
         * If this symptom's {@link #stages} is >0, the symptom will start occurring at {@link #minThreshold}
         * and the maximum stage will be reached at {@link #maxThreshold}.
         * </p>
         * <p>
         * For example: The maximum threshold of this symptom is 150 and
         * the condition's maximum progress is 200 seconds.
         * This symptom will reach its peak when the player has had the condition for 150 seconds.
         * </p>
         */
        @Getter
        private float maxThreshold;

        /**
         * Whether this {@code ConfiguredSymptom} uses the default progression thresholds for its {@link #symptom}
         * and should recalculate absolute progression values for the {@linkplain MedicalCondition} it's a part of
         */
        private boolean relativeThresholds = false;

        protected ConfiguredSymptom(Symptom symptom, int stages,
                                    float relativeMinThreshold, float relativeMaxThreshold) {
            this.symptom = symptom;
            this.stages = stages;
            this.relativeHarshness = (float) stages / symptom.defaultStages;

            this.minThreshold = relativeMinThreshold;
            this.maxThreshold = relativeMaxThreshold;
        }

        public ConfiguredSymptom(Symptom symptom, int stages, int absMinThreshold, int absMaxThreshold) {
            this(symptom, stages, (float) absMinThreshold, (float) absMaxThreshold);
        }

        public ConfiguredSymptom(Symptom symptom, int absMinThreshold, int absMaxThreshold) {
            this(symptom, symptom.defaultStages, absMinThreshold, absMaxThreshold);
        }

        public ConfiguredSymptom(Symptom symptom, int stages) {
            this(symptom, stages, symptom.minThreshold, symptom.maxThreshold);
            this.relativeThresholds = true;
        }

        public ConfiguredSymptom(Symptom symptom) {
            this(symptom, symptom.defaultStages);
        }

        /**
         * Update the stored progression threshold values based on the passed condition's
         * {@link MedicalCondition#maxProgression maxProgression} value
         * 
         * @param condition the medical condition that the threshold values will be based on
         * @param index     the index in the condition's symptom list this symptom will be added to
         */
        public void addedToCondition(MedicalCondition condition, int index) {
            if (this.relativeThresholds) {
                this.relativeThresholds = false;
                this.minThreshold = this.minThreshold * condition.maxProgression;
                this.maxThreshold = this.maxThreshold * condition.maxProgression;
            }

            this.minThreshold = Mth.clamp(this.minThreshold, 0.0f, condition.maxProgression);
            this.maxThreshold = Mth.clamp(this.maxThreshold, 0.0f, condition.maxProgression);

            Preconditions.checkArgument(minThreshold <= maxThreshold,
                    "minProgressThreshold must be <= maxProgressThreshold for symptom %s (%s) of condition %s (min %s > max %s)",
                    index, symptom.name, condition.id.toString(), minThreshold, maxThreshold);
        }

        @Override
        public String toString() {
            StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
            stringJoiner.add("stages=" + this.stages);
            stringJoiner.add("relativeHarshness=" + this.relativeHarshness);
            stringJoiner.add("minTreshold=" + this.minThreshold);
            stringJoiner.add("maxTreshold=" + this.maxThreshold);

            return this.symptom.toString() + stringJoiner;
        }
    }

    @FunctionalInterface
    public interface Effect {

        Effect NO_OP = (tracker, condition, configuredSymptom, baseSymptom, amplifier) -> {};

        /**
         * If {@code stage} is 0, any effects should be removed.
         *
         * @param tracker           the medical condition tracker processing this effect
         * @param condition         the medical condition this symptom belongs to
         * @param configuredSymptom the symptom this effect belongs to
         * @param baseSymptom       the unconfigured symptom
         * @param stage             the stage of this symptom
         */
        void apply(MedicalConditionTracker tracker, MedicalCondition condition,
                   ConfiguredSymptom configuredSymptom, Symptom baseSymptom, int stage);
    }

    private static String defaultKey(String name) {
        return "symptom.gtceu." + name;
    }
}
