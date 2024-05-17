package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class ArmorLogicSuite implements IArmorLogic, IItemHUDProvider {

    protected final int energyPerUse;
    protected final int tier;
    protected final long maxCapacity;
    protected final ArmorItem.Type type;

    protected ArmorLogicSuite(int energyPerUse, long maxCapacity, int tier, ArmorItem.Type type) {
        this.energyPerUse = energyPerUse;
        this.maxCapacity = maxCapacity;
        this.tier = tier;
        this.type = type;
    }

    @Override
    public abstract void onArmorTick(Level Level, Player player, ItemStack itemStack);

    @Override
    public int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(armor);
        if (item == null) return 0;
        if (item.getCharge() >= energyPerUse) {
            return (int) Math.round(20.0F * this.getAbsorption() * this.getDamageAbsorption());
        } else {
            return (int) Math.round(4.0F * this.getAbsorption() * this.getDamageAbsorption());
        }
    }

    @Override
    public List<ItemAttributeModifiers.Entry> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != this.type.getSlot()) return Collections.emptyList();
        IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
        UUID uuid = IArmorLogic.ARMOR_MODIFIER_UUID_PER_TYPE.get(type);
        if (item == null) return Collections.emptyList();
        if (item.getCharge() >= energyPerUse) {
            return ItemAttributeModifiers.builder().add(
                    Attributes.ARMOR,
                    new AttributeModifier(uuid, "Armor modifier",
                            20.0F * this.getAbsorption() * this.getDamageAbsorption(),
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.bySlot(slot)).build().modifiers();
        } else {
            return ItemAttributeModifiers.builder().add(
                    Attributes.ARMOR,
                    new AttributeModifier(uuid, "Armor modifier",
                            4.0F * this.getAbsorption() * this.getDamageAbsorption(),
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.bySlot(slot)).build().modifiers();
        }
    }

    @Override
    public void addToolComponents(ArmorComponentItem mvi) {
        mvi.attachComponents(new ElectricStats(maxCapacity, tier, true, false) {

            @Override
            public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                          InteractionHand usedHand) {
                return onRightClick(level, player, usedHand);
            }

            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                        TooltipFlag isAdvanced) {
                addInfo(stack, tooltipComponents);
            }
        });
    }

    public void addInfo(ItemStack itemStack, List<Component> lines) {}

    public InteractionResultHolder<ItemStack> onRightClick(Level Level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public ArmorItem.Type getArmorType() {
        return type;
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        return null;
    }

    public double getDamageAbsorption() {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    protected static void addCapacityHUD(ItemStack stack, ArmorUtils.ModularHUD hud) {
        IElectricItem cont = GTCapabilityHelper.getElectricItem(stack);
        if (cont == null) return;
        if (cont.getCharge() == 0) return;
        float energyMultiplier = cont.getCharge() * 100.0F / cont.getMaxCharge();
        hud.newString(
                Component.translatable("metaarmor.hud.energy_lvl", String.format("%.1f", energyMultiplier) + "%"));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldDrawHUD() {
        return this.type == ArmorItem.Type.CHESTPLATE;
    }

    public int getEnergyPerUse() {
        return this.energyPerUse;
    }

    protected float getAbsorption() {
        return switch (this.getArmorType()) {
            case HELMET, BOOTS -> 0.15F;
            case CHESTPLATE -> 0.4F;
            case LEGGINGS -> 0.3F;
            case BODY -> 0.0F;
        };
    }
}
