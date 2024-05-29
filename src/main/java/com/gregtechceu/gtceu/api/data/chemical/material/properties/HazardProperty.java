package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.forge.GTBucketItem;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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

    public static final UUID HAZARD_MAX_HEALTH_UUID = UUID.fromString("607aa6d9-a7e4-4919-9962-f007104c4be8");
    public static final String HAZARD_MAX_HEALTH_KEY = "gtceu.hazard.max_health";

    @Getter
    @Nullable
    private final HazardProperty.HazardDamage damage;
    @Getter
    private final List<HazardProperty.HazardEffect> effects = new ArrayList<>();
    @Getter
    private final HazardTrigger hazardType;
    @Getter
    private final boolean applyToDerivatives;

    public HazardProperty(HazardTrigger hazardType, @Nullable HazardProperty.HazardEffect effect,
                          @Nullable HazardProperty.HazardDamage damage, boolean applyToDerivatives) {
        this.hazardType = hazardType;
        this.effects.add(effect);
        this.damage = damage;
        this.applyToDerivatives = applyToDerivatives;
    }

    public HazardProperty(HazardTrigger hazardType, List<HazardProperty.HazardEffect> effects,
                          @Nullable HazardProperty.HazardDamage damage, boolean applyToDerivatives) {
        this.hazardType = hazardType;
        this.effects.addAll(effects);
        this.damage = damage;
        this.applyToDerivatives = applyToDerivatives;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    public record HazardTrigger(String name, ProtectionType protectionType, Set<TagPrefix> affectedTagPrefixes)
            implements StringRepresentable {

        public static final Map<String, HazardTrigger> ALL_HAZARDS = new HashMap<>();

        public static final HazardTrigger INHALATION = new HazardTrigger("inhalation", ProtectionType.MASK,
                TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny, TagPrefix.dustPure, TagPrefix.dustImpure);
        public static final HazardTrigger ANY = new HazardTrigger("any", ProtectionType.FULL);
        public static final HazardTrigger HANDS = new HazardTrigger("hands", ProtectionType.HANDS,
                TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny);
        public static final HazardTrigger NONE = new HazardTrigger("none", ProtectionType.NONE);

        public HazardTrigger {
            ALL_HAZARDS.put(name, this);
        }

        public HazardTrigger(String name, ProtectionType protectionType, TagPrefix... tagPrefixes) {
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
                material = ChemicalHelper.getMaterial(bucket.getFluid());
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


    public record HazardEffect(int duration, int modifierStartTime,
                               Map<Attribute, AttributeModifier> modifiers) {

        public HazardEffect(int duration) {
            this(duration, 0, Object2ObjectMaps.emptyMap());
        }





    }
}
