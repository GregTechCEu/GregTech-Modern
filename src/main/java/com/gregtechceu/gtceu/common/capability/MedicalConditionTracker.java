package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MedicalConditionTracker implements IMedicalConditionTracker, INBTSerializable<CompoundTag> {


    @Getter
    private final Map<MedicalCondition,Float> medicalConditions = new HashMap<>();

    private final Map<MedicalCondition,Material> medicalConditionTriggerMap = new HashMap<>();

    private final Object2IntMap<Symptom.ConfiguredSymptom> activeSymptoms = new Object2IntOpenHashMap<>();

    private final Object2IntMap<MobEffect> activeMobEffects = new Object2IntOpenHashMap<>();

    @Setter
    private int maxAirSupply = -1; // -1 for default (300).


    @Getter
    private final Player player;

    public MedicalConditionTracker(Player player) {
        this.player = player;
    }



    @Override
    public void tick() {
        for (MobEffect mobEffect : activeMobEffects.keySet()) {
            player.addEffect(new MobEffectInstance(mobEffect,1,activeMobEffects.getOrDefault(mobEffect,0)));
        }

        if(player.level().getGameTime()%20==0){ //apply idle progression every second
           for (MedicalCondition condition: medicalConditions.keySet()){
               if(condition.idleProgressionType == MedicalCondition.IdleProgressionType.NONE){
                   continue;
               }
               int multiplier = (condition.idleProgressionType == MedicalCondition.IdleProgressionType.HEAL)?1:-1;
               medicalConditions.replace(condition,medicalConditions.get(condition)+condition.idleProgressionRate*multiplier);
           }
            if(!medicalConditions.isEmpty()) {
                updateActiveSymptoms();

            }

        }
    }

    @Override
    public void progressRelatedCondition(@NotNull Material material){
        HazardProperty materialHazard = material.getProperty(PropertyKey.HAZARD);
        if(!medicalConditions.containsKey(materialHazard.condition)){
            medicalConditions.put(materialHazard.condition,materialHazard.progressionMultiplier);
        }
        if(medicalConditions.containsKey(materialHazard.condition)){
            medicalConditions.replace(materialHazard.condition,medicalConditions.get(materialHazard.condition)+materialHazard.progressionMultiplier);
        }

        updateActiveSymptoms();


    }

    private void updateActiveSymptoms(){
        for(MedicalCondition condition: medicalConditions.keySet()){
            for(Symptom.ConfiguredSymptom symptom: condition.symptoms.values()){
                int stage = calculateStage(condition,symptom);
                Optional<Symptom.ConfiguredSymptom> currentSymptom = activeSymptoms.keySet().stream().filter(symptom1 -> symptom1.symptom==symptom.symptom).findFirst();
                if(currentSymptom.isEmpty()){
                    activeSymptoms.put(symptom, stage);
                    symptom.symptom.applyProgression(this,condition.getDamageSource(this),-stage);
                    continue;
                }
                if(currentSymptom.get()==symptom&&stage>activeSymptoms.getOrDefault(symptom,-1)){
                    symptom.symptom.applyProgression(this,condition.getDamageSource(this),activeSymptoms.getOrDefault(symptom,0));
                    activeSymptoms.replace(symptom,stage);
                    symptom.symptom.applyProgression(this,condition.getDamageSource(this),-stage);
                    continue;
                }
                if(symptom.relativeHarshness*stage>currentSymptom.get().relativeHarshness*activeSymptoms.getOrDefault(currentSymptom,0)){
                    currentSymptom.get().symptom.applyProgression(this,condition.getDamageSource(this),activeSymptoms.getOrDefault( currentSymptom.get(),0));
                    activeSymptoms.removeInt(currentSymptom);
                    activeSymptoms.put(symptom,stage);
                    symptom.symptom.applyProgression(this,condition.getDamageSource(this),-stage);

                }

            }
            System.err.println(activeSymptoms.values().toArray()[0]);



        }
    }

    private int calculateStage(MedicalCondition condition, Symptom.ConfiguredSymptom symptom){
        return (int) (condition.maxProgression/(condition.maxProgression-symptom.progressionThreshold*condition.maxProgression)*symptom.stages);
    }

    /**
     * called on antidote/cure consumption
     * @param condition MedicalCondition to heal
     * @param progression amount of progression to decrease
     */
    @Override
    public void heal(MedicalCondition condition, int progression){
        if(progression>= medicalConditions.getOrDefault(condition,-1f)){
            medicalConditions.remove(condition);
            medicalConditionTriggerMap.remove(condition);
            return;
        }
        medicalConditions.replace(condition, medicalConditions.getOrDefault(condition,1f));
    }

    @Override
    public int getMaxAirSupply() {
        if(maxAirSupply==-1)
            return 300; //300 is default
        return maxAirSupply;
    }

    @Override
    public void setMobEffect(MobEffect effect, int amplifier){
        if(amplifier<=0){
            activeMobEffects.removeInt(effect);
        } else if(amplifier>=activeMobEffects.getOrDefault(effect,-1)) {
            activeMobEffects.put(effect,amplifier);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag effectsTag = new ListTag();
        for (var medicalCondition : medicalConditions.keySet()) {
            if (medicalCondition == null) {
                continue;
            }
            CompoundTag medicalConditionTag = new CompoundTag();
            medicalConditionTag.putString("material", medicalConditionTriggerMap.get(medicalCondition).getResourceLocation().toString());
            medicalConditionTag.putFloat("progression", medicalConditions.get(medicalCondition));
            effectsTag.add(medicalConditionTag);
        }
        tag.put("medicalconditions", effectsTag);


        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        ListTag medicalConditionTagList = arg.getList("medicalconditions", Tag.TAG_COMPOUND);
        for (Tag tag : medicalConditionTagList) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                continue;
            }
            Material material = GTCEuAPI.materialManager.getMaterial(compoundTag.getString("material"));
            int progression = compoundTag.getInt("progression");
            //TODO associate material with specific condition
        }
    }
}
