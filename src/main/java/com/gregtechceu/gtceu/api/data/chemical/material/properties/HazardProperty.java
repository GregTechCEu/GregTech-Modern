package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author h3tR
 * @date 2024/2/12
 * @implNote HazardProperty
 */
public class HazardProperty implements IMaterialProperty<HazardProperty> {

    @Getter
    @Nullable
    private final HazardProperty.HazardDamage damage;

    @Getter
    @Nullable
    private final HazardProperty.HazardEffect effect;

    @Getter
    private final HazardType hazardType;

    @Getter
    private final boolean applyToDerivatives;

    public HazardProperty(HazardType hazardType, @Nullable HazardProperty.HazardEffect effect,
                          @Nullable HazardProperty.HazardDamage damage, boolean applyToDerivatives) {
        this.damage = damage;
        this.hazardType = hazardType;
        this.applyToDerivatives = applyToDerivatives;
        this.effect = effect;
    }

    /**
     * Default property constructor.
     */
    public HazardProperty() {
        this(HazardType.CONTACT_POISON, null, null, false);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    public enum HazardType {

        INHALATION_POISON(ProtectionType.MASK, TagPrefix.dust, TagPrefix.dustImpure, TagPrefix.dustSmall,
                TagPrefix.dustPure, TagPrefix.dustTiny, TagPrefix.rawOre, TagPrefix.rawOreBlock, TagPrefix.crushed,
                TagPrefix.crushedRefined, TagPrefix.crushedPurified),
        CONTACT_POISON(ProtectionType.FULL),
        RADIOACTIVE(ProtectionType.FULL),
        CORROSIVE(ProtectionType.HANDS, TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny),
        NONE(ProtectionType.FULL);

        private final List<TagPrefix> affectedTagPrefixes = new ArrayList<>();

        @Getter
        private final ProtectionType protectionType;

        HazardType(ProtectionType protectionType, TagPrefix... tagPrefixes) {
            this.protectionType = protectionType;
            affectedTagPrefixes.addAll(Arrays.asList(tagPrefixes));
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

        private final List<ArmorItem.Type> equipmentTypes;

        ProtectionType(ArmorItem.Type... equipmentTypes) {
            this.equipmentTypes = List.of(equipmentTypes);
        }

        public boolean isProtected(LivingEntity livingEntity) {
            List<ArmorItem.Type> correctArmorItems = new ArrayList<>();
            for (ArmorItem.Type equipmentType : equipmentTypes) {
                ItemStack armor = livingEntity.getItemBySlot(equipmentType.getSlot());
                if (!armor.isEmpty() && ((armor.getItem() instanceof ArmorComponentItem armorItem &&
                        armorItem.getArmorLogic().isPPE()) ||
                        armor.getTags().anyMatch(tag -> tag.equals(CustomTags.PPE_ARMOR)))) {
                    correctArmorItems.add(equipmentType);
                }
            }
            return new HashSet<>(correctArmorItems).containsAll(equipmentTypes);
        }
    }

    /**
     * @param delay damage is applied every X seconds
     */
    public record HazardDamage(int damage, int delay) {}

    public record HazardEffect(int duration, MobEffect... effects) {

        public void apply(LivingEntity entity) {
            for (MobEffect effect : effects)
                entity.addEffect(new MobEffectInstance(effect, duration));
        }
    }
}
