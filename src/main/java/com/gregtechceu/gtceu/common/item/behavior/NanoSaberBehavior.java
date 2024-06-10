package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.IEnchantableItem;
import com.gregtechceu.gtceu.api.item.component.IItemAttributes;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class NanoSaberBehavior extends ToggleEnergyConsumerBehavior implements IItemAttributes, IEnchantableItem {

    public static final ResourceLocation OVERRIDE_KEY_LOCATION = GTCEu.id("nano_saber_active");

    private final double baseAttackDamage;
    private final double additionalAttackDamage;

    public NanoSaberBehavior() {
        super(ConfigHolder.INSTANCE.tools.nanoSaber.energyConsumption);
        this.baseAttackDamage = ConfigHolder.INSTANCE.tools.nanoSaber.nanoSaberBaseDamage;
        this.additionalAttackDamage = ConfigHolder.INSTANCE.tools.nanoSaber.nanoSaberDamageBoost;
    }

    @Override
    public ItemAttributeModifiers getAttributeModifiers(ItemStack stack) {
        double attackDamage = baseAttackDamage + (isItemActive(stack) ? additionalAttackDamage : 0.0D);
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HAND)
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HAND)
                .build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 33;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        Registry<Enchantment> registry = GTRegistries.builtinRegistry().registryOrThrow(Registries.ENCHANTMENT);
        var key = registry.getResourceKey(enchantment).get();
        return key != Enchantments.UNBREAKING &&
                key != Enchantments.MENDING &&
                enchantment.canEnchant(Items.IRON_SWORD.getDefaultInstance());
    }
}
