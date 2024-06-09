package com.gregtechceu.gtceu.api.material.material.properties;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.forge.GTBucketItem;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.effect.GTMobEffects;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
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

    public static final ResourceLocation HAZARD_MAX_HEALTH_ID = GTCEu.id("hazard_max_health_modifier");

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

    public record HazardType(String name, ProtectionType protectionType, Set<TagPrefix> affectedTagPrefixes)
            implements StringRepresentable {

        public static final Map<String, HazardType> ALL_HAZARDS = new HashMap<>();

        public static final HazardType INHALATION_POISON = new HazardType("inhalation_poison", ProtectionType.MASK,
                TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny, TagPrefix.dustPure, TagPrefix.dustImpure);
        public static final HazardType CONTACT_POISON = new HazardType("contact_poison", ProtectionType.FULL);
        public static final HazardType RADIOACTIVE = new HazardType("radioactive", ProtectionType.FULL);
        public static final HazardType CORROSIVE = new HazardType("corrosive", ProtectionType.HANDS,
                TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny);
        public static final HazardType NONE = new HazardType("none", ProtectionType.NONE);

        public HazardType {
            ALL_HAZARDS.put(name, this);
        }

        public HazardType(String name, ProtectionType protectionType, TagPrefix... tagPrefixes) {
            this(name, protectionType, new HashSet<>());
            affectedTagPrefixes.addAll(Arrays.asList(tagPrefixes));
        }

        public boolean isAffected(TagPrefix prefix) {
            if (affectedTagPrefixes.isEmpty()) return true; // empty list means all prefixes are affected
            return affectedTagPrefixes.contains(prefix);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum ProtectionType {

        MASK(ArmorItem.Type.HELMET),
        HANDS(ArmorItem.Type.CHESTPLATE),
        FULL(ArmorItem.Type.BOOTS, ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS),
        NONE();

        @Getter
        private final Set<ArmorItem.Type> equipmentTypes;

        ProtectionType(ArmorItem.Type... equipmentTypes) {
            this.equipmentTypes = Set.of(equipmentTypes);
        }

        public boolean isProtected(LivingEntity livingEntity) {
            if (this == NONE) {
                return true;
            }
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
                Map.of(Attributes.MAX_HEALTH, new AttributeModifier(HazardProperty.HAZARD_MAX_HEALTH_ID,
                        -modifier, AttributeModifier.Operation.ADD_VALUE)));
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
                () -> new MobEffectInstance(GTMobEffects.WEAK_POISON, 1, amplifier));
    }

    public static HazardProperty.HazardEffect weaknessEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.WEAKNESS, 1, amplifier));
    }

    public static HazardProperty.HazardEffect blindnessEffect(int duration, int startTime, int amplifier) {
        return new HazardProperty.HazardEffect(duration, startTime,
                () -> new MobEffectInstance(MobEffects.BLINDNESS, 1, amplifier));
    }

    @Nullable
    public static Material getValidHazardMaterial(ItemStack item) {
        Material material = null;
        TagPrefix prefix = null;
        boolean isFluid = false;
        if (item.getItem() instanceof TagPrefixItem prefixItem) {
            material = prefixItem.material;
            prefix = prefixItem.tagPrefix;
        } else if (item.getItem() instanceof BucketItem bucket) {
            if (ConfigHolder.INSTANCE.gameplay.universalHazards || bucket instanceof GTBucketItem) {
                material = ChemicalHelper.getMaterial(bucket.content);
                isFluid = true;
            }
        } else if (ConfigHolder.INSTANCE.gameplay.universalHazards) {
            UnificationEntry entry = ChemicalHelper.getUnificationEntry(item.getItem());
            if (entry != null && entry.material != null) {
                material = entry.material;
                prefix = entry.tagPrefix;
            }
        }
        if (material == null) {
            return null;
        }
        HazardProperty property = material.getProperty(PropertyKey.HAZARD);
        if (property == null) {
            return null;
        }
        if (!isFluid && !property.getHazardType().isAffected(prefix)) {
            return null;
        }
        return material;
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
                               Map<Holder<Attribute>, AttributeModifier> modifiers, int newMaxAirSupply) {

        @SafeVarargs
        public HazardEffect(int duration, Supplier<MobEffectInstance>... effects) {
            this(duration, 0, Arrays.stream(effects).toList(), Object2ObjectMaps.emptyMap(), -1);
        }

        @SafeVarargs
        public HazardEffect(int duration, int modifierStartTime, Supplier<MobEffectInstance>... effects) {
            this(duration, modifierStartTime, Arrays.stream(effects).toList(), Object2ObjectMaps.emptyMap(), -1);
        }

        public HazardEffect(int secondsToMax, Map<Holder<Attribute>, AttributeModifier> modifiers) {
            this(secondsToMax, 0, List.of(), modifiers, -1);
        }

        public HazardEffect(int secondsToMax, int modifierStartTime,
                            Map<Holder<Attribute>, AttributeModifier> modifiers) {
            this(secondsToMax, modifierStartTime, List.of(), modifiers, -1);
        }

        public HazardEffect(int secondsToMax, Map<Holder<Attribute>, AttributeModifier> modifiers, int maxAirModifier) {
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

        public Map<Holder<Attribute>, AttributeModifier> getModifiersAtTime(int timeFromStart) {
            if (this.modifiers.isEmpty()) {
                return Object2ObjectMaps.emptyMap();
            }
            Map<Holder<Attribute>, AttributeModifier> modifierMap = new HashMap<>();
            for (var entry : this.modifiers.entrySet()) {
                AttributeModifier modifier = entry.getValue();
                double amount = modifier.amount() * (double) timeFromStart / Math.max(duration, 1);
                modifierMap.put(entry.getKey(),
                        new AttributeModifier(modifier.id(), amount, modifier.operation()));
            }
            return modifierMap;
        }

        public int getNewMaxAirSupplyAtTime(int timeFromStart) {
            if (newMaxAirSupply == -1) {
                return -1;
            }
            return newMaxAirSupply / Math.max(Math.round((float) timeFromStart / Math.max(duration, 1)), 1);
        }
    }
}
