package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HazardEffectTracker implements IHazardEffectTracker, INBTSerializable<CompoundTag> {

    @Getter
    @Setter
    private int maxAirSupply = -1;

    @Getter
    private final Object2IntMap<UnificationEntry> entryToAmount = new Object2IntOpenHashMap<>();
    @Getter
    private final Map<HazardProperty.HazardType, Set<HazardProperty.HazardEffect>> typesToEffects = new EnumMap<>(
            HazardProperty.HazardType.class);
    @Getter
    private final Object2IntMap<HazardProperty.HazardEffect> currentHazardEffects = new Object2IntOpenHashMap<>();

    private final Player player;

    public HazardEffectTracker(Player player) {
        this.player = player;
    }

    @Override
    public void tick() {
        Set<HazardProperty.HazardEffect> protectedFrom = new HashSet<>();
        Object2BooleanMap<HazardProperty.HazardEffect> isAffected = new Object2BooleanOpenHashMap<>();
        for (UnificationEntry entry : entryToAmount.keySet()) {
            HazardProperty property = entry.material.getProperty(PropertyKey.HAZARD);
            if (property.getHazardType().getProtectionType().isProtected(player)) {
                // entity has proper safety equipment, so damage it per material every 5 seconds.
                if (player.level().getGameTime() % 100 == 0) {
                    for (ArmorItem.Type type : property.getHazardType().getProtectionType().getEquipmentTypes()) {
                        player.getItemBySlot(type.getSlot()).hurtAndBreak(1, player, type.getSlot());
                    }
                }
                protectedFrom.addAll(property.getEffects());
            }
            for (var effect : property.getEffects()) {
                isAffected.put(effect, property.getHazardType().isAffected(entry.tagPrefix));
            }
        }
        if (protectedFrom.containsAll(currentHazardEffects.keySet())) {
            return;
        }

        int totalMaxAirSupply = 0;
        int maxAirSupplySetterAmount = 0;

        for (var entry : currentHazardEffects.object2IntEntrySet()) {
            HazardProperty.HazardEffect effect = entry.getKey();
            int time = entry.getIntValue();

            if (protectedFrom.contains(effect) || !isAffected.getBoolean(effect)) {
                continue;
            }

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
                AttributeInstance attributeInstance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(modifierEntry.getKey()));
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
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag amountsTag = new ListTag();
        for (var amount : entryToAmount.object2IntEntrySet()) {
            CompoundTag amountTag = new CompoundTag();
            if (amount.getKey().tagPrefix != null) {
                amountTag.putString("prefix", amount.getKey().tagPrefix.name);
            }
            amountTag.putString("material", amount.getKey().material.toString());
            amountTag.putInt("amount", amount.getIntValue());
            amountsTag.add(amountTag);
        }
        tag.put("amounts", amountsTag);

        ListTag effectsTag = new ListTag();
        for (var effect : currentHazardEffects.object2IntEntrySet()) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.put("effect", effect.getKey().serializeNBT());
            HazardProperty.HazardType type = getType(effect.getKey());
            if (type != null) {
                effectTag.putByte("type", (byte) type.ordinal());
            }
            effectTag.putInt("time", effect.getIntValue());
            effectsTag.add(effectTag);
        }
        tag.put("effects", effectsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag arg) {
        ListTag amounts = arg.getList("amounts", Tag.TAG_COMPOUND);
        for (Tag tag : amounts) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                continue;
            }
            TagPrefix prefix = null;
            if (compoundTag.contains("prefix", Tag.TAG_STRING)) {
                prefix = TagPrefix.get(compoundTag.getString("prefix"));
            }
            Material material = GTCEuAPI.materialManager.getMaterial(compoundTag.getString("material"));
            int amount = compoundTag.getInt("amount");
            entryToAmount.put(new UnificationEntry(prefix, material), amount);
        }

        ListTag effects = arg.getList("effects", Tag.TAG_COMPOUND);
        for (Tag tag : effects) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                continue;
            }
            HazardProperty.HazardEffect effect = HazardProperty.HazardEffect
                    .deserializeNBT(compoundTag.getCompound("effect"));
            int time = compoundTag.getInt("time");
            currentHazardEffects.put(effect, time);
            if (compoundTag.contains("type", Tag.TAG_BYTE)) {
                HazardProperty.HazardType type = HazardProperty.HazardType.values()[compoundTag.getByte("type")];
                if (!typesToEffects.containsKey(type)) {
                    typesToEffects.put(type, new HashSet<>());
                }
                typesToEffects.get(type).add(effect);
            }
        }
    }

    @Override
    public void removeHazardItem(UnificationEntry entry) {
        if (!entryToAmount.containsKey(entry)) {
            return;
        }
        entryToAmount.put(entry, entryToAmount.getOrDefault(entry, 0) - 1);
        if (entryToAmount.getInt(entry) <= 0) {
            entryToAmount.removeInt(entry);
        }
    }

    @Override
    public void addHazardItem(UnificationEntry entry) {
        if (player.isCreative()) {
            return;
        }
        entryToAmount.put(entry, entryToAmount.getOrDefault(entry, 0) + 1);
        // noinspection DataFlowIssue property existence is checked before this method is called.
        for (HazardProperty.HazardEffect effect : entry.material.getProperty(PropertyKey.HAZARD).getEffects()) {
            if (!this.currentHazardEffects.containsKey(effect)) {
                this.currentHazardEffects.put(effect, 0);
            }
        }
    }

    @Nullable
    public HazardProperty.HazardType getType(HazardProperty.HazardEffect value) {
        for (var entry : typesToEffects.entrySet()) {
            if (entry.getValue().contains(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
