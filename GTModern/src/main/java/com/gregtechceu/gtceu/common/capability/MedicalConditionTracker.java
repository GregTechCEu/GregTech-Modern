package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MedicalConditionTracker implements IMedicalConditionTracker, INBTSerializable<CompoundTag> {

    @Getter
    private final Object2FloatMap<MedicalCondition> medicalConditions = new Object2FloatOpenHashMap<>();
    private final Set<MedicalCondition> permanentConditions = new HashSet<>();
    private final Object2IntMap<Symptom.ConfiguredSymptom> activeSymptoms = new Object2IntOpenHashMap<>();
    private final Object2IntMap<MobEffect> activeMobEffects = new Object2IntOpenHashMap<>();

    private final Set<MedicalCondition> flaggedForRemoval = new HashSet<>();

    @Setter
    private int maxAirSupply = -1;

    @Getter
    private final Player player;

    public MedicalConditionTracker(Player player) {
        this.player = player;
    }

    @Override
    public void tick() {
        if (player.isCreative()) return;

        for (var entry : activeMobEffects.object2IntEntrySet()) {
            player.addEffect(new MobEffectInstance(entry.getKey(), 100, entry.getIntValue()));
        }

        if (player.level().getGameTime() % 20 == 0) { // apply idle progression every second
            for (MedicalCondition condition : medicalConditions.keySet()) {
                if (condition.idleProgressionType == MedicalCondition.IdleProgressionType.NONE) {
                    continue;
                }
                if (permanentConditions.contains(condition) &&
                        condition.idleProgressionType == MedicalCondition.IdleProgressionType.HEAL) {
                    // can't automatically heal permanent conditions.
                    continue;
                }
                int multiplier = (condition.idleProgressionType == MedicalCondition.IdleProgressionType.HEAL) ? -1 : 1;
                medicalConditions.replace(condition,
                        medicalConditions.getFloat(condition) + condition.idleProgressionRate * multiplier);
                evaluateMedicalCondition(condition);
            }
            if (!medicalConditions.isEmpty()) {
                updateActiveSymptoms();
            }
        }
    }

    public void progressCondition(@NotNull MedicalCondition condition, float strength) {
        if (player.isCreative()) return;

        medicalConditions.put(condition, medicalConditions.getOrDefault(condition, 0) + strength);

        updateActiveSymptoms();
    }

    private void updateActiveSymptoms() {
        for (MedicalCondition condition : medicalConditions.keySet()) {
            if (medicalConditions.getFloat(condition) >= condition.maxProgression * 2) {
                // If condition has been applied for 2x the maximum time, make it permanent.
                permanentConditions.add(condition);
            }

            for (Symptom.ConfiguredSymptom symptom : condition.symptoms) {
                int stage = calculateStage(condition, symptom);
                if (stage <= 0) {
                    continue;
                }
                symptom.symptom.tick(this, condition, symptom, stage);

                Optional<Symptom.ConfiguredSymptom> currentSymptomOptional = activeSymptoms.keySet()
                        .stream()
                        .filter(symptom1 -> symptom1.symptom == symptom.symptom)
                        .findFirst();
                if (currentSymptomOptional.isEmpty()) {
                    activeSymptoms.put(symptom, stage);
                    symptom.symptom.applyProgression(this, condition, null, stage);
                    continue;
                }
                Symptom.ConfiguredSymptom currentSymptom = currentSymptomOptional.get();
                if (currentSymptom == symptom && stage > activeSymptoms.getOrDefault(symptom, 0)) {
                    symptom.symptom.applyProgression(this, condition, symptom,
                            activeSymptoms.getOrDefault(symptom, 0));
                    activeSymptoms.replace(symptom, stage);
                    symptom.symptom.applyProgression(this, condition, symptom, stage);
                    continue;
                }
                if (symptom.relativeHarshness * stage >
                        currentSymptom.relativeHarshness * activeSymptoms.getOrDefault(currentSymptom, 0)) {
                    currentSymptom.symptom.applyProgression(this, condition, symptom,
                            activeSymptoms.getOrDefault(currentSymptom, 0));
                    activeSymptoms.removeInt(currentSymptom);
                    activeSymptoms.put(symptom, stage);
                    symptom.symptom.applyProgression(this, condition, symptom, stage);
                }
            }
        }

        if (flaggedForRemoval.isEmpty()) {
            return;
        }
        for (MedicalCondition condition : flaggedForRemoval) {
            for (Symptom.ConfiguredSymptom configuredSymptom : activeSymptoms.keySet().stream()
                    .filter(condition.symptoms::contains).toList()) {
                // reset all symptom effects for this condition
                configuredSymptom.symptom.applyProgression(this, condition, configuredSymptom, 0);
            }
            medicalConditions.removeFloat(condition);
        }
        flaggedForRemoval.clear();
    }

    @Override
    public void removeMedicalCondition(MedicalCondition condition) {
        flaggedForRemoval.add(condition);
        permanentConditions.remove(condition);
    }

    private int calculateStage(MedicalCondition condition, Symptom.ConfiguredSymptom symptom) {
        return (int) Math.floor(Math.min(medicalConditions.getFloat(condition), condition.maxProgression) /
                (symptom.progressionThreshold * condition.maxProgression * symptom.stages));
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
    @Override
    public void heal(MedicalCondition condition, int progression) {
        if (progression >= medicalConditions.getOrDefault(condition, 0)) {
            medicalConditions.removeFloat(condition);
            permanentConditions.remove(condition);
            return;
        }
        medicalConditions.replace(condition, medicalConditions.getOrDefault(condition, 0) - progression);
    }

    @Override
    public int getMaxAirSupply() {
        return maxAirSupply;
    }

    @Override
    public void setMobEffect(MobEffect effect, int amplifier) {
        if (amplifier <= 0) {
            activeMobEffects.removeInt(effect);
        } else if (amplifier >= activeMobEffects.getOrDefault(effect, -1)) {
            activeMobEffects.put(effect, amplifier);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag effectsTag = new ListTag();
        for (var entry : medicalConditions.object2FloatEntrySet()) {
            CompoundTag medicalConditionTag = new CompoundTag();
            medicalConditionTag.putString("condition", entry.getKey().name);
            medicalConditionTag.putFloat("progression", entry.getFloatValue());
            effectsTag.add(medicalConditionTag);
        }
        tag.put("medical_conditions", effectsTag);

        ListTag permanentsTag = new ListTag();
        for (MedicalCondition condition : permanentConditions) {
            permanentsTag.add(StringTag.valueOf(condition.name));
        }
        tag.put("permanent_conditions", permanentsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        ListTag medicalConditionsTag = arg.getList("medical_conditions", Tag.TAG_COMPOUND);
        for (int i = 0; i < medicalConditionsTag.size(); ++i) {
            CompoundTag compoundTag = medicalConditionsTag.getCompound(i);
            MedicalCondition condition = MedicalCondition.CONDITIONS.get(compoundTag.getString("condition"));
            float progression = compoundTag.getFloat("progression");

            medicalConditions.put(condition, progression);
        }

        ListTag permanentConditionsTag = arg.getList("permanent_conditions", Tag.TAG_STRING);
        for (int i = 0; i < permanentConditionsTag.size(); ++i) {
            permanentConditions.add(MedicalCondition.CONDITIONS.get(permanentConditionsTag.getString(i)));
        }
    }
}
