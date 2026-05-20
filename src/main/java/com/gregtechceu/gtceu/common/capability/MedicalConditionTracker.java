package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
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
    final Reference2FloatOpenHashMap<MedicalCondition> medicalConditions = new Reference2FloatOpenHashMap<>();
    private final Set<MedicalCondition> permanentConditions = new ReferenceOpenHashSet<>();
    @Getter
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

    /**
     * Progress a condition {@code condition} by {@code progression} counts.<br>
     * This is invoked with negative values on antidote/cure consumption.
     *
     * @param condition   MedicalCondition to heal
     * @param progression amount of progression to decrease
     */
    public void progressCondition(@NotNull MedicalCondition condition, float progression) {
        // if progression is negative, remove zeroed conditions as well
        if (progression < 0.0f && -progression >= medicalConditions.getFloat(condition)) {
            removeMedicalCondition(condition);
        } else {
            if (player.isCreative()) return;

            medicalConditions.addTo(condition, progression);
        }
        updateActiveSymptoms();
    }

    public void removeMedicalCondition(MedicalCondition condition) {
        flaggedForRemoval.add(condition);
        permanentConditions.remove(condition);
    }

    @VisibleForTesting
    void updateActiveSymptoms() {
        for (MedicalCondition condition : medicalConditions.keySet()) {
            if (flaggedForRemoval.contains(condition)) {
                continue;
            }
            if (medicalConditions.getFloat(condition) >= condition.maxProgression * 2) {
                // If condition has been applied for 2x the maximum time, make it permanent.
                permanentConditions.add(condition);
            }

            for (ConfiguredSymptom configured : condition.symptoms) {
                int stage = Math.max(calculateStage(condition, configured), 0);
                Symptom symptom = configured.getSymptom();
                symptom.tick(this, condition, configured, stage);

                Optional<ConfiguredSymptom> maybeExisting = activeSymptoms.keySet()
                        .stream()
                        .filter(s -> s.getSymptom() == symptom)
                        .findFirst();
                if (maybeExisting.isEmpty() || maybeExisting.get() == configured) {
                    if (stage == 0) {
                        activeSymptoms.removeInt(configured);
                    } else {
                        activeSymptoms.put(configured, stage);
                    }

                    symptom.applyProgression(this, condition, configured, stage);
                    continue;
                }
                if (stage == 0) {
                    // if stage == 0, the last check can't ever be true. In that case, just skip it.
                    continue;
                }

                ConfiguredSymptom existing = maybeExisting.get();
                int existingStage = activeSymptoms.getInt(existing);
                if (configured.getRelativeHarshness() * stage > existing.getRelativeHarshness() * existingStage) {
                    activeSymptoms.removeInt(existing);
                    activeSymptoms.put(configured, stage);

                    symptom.applyProgression(this, condition, configured, stage);
                    continue;
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

    private int calculateStage(MedicalCondition condition, ConfiguredSymptom symptom) {
        float minThreshold = symptom.getMinThreshold();
        float maxThreshold = symptom.getMaxThreshold();
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
