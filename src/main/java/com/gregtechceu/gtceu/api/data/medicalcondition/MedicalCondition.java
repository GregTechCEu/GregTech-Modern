package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MedicalCondition {
    //General Conditions
    public static final MedicalCondition CHEMICAL_BURNS = new MedicalCondition("chemical_burns",200, IdleProgressionType.HEAL,1,
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS));
    public static final MedicalCondition POISON = new MedicalCondition("poison",600, IdleProgressionType.HEAL,.25f,
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF),
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS));

    //Compound specific Conditions
    public static final MedicalCondition ASBESTOSIS = new MedicalCondition("asbestosis",5000, IdleProgressionType.UNTREATED_PROGRESSION,1,
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF,.3f),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF,.1f));
    public static final MedicalCondition SILICOSIS = new MedicalCondition("silicosis",15000, IdleProgressionType.UNTREATED_PROGRESSION,.5f,
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF,4,.75f),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF,.6f));

    public final String name;
    public final float maxProgression; //amount of seconds until maximum progression is reached
    public final HashMap<Symptom,Symptom.ConfiguredSymptom> symptoms = new HashMap<>();
    private final ResourceKey<DamageType> damageTypeKey; //TODO register these fuckers
    public final IdleProgressionType idleProgressionType;
    public final float idleProgressionRate;

    public MedicalCondition(String name, int maxProgression, IdleProgressionType idleProgressionType, float idleProgressionRate, Symptom.ConfiguredSymptom... symptoms) {
        this.name = name;
        this.maxProgression = maxProgression;
        this.damageTypeKey = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(GTCEu.MOD_ID,name));
        for(Symptom.ConfiguredSymptom symptom: symptoms){
            this.symptoms.put(symptom.symptom,symptom);
        }
        this.idleProgressionType = idleProgressionType;
        this.idleProgressionRate = idleProgressionRate;
    }

    public MedicalCondition(String name, int maxProgression, IdleProgressionType progressionType, Symptom.ConfiguredSymptom... symptoms){
        this(name,maxProgression,progressionType,1,symptoms);
    }

    public MedicalCondition(String name, int maxProgression, Symptom.ConfiguredSymptom... symptoms){
        this(name,maxProgression, IdleProgressionType.NONE,0,symptoms);
    }

    public DamageSource getDamageSource(MedicalConditionTracker tracker){
       return new DamageSource(tracker.getPlayer().level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageTypeKey));
    }


    public enum IdleProgressionType {
        UNTREATED_PROGRESSION,
        HEAL,
        NONE
    }
}
