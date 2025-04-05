package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.api.data.damagesource.DamageTypeData;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.AirScrubberRecipes;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@Accessors(chain = true)
public class MedicalCondition {

    public static final Map<String, MedicalCondition> CONDITIONS = new HashMap<>();
    public static final Codec<MedicalCondition> CODEC = Codec.STRING.xmap(MedicalCondition.CONDITIONS::get,
            MedicalCondition::getName);

    @Getter
    public final String name;
    public final int color;
    public final float maxProgression; // amount of seconds until maximum progression is reached
    public final Set<Symptom.ConfiguredSymptom> symptoms = new HashSet<>();
    private final DamageTypeData damageTypeData;
    public final IdleProgressionType idleProgressionType;
    public final float idleProgressionRate;
    public final boolean canBePermanent;
    /**
     * This should mirror the {@link AirScrubberRecipes} recipe's outputs for this condition.
     */
    @Getter
    @Setter
    @NotNull
    public Consumer<GTRecipeBuilder> recipeModifier = builder -> {};

    public MedicalCondition(String name, int color, int maxProgression, IdleProgressionType idleProgressionType,
                            float idleProgressionRate, boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        this.name = name;
        this.color = color;
        this.maxProgression = maxProgression;
        this.damageTypeData = new DamageTypeData.Builder()
                .simpleId("medical_condition/" + name)
                .scaling(DamageScaling.NEVER)
                .tag(DamageTypeTags.BYPASSES_ARMOR)
                .build();

        this.symptoms.addAll(Arrays.asList(symptoms));
        this.idleProgressionType = idleProgressionType;
        this.idleProgressionRate = idleProgressionRate;
        this.canBePermanent = canBePermanent;

        CONDITIONS.put(name, this);
    }

    public MedicalCondition(String name, int color, int maxProgression, IdleProgressionType progressionType,
                            boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        this(name, color, maxProgression, progressionType, 1, canBePermanent, symptoms);
    }

    public MedicalCondition(String name, int color, int maxProgression, Symptom.ConfiguredSymptom... symptoms) {
        this(name, color, maxProgression, IdleProgressionType.NONE, 0, false, symptoms);
    }

    public DamageSource getDamageSource(MedicalConditionTracker tracker) {
        return damageTypeData.source(tracker.getPlayer().level());
    }

    public DamageSource getDamageSource(Level level) {
        return damageTypeData.source(level);
    }

    public enum IdleProgressionType {
        UNTREATED_PROGRESSION,
        HEAL,
        NONE
    }
}
