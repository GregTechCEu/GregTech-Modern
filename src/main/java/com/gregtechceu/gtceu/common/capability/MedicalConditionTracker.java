package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

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
    private final Object2IntMap<MedicalCondition> medicalConditions = new Object2IntOpenHashMap<>();

    private final Map<MedicalCondition,Material> medicalConditionTriggerMap = new HashMap<>();

    private final Set<Symptom> activeSymptoms = new HashSet<>();

    private final Hashtable<MobEffect,Integer> activeMobEffects = new Hashtable<>();

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
            player.addEffect(new MobEffectInstance(mobEffect,1,activeMobEffects.get(mobEffect)));
        }
    }

    @Override
    public void progressRelatedCondition(@NotNull Material material){
        //TODO apply new medicalConditions here

        //TODO swap out symptoms with worse variants here


    }

    /**
     * called on antidote/cure consumption
     * @param condition MedicalCondition to heal
     * @param progression amount of progression to decrease
     */
    public void heal(MedicalCondition condition, int progression){
        if(progression>= medicalConditions.getOrDefault(condition,-1)){
            medicalConditions.removeInt(condition);
            medicalConditionTriggerMap.remove(condition);
            return;
        }
        medicalConditions.replace(condition, medicalConditions.getOrDefault(condition,1));
    }

    @Override
    public int getMaxAirSupply() {
        if(maxAirSupply==-1)
            return 300; //300 is default
        return maxAirSupply;
    }

    public void setMobEffect(MobEffect effect, int amplifier){
        if(amplifier<=0){
            activeMobEffects.remove(effect);
        } else if(amplifier>=activeMobEffects.getOrDefault(effect,-1)) {
            activeMobEffects.put(effect,amplifier);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag effectsTag = new ListTag();
        for (var medicalConditionEntry : medicalConditions.object2IntEntrySet()) {
            if (medicalConditionEntry.getKey() == null) {
                continue;
            }
            CompoundTag medicalConditionTag = new CompoundTag();
            medicalConditionTag.putString("material", medicalConditionTriggerMap.get(medicalConditionEntry.getKey()).getResourceLocation().toString());
            medicalConditionTag.putInt("progression", medicalConditionEntry.getIntValue());
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
