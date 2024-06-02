package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.Set;

public class HazardEffectTracker implements IHazardEffectTracker {

    @Getter
    @Setter
    private int maxAirSupply = -1;
    @Getter
    private final Set<Material> extraHazards = new HashSet<>();
    @Getter
    private final Object2IntMap<Material> currentHazards = new Object2IntOpenHashMap<>();

    private final Player player;

    private int totalMaxAirSupply, maxAirSupplySetterAmount;

    public HazardEffectTracker(Player player) {
        this.player = player;
    }

    @Override
    public void startTick() {
        for (Material material : this.getExtraHazards()) {
            tick(material);
        }
    }

    @Override
    public void tick(@NotNull Material material) {
        HazardProperty property = material.getProperty(PropertyKey.HAZARD);
        if (property == null) {
            return;
        }
        int time = currentHazards.getOrDefault(material, 0);

        if (property.getHazardType().protectionType().isProtected(player)) {
            // entity has proper safety equipment, so damage it per material every 5 seconds.
            if (player.level().getGameTime() % 100 == 0) {
                for (ArmorItem.Type type : property.getHazardType().protectionType().getEquipmentTypes()) {
                    player.getItemBySlot(type.getSlot()).hurtAndBreak(1, player, type.getSlot());
                }
            }
            // exit the hazard applying after.
            return;
        }

        int totalTime = 0;
        int effectsCount = 0;
        for (HazardProperty.HazardEffect effect : property.getEffects()) {
            totalTime += effect.duration() + effect.modifierStartTime();
            effectsCount++;
            if (time < effect.modifierStartTime()) {
                // if the current applied time is less than the minimum for effects to be applied, return early.
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
        }

        currentHazards.put(material, time += 1);
        // if the hazardous material has been held for 5x the average of the effects' start times, make it permanent.
        // this also makes it tick 1 more time per tick, thus speeding up the effects.
        if (time >= 5 * (totalTime / effectsCount)) {
            extraHazards.add(material);
        }
    }

    @Override
    public void endTick() {
        if (maxAirSupplySetterAmount > 0) {
            this.setMaxAirSupply(totalMaxAirSupply / maxAirSupplySetterAmount);
        }
        totalMaxAirSupply = 0;
        maxAirSupplySetterAmount = 0;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag effectsTag = new ListTag();
        for (var effect : currentHazards.object2IntEntrySet()) {
            if (effect.getKey() == null) {
                continue;
            }
            CompoundTag effectTag = new CompoundTag();
            effectTag.putString("material", effect.getKey().getResourceLocation().toString());
            effectTag.putInt("time", effect.getIntValue());
            effectsTag.add(effectTag);
        }
        tag.put("effects", effectsTag);

        ListTag extrasTag = new ListTag();
        for (Material material : extraHazards) {
            extrasTag.add(StringTag.valueOf(material.getResourceLocation().toString()));
        }
        tag.put("extras", extrasTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag arg) {
        ListTag effects = arg.getList("effects", Tag.TAG_COMPOUND);
        for (Tag tag : effects) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                continue;
            }
            Material material = GTCEuAPI.materialManager.getMaterial(compoundTag.getString("material"));
            int time = compoundTag.getInt("time");
            currentHazards.put(material, time);
        }
        ListTag extras = arg.getList("extras", Tag.TAG_STRING);
        for (Tag tag : extras) {
            if (!(tag instanceof StringTag stringTag)) {
                continue;
            }
            extraHazards.add(GTCEuAPI.materialManager.getMaterial(stringTag.getAsString()));
        }
    }
}
