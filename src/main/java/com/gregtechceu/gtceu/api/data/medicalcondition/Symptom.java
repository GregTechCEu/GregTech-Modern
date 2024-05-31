package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.UUID;

public class Symptom {

    public static final UUID SYMPTOM_HEALTH_DEBUFF_UUID = UUID.fromString("607aa6d9-a7e4-4919-9962-f007104c4be8");
    public static final UUID SYMPTOM_ATTACK_SPEED_DEBUFF_UUID = UUID.fromString("f2378ee6-3427-45b5-8440-4b797f7b664a");
    public static final UUID SYMPTOM_WEAKNESS_UUID = UUID.fromString("482e64e0-de77-49cd-b9bc-96b7e7eb16db");
    public static final UUID SYMPTOM_SLOWNESS_UUID = UUID.fromString("b3ac6b40-2d30-419f-9cac-5b2cf998ad72");

    public static final Symptom DEATH = new Symptom(defaultKey("death"),1,0,
            ((hazardEffectTracker, damageSource, modifier) -> hazardEffectTracker.getPlayer().die(damageSource)));
    public static final Symptom HEALTH_DEBUFF = new Symptom(defaultKey("health_debuff"),10,0, 1,Attributes.MAX_HEALTH, SYMPTOM_HEALTH_DEBUFF_UUID);
    public static final Symptom ATTACK_SPEED_DEBUFF = new Symptom(defaultKey("attack_speed_debuff"),10,0,.2f,Attributes.ATTACK_SPEED, SYMPTOM_ATTACK_SPEED_DEBUFF_UUID);
    public static final Symptom WEAKNESS = new Symptom(defaultKey("weakness"),10,0,.1f,Attributes.ATTACK_DAMAGE, SYMPTOM_WEAKNESS_UUID);
    public static final Symptom SLOWNESS = new Symptom(defaultKey("slowness"),7,0,.05f,Attributes.MOVEMENT_SPEED, SYMPTOM_SLOWNESS_UUID);
    public static final Symptom AIR_SUPPLY_DEBUFF = new Symptom(defaultKey("air_supply_debuff"),10,0,
            (hazardEffectTracker, damageSource, modifier) -> hazardEffectTracker.setMaxAirSupply(hazardEffectTracker.getMaxAirSupply()+10*modifier));
    public static final Symptom BLINDNESS = new Symptom(defaultKey("blindness"),10,0, MobEffects.BLINDNESS);
    public static final Symptom MINING_FATIGUE = new Symptom(defaultKey("mining_fatigue"),10,0, MobEffects.DIG_SLOWDOWN);



    public final String name;
    public final int defaultStages;
    public final float defaultProgressionThreshold;
    private final TriConsumer<MedicalConditionTracker, DamageSource, Integer> progressionEffect;

    public Symptom(String name, int defaultStages, float defaultProgressionThreshold, TriConsumer<MedicalConditionTracker, DamageSource, Integer> progressionEffect) {
        this.name = name;
        this.defaultStages = defaultStages;
        this.defaultProgressionThreshold = defaultProgressionThreshold;
        this.progressionEffect = progressionEffect;

    }

    /**
     * @param multiplier multiplier for Attribute modification
     * @param attribute Attribute to modify
     * @param uuid AttributeModifier UUID
     */
    public Symptom(String name, int defaultStages, float defaultProgressionThreshold, float multiplier, Attribute attribute, UUID uuid) {
        this(name, defaultStages, defaultProgressionThreshold, ((hazardEffectTracker, damageSource, modifier) ->
                hazardEffectTracker.getPlayer().getAttribute(attribute).addPermanentModifier(new AttributeModifier(uuid, name, modifier*multiplier, AttributeModifier.Operation.ADDITION))));
    }

    /**
     * @param mobEffect MobEffect to apply
     * @param amplifiermultiplier amplifier added to MobEffect every progression
     */
    public Symptom(String name, int defaultStages, float defaultProgressionThreshold, MobEffect mobEffect, int amplifiermultiplier) {
        this(name, defaultStages, defaultProgressionThreshold, ((hazardEffectTracker, damageSource, modifier) ->
                hazardEffectTracker.setMobEffect(mobEffect,amplifiermultiplier*modifier)));
    }


    /**
     * @param mobEffect MobEffect to apply
     */
    public Symptom(String name, int defaultStages, float defaultProgressionThreshold, MobEffect mobEffect) {
        this(name, defaultStages, defaultProgressionThreshold, ((hazardEffectTracker, damageSource, modifier) ->
                hazardEffectTracker.setMobEffect(mobEffect,modifier)));
    }


    public void applyProgression(MedicalConditionTracker subject, @Nullable DamageSource source, int modifier){
        progressionEffect.accept(subject, source, modifier);
    }

    public static class ConfiguredSymptom{
        public final Symptom symptom;
        public final int stages;
        public final float progressionThreshold;
        public final float relativeHarshness;

        ConfiguredSymptom(Symptom symptom, int stages, float progressionThreshold){
            this.symptom = symptom;
            this.stages = stages;
            this.progressionThreshold = progressionThreshold;
            this.relativeHarshness = (float)stages/symptom.defaultStages;
        }

        ConfiguredSymptom(Symptom symptom){
            this(symptom,symptom.defaultStages,symptom.defaultProgressionThreshold);
        }

        ConfiguredSymptom(Symptom symptom, int stages){
            this(symptom,stages,symptom.defaultProgressionThreshold);
        }

        ConfiguredSymptom(Symptom symptom, float progressionThreshold){
            this(symptom,symptom.defaultStages,progressionThreshold);
        }
    }


    private static String defaultKey(String name){
        return "gtceu.symptom."+name;
    }
}
