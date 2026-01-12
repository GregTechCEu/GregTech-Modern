package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition.IdleProgressionType;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom.ConfiguredSymptom;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

public class MedicalConditionTracker implements ICapabilitySerializable<CompoundTag> {

    @Getter
    @VisibleForTesting
    protected final Reference2FloatOpenHashMap<MedicalCondition> medicalConditions = new Reference2FloatOpenHashMap<>();
    private final Set<MedicalCondition> permanentConditions = new ReferenceOpenHashSet<>();
    private final Object2IntMap<ConfiguredSymptom> activeSymptoms = new Object2IntOpenHashMap<>();
    private final Reference2IntMap<MobEffect> activeMobEffects = new Reference2IntOpenHashMap<>();

    private final Set<MedicalCondition> flaggedForRemoval = new ReferenceOpenHashSet<>();

    @Getter
    private final Player player;

    private final LazyOptional<MedicalConditionTracker> holder = LazyOptional.of(() -> this);

    public MedicalConditionTracker(Player player) {
        this.player = player;
    }

    public void tick() {
        if (player.isCreative()) return;

        for (var entry : activeMobEffects.reference2IntEntrySet()) {
            player.addEffect(new MobEffectInstance(entry.getKey(), 100, entry.getIntValue()));
        }

        for (MedicalCondition condition : medicalConditions.keySet()) {
            if (condition.idleProgressionType == IdleProgressionType.NONE ||
                    condition.idleProgressionRate == 0.0f) {
                continue;
            }
            if (permanentConditions.contains(condition) &&
                    condition.idleProgressionType == IdleProgressionType.HEAL) {
                // can't automatically heal permanent conditions.
                continue;
            }
            int multiplier = (condition.idleProgressionType == IdleProgressionType.HEAL) ? -1 : 1;
            medicalConditions.addTo(condition, condition.idleProgressionRate * multiplier);
            evaluateMedicalCondition(condition);
        }
        if (!medicalConditions.isEmpty()) {
            updateActiveSymptoms();
        }
    }

    public void progressRelatedCondition(@NotNull MaterialEntry materialEntry, int count) {
        HazardProperty materialHazard = materialEntry.material().getProperty(PropertyKey.HAZARD);
        float strength = (float) (materialEntry.getMaterialAmount() / GTValues.M) * count *
                materialHazard.progressionMultiplier;
        progressCondition(materialHazard.condition, strength);
    }

    public void progressCondition(@NotNull MedicalCondition condition, float strength) {
        if (player.isCreative()) return;

        medicalConditions.addTo(condition, strength);
        updateActiveSymptoms();
    }

    private void updateActiveSymptoms() {
        for (MedicalCondition condition : medicalConditions.keySet()) {
            if (medicalConditions.getFloat(condition) >= condition.maxProgression * 2) {
                // If condition has been applied for 2x the maximum time, make it permanent.
                permanentConditions.add(condition);
            }

            for (ConfiguredSymptom symptom : condition.symptoms) {
                int lastStage = activeSymptoms.getInt(symptom);
                int stage = calculateStage(condition, symptom);
                if (stage <= 0) {
                    continue;
                }
                Symptom baseSymptom = symptom.getSymptom();
                baseSymptom.tick(this, condition, symptom, stage);

                Optional<ConfiguredSymptom> maybeExistingSymptom = activeSymptoms.keySet()
                        .stream()
                        .filter(s -> s.getSymptom() == baseSymptom)
                        .findFirst();
                if (maybeExistingSymptom.isEmpty()) {
                    activeSymptoms.put(symptom, stage);
                    baseSymptom.applyProgression(this, condition, symptom, stage);
                    continue;
                }
                ConfiguredSymptom existingSymptom = maybeExistingSymptom.get();
                int existingStage = activeSymptoms.getInt(existingSymptom);
                if (existingSymptom == symptom && stage > lastStage) {
                    activeSymptoms.put(symptom, stage);
                    baseSymptom.applyProgression(this, condition, symptom, stage);
                    continue;
                }
                if (symptom.getRelativeHarshness() * stage > existingSymptom.getRelativeHarshness() * existingStage) {
                    activeSymptoms.removeInt(existingSymptom);
                    activeSymptoms.put(symptom, stage);
                    baseSymptom.applyProgression(this, condition, symptom, stage);
                }
            }
        }

        if (flaggedForRemoval.isEmpty()) {
            return;
        }
        for (MedicalCondition condition : flaggedForRemoval) {
            Set<ConfiguredSymptom> toRemove = new HashSet<>();
            activeSymptoms.keySet().stream()
                    .filter(condition.symptoms::contains)
                    .forEach(symptom -> {
                        // reset all symptom effects for this condition
                        symptom.getSymptom().applyProgression(this, condition, symptom, 0);
                        toRemove.add(symptom);
                    });
            for (ConfiguredSymptom symptom : toRemove) {
                activeSymptoms.removeInt(symptom);
            }

            medicalConditions.removeFloat(condition);
        }
        flaggedForRemoval.clear();
    }

    public void removeMedicalCondition(MedicalCondition condition) {
        flaggedForRemoval.add(condition);
        permanentConditions.remove(condition);
    }

    private int calculateStage(MedicalCondition condition, ConfiguredSymptom symptom) {
        float minThreshold = symptom.getMinProgressionThreshold();
        float maxThreshold = symptom.getMaxProgressionThreshold();
        float progression = medicalConditions.getFloat(condition);

        if (progression < minThreshold) {
            return 0;
        }
        if (progression >= maxThreshold) {
            return symptom.getStages();
        }
        float delta = Mth.inverseLerp(Math.min(progression, condition.maxProgression), minThreshold, maxThreshold);
        return (int) (delta * symptom.getStages());
    }

    // removes MedicalConditions without progression
    private void evaluateMedicalCondition(MedicalCondition condition) {
        if (permanentConditions.contains(condition)) {
            return;
        }
        if (medicalConditions.getFloat(condition) <= 0) {
            removeMedicalCondition(condition);
        }
    }

    /**
     * called on antidote/cure consumption
     *
     * @param condition   MedicalCondition to heal
     * @param progression amount of progression to decrease
     */
    public void heal(MedicalCondition condition, int progression) {
        if (progression >= medicalConditions.getFloat(condition)) {
            medicalConditions.removeFloat(condition);
            permanentConditions.remove(condition);
            return;
        }
        medicalConditions.addTo(condition, -progression);
    }

    public void setMobEffect(MobEffect effect, int amplifier) {
        if (amplifier <= 0) {
            activeMobEffects.removeInt(effect);
            return;
        }
        activeMobEffects.mergeInt(effect, amplifier, Math::max);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag effectsTag = new ListTag();
        for (var entry : medicalConditions.reference2FloatEntrySet()) {
            CompoundTag medicalConditionTag = new CompoundTag();
            medicalConditionTag.putString("condition", entry.getKey().id.toString());
            medicalConditionTag.putFloat("progression", entry.getFloatValue());
            effectsTag.add(medicalConditionTag);
        }
        tag.put("medical_conditions", effectsTag);

        ListTag permanentsTag = new ListTag();
        for (MedicalCondition condition : permanentConditions) {
            permanentsTag.add(StringTag.valueOf(condition.id.toString()));
        }
        tag.put("permanent_conditions", permanentsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        // ensure the medical condition map(s) is actually empty before loading.
        // IDK if this actually happens, but better be safe than sorry.
        medicalConditions.clear();
        permanentConditions.clear();

        ListTag medicalConditionsTag = arg.getList("medical_conditions", Tag.TAG_COMPOUND);
        for (int i = 0; i < medicalConditionsTag.size(); ++i) {
            CompoundTag compoundTag = medicalConditionsTag.getCompound(i);
            ResourceLocation id = GTCEu.id(compoundTag.getString("condition"));
            if (!GTRegistries.MEDICAL_CONDITIONS.containKey(id)) {
                continue;
            }
            MedicalCondition condition = GTRegistries.MEDICAL_CONDITIONS.get(id);
            float progression = compoundTag.getFloat("progression");

            medicalConditions.put(condition, progression);
        }

        ListTag permanentConditionsTag = arg.getList("permanent_conditions", Tag.TAG_STRING);
        for (int i = 0; i < permanentConditionsTag.size(); ++i) {
            ResourceLocation id = GTCEu.id(permanentConditionsTag.getString(i));
            if (!GTRegistries.MEDICAL_CONDITIONS.containKey(id)) {
                continue;
            }
            permanentConditions.add(GTRegistries.MEDICAL_CONDITIONS.get(id));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return GTCapability.CAPABILITY_MEDICAL_CONDITION_TRACKER.orEmpty(cap, this.holder);
    }
}
