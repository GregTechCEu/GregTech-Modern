package com.gregtechceu.gtceu.api.material.material.properties;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMobEffects;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author h3tR
 * @date 2024/2/12
 * @implNote HazardProperty
 */
public class HazardProperty implements IMaterialProperty<HazardProperty> {

    public static final UUID HAZARD_MAX_HEALTH_UUID = UUID.fromString("607aa6d9-a7e4-4919-9962-f007104c4be8");
    public static final String HAZARD_MAX_HEALTH_KEY = "gtceu.hazard.max_health";

    @Getter
    @Nullable
    private final HazardProperty.HazardDamage damage;
    @Getter
    private final List<HazardProperty.HazardEffect> effects = new ArrayList<>();
    @Getter
    private final HazardType hazardType;
    @Getter
    private final boolean applyToDerivatives;

    public HazardProperty(HazardType hazardType, @Nullable HazardProperty.HazardEffect effect,
                          @Nullable HazardProperty.HazardDamage damage, boolean applyToDerivatives) {
        this.hazardType = hazardType;
        this.effects.add(effect);
        this.damage = damage;
        this.applyToDerivatives = applyToDerivatives;
    }

    public HazardProperty(HazardType hazardType, List<HazardProperty.HazardEffect> effects,
                          @Nullable HazardProperty.HazardDamage damage, boolean applyToDerivatives) {
        this.hazardType = hazardType;
        this.effects.addAll(effects);
        this.damage = damage;
        this.applyToDerivatives = applyToDerivatives;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    public enum HazardType {

        INHALATION_POISON(ProtectionType.MASK, TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny,
                TagPrefix.dustPure, TagPrefix.dustImpure),
        CONTACT_POISON(ProtectionType.FULL),
        RADIOACTIVE(ProtectionType.FULL),
        CORROSIVE(ProtectionType.HANDS, TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny),
        NONE(ProtectionType.FULL);

        public static final HazardType[] ALL = { INHALATION_POISON, CONTACT_POISON, RADIOACTIVE, CORROSIVE };

        private final Set<TagPrefix> affectedTagPrefixes = new HashSet<>();
        @Getter
        private final ProtectionType protectionType;

        HazardType(ProtectionType protectionType, TagPrefix... tagPrefixes) {
            this.protectionType = protectionType;
            affectedTagPrefixes.addAll(Arrays.asList(tagPrefixes));
            if (tagPrefixes.length > 0)
                affectedTagPrefixes.add(null); // add a null for fluid, because they don't have a prefix but still need
                                               // to always be harmful.
        }

        public boolean isAffected(TagPrefix prefix) {
            if (affectedTagPrefixes.isEmpty()) return true; // empty list means all prefixes are affected
            return affectedTagPrefixes.contains(prefix);
        }
    }

    public enum ProtectionType {

        MASK(ArmorItem.Type.HELMET),
        HANDS(ArmorItem.Type.CHESTPLATE),
        FULL(ArmorItem.Type.BOOTS, ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS);

        @Getter
        private final Set<ArmorItem.Type> equipmentTypes;

        ProtectionType(ArmorItem.Type... equipmentTypes) {
            this.equipmentTypes = Set.of(equipmentTypes);
        }

        public boolean isProtected(LivingEntity livingEntity) {
            Set<ArmorItem.Type> correctArmorItems = new HashSet<>();
            for (ArmorItem.Type equipmentType : equipmentTypes) {
                ItemStack armor = livingEntity.getItemBySlot(equipmentType.getSlot());
                if (!armor.isEmpty() && ((armor.getItem() instanceof ArmorComponentItem armorItem &&
                        armorItem.getArmorLogic().isPPE()) ||
                        armor.getTags().anyMatch(tag -> tag.equals(CustomTags.PPE_ARMOR)))) {
                    correctArmorItems.add(equipmentType);
                }
            }
            return correctArmorItems.containsAll(equipmentTypes);
        }
    }

    public static HazardProperty.HazardEffect maxHealthLoweringEffect(int secondsToMax, int startTime, int modifier) {
        return new HazardProperty.HazardEffect(secondsToMax, startTime,
                Map.of(Attributes.MAX_HEALTH, new AttributeModifier(HazardProperty.HAZARD_MAX_HEALTH_UUID,
                        HazardProperty.HAZARD_MAX_HEALTH_KEY, -modifier, AttributeModifier.Operation.ADD_VALUE)));
    }

    public static HazardProperty.HazardEffect maxAirLoweringEffect(int secondsToMax, int startTime,
                                                                   int newMaxAirSupply) {
        return new HazardProperty.HazardEffect(secondsToMax, startTime, newMaxAirSupply);
    }

    public static HazardProperty.HazardEffect witherEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.WITHER, 1, amplifier));
    }

    public static HazardProperty.HazardEffect slownessEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, amplifier));
    }

    public static HazardProperty.HazardEffect miningFautigueEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1, amplifier));
    }

    public static HazardProperty.HazardEffect poisonEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(GTMobEffects.WEAK_POISON.get(), 1, amplifier));
    }

    public static HazardProperty.HazardEffect weaknessEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.WEAKNESS, 1, amplifier));
    }

    public static HazardProperty.HazardEffect blindnessEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.BLINDNESS, 1, amplifier));
    }

    /**
     * @param damage amount of damage applied every {@code delay} seconds.
     * @param delay  damage is applied every {@code delay} seconds
     */
    public record HazardDamage(int damage, int delay) {}

    /**
     * A group of effects applied by a hazard.
     * 
     * @param duration          if this is a potion effect, the time (in ticks) the effect has. else it's the ramp-up
     *                          time (in ticks) for the attribute modifiers.
     * @param modifierStartTime the time (in seconds) before the modifier is applied at all.
     * @param effects           the mob effects, if any.
     * @param modifiers         the attribute modifiers, if any.
     */
    public record HazardEffect(int duration, int modifierStartTime, List<Supplier<MobEffectInstance>> effects,
                               Map<Attribute, AttributeModifier> modifiers, int newMaxAirSupply) {

        @SafeVarargs
        public HazardEffect(int duration, Supplier<MobEffectInstance>... effects) {
            this(duration, 0, Arrays.stream(effects).toList(), Object2ObjectMaps.emptyMap(), -1);
        }

        @SafeVarargs
        public HazardEffect(int duration, int modifierStartTime, Supplier<MobEffectInstance>... effects) {
            this(duration, modifierStartTime, Arrays.stream(effects).toList(), Object2ObjectMaps.emptyMap(), -1);
        }

        public HazardEffect(int secondsToMax, Map<Attribute, AttributeModifier> modifiers) {
            this(secondsToMax, 0, List.of(), modifiers, -1);
        }

        public HazardEffect(int secondsToMax, int modifierStartTime, Map<Attribute, AttributeModifier> modifiers) {
            this(secondsToMax, modifierStartTime, List.of(), modifiers, -1);
        }

        public HazardEffect(int secondsToMax, Map<Attribute, AttributeModifier> modifiers, int maxAirModifier) {
            this(secondsToMax, 0, List.of(), modifiers, maxAirModifier);
        }

        public HazardEffect(int secondsToMax, int maxAirModifier) {
            this(secondsToMax, 0, List.of(), Object2ObjectMaps.emptyMap(), maxAirModifier);
        }

        public HazardEffect(int secondsToMax, int modifierStartTime, int maxAirModifier) {
            this(secondsToMax, modifierStartTime, List.of(), Object2ObjectMaps.emptyMap(), maxAirModifier);
        }

        public List<MobEffectInstance> getEffectInstancesAtTime(int timeFromStart) {
            if (this.effects.isEmpty()) {
                return List.of();
            }
            List<MobEffectInstance> effectInstances = new ArrayList<>();
            for (Supplier<MobEffectInstance> effectSupplier : effects) {
                MobEffectInstance effect = effectSupplier.get();
                // the effects get stronger the longer you hold the item.
                int effectDuration = duration == -1 ? -1 : effect.getDuration() * duration * timeFromStart / 500;
                int effectAmplifier = effect.getAmplifier() * timeFromStart / 1000;
                effectInstances.add(new MobEffectInstance(effect.getEffect(), effectDuration, effectAmplifier));
            }
            return effectInstances;
        }

        public Map<Attribute, AttributeModifier> getModifiersAtTime(int timeFromStart) {
            if (this.modifiers.isEmpty()) {
                return Object2ObjectMaps.emptyMap();
            }
            Map<Attribute, AttributeModifier> modifierMap = new HashMap<>();
            for (var entry : this.modifiers.entrySet()) {
                AttributeModifier modifier = entry.getValue();
                double amount = modifier.amount() * (double) timeFromStart / Math.max(duration, 1);
                modifierMap.put(entry.getKey(),
                        new AttributeModifier(modifier.id(), modifier.name(), amount, modifier.operation()));
            }
            return modifierMap;
        }

        public int getNewMaxAirSupplyAtTime(int timeFromStart) {
            if (newMaxAirSupply == -1) {
                return -1;
            }
            return newMaxAirSupply / Math.max(Math.round((float) timeFromStart / Math.max(duration, 1)), 1);
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("duration", duration);
            tag.putInt("modifier_start_time", modifierStartTime);

            ListTag effectsTag = new ListTag();
            for (Supplier<MobEffectInstance> effect : effects) {
                effectsTag.add(effect.get().save());
            }
            tag.put("effects", effectsTag);
            CompoundTag attributesTag = new CompoundTag();
            for (Map.Entry<Attribute, AttributeModifier> modifier : modifiers.entrySet()) {
                attributesTag.put(BuiltInRegistries.ATTRIBUTE.getKey(modifier.getKey()).toString(),
                        modifier.getValue().save());
            }
            tag.put("modifiers", attributesTag);
            tag.putInt("max_air_supply", newMaxAirSupply);

            return tag;
        }

        public static HazardEffect deserializeNBT(CompoundTag tag) {
            int duration = tag.getInt("duration");
            int modifierStartTime = tag.getInt("modifier_start_time");

            List<Supplier<MobEffectInstance>> effects = new ArrayList<>();
            for (Tag effect : tag.getList("effects", Tag.TAG_COMPOUND)) {
                if (!(effect instanceof CompoundTag compoundTag)) {
                    continue;
                }
                effects.add(() -> MobEffectInstance.load(compoundTag));
            }
            Map<Attribute, AttributeModifier> modifiers = new HashMap<>();
            CompoundTag attributesTag = tag.getCompound("modifiers");
            for (String key : attributesTag.getAllKeys()) {
                modifiers.put(BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(key)),
                        AttributeModifier.load(attributesTag.getCompound(key)));
            }
            int maxAirModifier = tag.getInt("max_air_supply");

            return new HazardEffect(duration, modifierStartTime, effects, modifiers, maxAirModifier);
        }
    }
}
