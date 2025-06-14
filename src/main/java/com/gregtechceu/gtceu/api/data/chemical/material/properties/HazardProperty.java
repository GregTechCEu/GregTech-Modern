package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.GTBucketItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.*;

public class HazardProperty implements IMaterialProperty {

    public final MedicalCondition condition;
    public final HazardTrigger hazardTrigger;
    public final boolean applyToDerivatives;
    public final float progressionMultiplier;

    public HazardProperty(HazardTrigger hazardTrigger, MedicalCondition condition, float progressionMultiplier,
                          boolean applyToDerivatives) {
        this.hazardTrigger = hazardTrigger;
        this.condition = condition;
        this.applyToDerivatives = applyToDerivatives;
        this.progressionMultiplier = progressionMultiplier;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    public record HazardTrigger(String name, ProtectionType protectionType, Set<TagPrefix> affectedTagPrefixes)
            implements StringRepresentable {

        public static final Map<String, HazardTrigger> ALL_TRIGGERS = new HashMap<>();

        public static final HazardTrigger INHALATION = new HazardTrigger("inhalation", ProtectionType.MASK,
                TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny, TagPrefix.dustPure, TagPrefix.dustImpure);
        public static final HazardTrigger ANY = new HazardTrigger("any", ProtectionType.FULL);
        public static final HazardTrigger SKIN_CONTACT = new HazardTrigger("skin_contact", ProtectionType.HANDS,
                TagPrefix.dust, TagPrefix.dustSmall, TagPrefix.dustTiny);
        public static final HazardTrigger NONE = new HazardTrigger("none", ProtectionType.NONE);

        public HazardTrigger {
            ALL_TRIGGERS.put(name, this);
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

        MASK(Set.of("head"), ArmorItem.Type.HELMET),
        HANDS(Set.of("hands"), ArmorItem.Type.CHESTPLATE),
        FULL(Set.of(), ArmorItem.Type.BOOTS, ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS),
        NONE(Set.of());

        @Getter
        private final Set<ArmorItem.Type> equipmentTypes;
        @Getter
        private final Set<String> curioSlots;

        /**
         * Equipment validity is treated in an OR fashion.
         * that is, EITHER all curio slots are valid, OR all equipment slots are valid.
         * 
         * @param curioSlots     curio slot names to test for
         * @param equipmentTypes armor slots to test for
         */
        ProtectionType(Set<String> curioSlots, ArmorItem.Type... equipmentTypes) {
            this.curioSlots = curioSlots;
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
            if (!GTCEu.Mods.isCuriosLoaded() || this.curioSlots.isEmpty()) {
                return correctArmorItems.containsAll(equipmentTypes);
            }
            Set<String> correctCurios = new HashSet<>();
            ICuriosItemHandler curiosInventory = CuriosApi.getCuriosInventory(livingEntity)
                    .resolve()
                    .orElse(null);
            if (curiosInventory == null) {
                return correctArmorItems.containsAll(equipmentTypes);
            }
            List<SlotResult> results = curiosInventory.findCurios(this.curioSlots.toArray(String[]::new));
            for (SlotResult result : results) {
                ItemStack armor = result.stack();
                if (!armor.isEmpty() && ((armor.getItem() instanceof ArmorComponentItem armorItem &&
                        armorItem.getArmorLogic().isPPE()) ||
                        armor.getTags().anyMatch(tag -> tag.equals(CustomTags.PPE_ARMOR)))) {
                    correctCurios.add(result.slotContext().identifier());
                }
            }
            return correctArmorItems.containsAll(equipmentTypes) || correctCurios.containsAll(curioSlots);
        }

        public void damageEquipment(Player player, int amount) {
            // entity has proper safety equipment, so damage it per material every 5 seconds.
            if (player.level().getGameTime() % 100 == 0) {
                for (ArmorItem.Type type : this.getEquipmentTypes()) {
                    ItemStack armor = player.getItemBySlot(type.getSlot());
                    if (!armor.isEmpty() && ((armor.getItem() instanceof ArmorComponentItem armorItem &&
                            armorItem.getArmorLogic().isPPE()) ||
                            armor.getTags().anyMatch(tag -> tag.equals(CustomTags.PPE_ARMOR)))) {
                        armor.hurtAndBreak(amount, player, p -> p.broadcastBreakEvent(type.getSlot()));
                    }
                }
                if (GTCEu.Mods.isCuriosLoaded()) {
                    ICuriosItemHandler curiosInventory = CuriosApi.getCuriosInventory(player)
                            .resolve()
                            .orElse(null);
                    if (curiosInventory != null) {
                        for (String curioItem : this.getCurioSlots()) {
                            curiosInventory.getStacksHandler(curioItem).ifPresent(handler -> {
                                IDynamicStackHandler stackHandler = handler.getStacks();
                                for (int i = 0; i < handler.getSlots(); ++i) {
                                    ItemStack armor = stackHandler.getStackInSlot(i);
                                    if (!armor.isEmpty() && ((armor.getItem() instanceof ArmorComponentItem armorItem &&
                                            armorItem.getArmorLogic().isPPE()) ||
                                            armor.getTags().anyMatch(tag -> tag.equals(CustomTags.PPE_ARMOR)))) {
                                        armor.hurtAndBreak(amount, player, p -> {});
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    public static Material getValidHazardMaterial(ItemStack item) {
        Material material = GTMaterials.NULL;
        TagPrefix prefix = TagPrefix.NULL_PREFIX;
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
            MaterialEntry entry = ChemicalHelper.getMaterialEntry(item.getItem());
            if (!entry.isEmpty()) {
                material = entry.material();
                prefix = entry.tagPrefix();
            }
        }
        HazardProperty property = material.getProperty(PropertyKey.HAZARD);
        if (property == null) {
            return GTMaterials.NULL;
        }
        if (!isFluid && !property.hazardTrigger.isAffected(prefix)) {
            return GTMaterials.NULL;
        }
        return material;
    }
}
