package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

public class HazardEffectTracker implements IHazardEffectTracker, INBTSerializable<ListTag> {
    @Getter @Setter
    private int maxAirSupply = -1;

    @Getter
    private final Object2IntMap<HazardProperty> propertyToAmount = new Object2IntOpenHashMap<>();
    @Getter
    private final Object2IntMap<HazardProperty.HazardEffect> currentHazardEffects = new Object2IntOpenHashMap<>();

    private final Player player;

    public HazardEffectTracker(Player player) {
        this.player = player;
    }

    @Override
    public void tick() {
        int totalMaxAirSupply = 0;
        int maxAirSupplySetterAmount = 0;

        for (var entry : currentHazardEffects.object2IntEntrySet()) {
            HazardProperty.HazardEffect effect = entry.getKey();
            int time = entry.getIntValue();

            if (time < effect.modifierStartTime()) {
                // if the current applied time is less than the minimum for effects to be applied, return early.
                entry.setValue(time + 1);
                continue;
            }

            for (MobEffectInstance mobEffect : effect.getEffectInstancesAtTime(time)) {
                player.addEffect(mobEffect);
            }
            var attributeModifiers = effect.getModifiersAtTime(time);
            for (var modifierEntry : attributeModifiers.entrySet()) {
                AttributeModifier modifier = modifierEntry.getValue();
                AttributeInstance attributeInstance = player.getAttribute(modifierEntry.getKey());
                if (attributeInstance == null) {
                    continue;
                }

                if (attributeInstance.hasModifier(modifier)) {
                    attributeInstance.removeModifier(modifier);
                }
                attributeInstance.addPermanentModifier(modifier);
            }
            int maxAirSupply = effect.getNewMaxAirSupplyAtTime(time);
            if (maxAirSupply >= 0) {
                totalMaxAirSupply += maxAirSupply;
                maxAirSupplySetterAmount++;
            }

            entry.setValue(time + 1);
        }

        if (maxAirSupplySetterAmount > 0) {
            this.setMaxAirSupply(totalMaxAirSupply / maxAirSupplySetterAmount);
        }
    }

    @Override
    public ListTag serializeNBT() {
        ListTag tag = new ListTag();
        for (var effect : currentHazardEffects.object2IntEntrySet()) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.put("effect", effect.getKey().serializeNBT());
            effectTag.putInt("time", effect.getIntValue());
            tag.add(effectTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(ListTag arg) {
        for (Tag tag : arg) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                continue;
            }
            HazardProperty.HazardEffect effect = HazardProperty.HazardEffect.deserializeNBT(compoundTag.getCompound("effect"));
            int time = compoundTag.getInt("time");
            currentHazardEffects.put(effect, time);
        }
    }

    /*
    @Override
    public Map<Attribute, AttributeModifier> getCurrentModifiers() {
        Map<Attribute, AttributeModifier> modifiers = new HashMap<>();
        for (var effect : currentHazardEffects.object2IntEntrySet()) {
            modifiers.putAll(effect.getKey().modifiers());
        }
        return modifiers;
    }

    @Override
    public List<MobEffectInstance> getCurrentPotionEffects() {
        List<MobEffectInstance> modifiers = new ArrayList<>();
        for (var effect : currentHazardEffects.object2IntEntrySet()) {
            modifiers.addAll(effect.getKey().getEffectInstances());
        }
        return modifiers;
    }
    */

    @Override
    public void removeHazardItem(HazardProperty property) {
        if (!propertyToAmount.containsKey(property)) {
            return;
        }
        propertyToAmount.put(property, propertyToAmount.getOrDefault(property, 0) - 1);
        if (propertyToAmount.getInt(property) <= 0) {
            propertyToAmount.removeInt(property);
            //currentHazardEffects.removeInt(property.getEffect());
        }
    }

    @Override
    public void addHazardItem(HazardProperty property) {
        if (player.isCreative()) {
            return;
        }
        propertyToAmount.put(property, propertyToAmount.getOrDefault(property, 0) + 1);
        for (HazardProperty.HazardEffect effect : property.getEffects()) {
            if (!this.currentHazardEffects.containsKey(effect)) {
                this.currentHazardEffects.put(effect, 0);
            }
        }
    }
}
