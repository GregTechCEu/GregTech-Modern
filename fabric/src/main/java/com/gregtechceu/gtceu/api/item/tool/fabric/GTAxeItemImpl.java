package com.gregtechceu.gtceu.api.item.tool.fabric;

import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.gregtechceu.gtceu.api.item.fabric.IGTToolImpl;
import com.gregtechceu.gtceu.api.item.tool.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class GTAxeItemImpl extends GTAxeItem implements IGTToolImpl, IGTFabricItem {
    public GTAxeItemImpl(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition toolStats, Properties properties) {
        super(toolType, tier, material, toolStats, properties);
    }

    public static GTAxeItem create(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition toolStats, Item.Properties properties) {
        return new GTAxeItemImpl(toolType, tier, material, toolStats, properties);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        return super.onBlockStartBreak(stack, pos, player);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return super.getEnchantmentValue(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        return super.getDefaultAttributeModifiers(slot, stack);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return super.canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return super.doesSneakBypassUse(stack, level, pos, player);
    }

    @Override
    public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return !super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return super.hasCraftingRemainingItem(stack);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack itemStack) {
        return hasCraftingRemainingItem(itemStack) ? super.getCraftingRemainingItem(itemStack) : ItemStack.EMPTY;
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return super.shouldCauseReequipAnimation(oldStack, newStack, ItemStack.matches(oldStack, newStack));
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return super.isDamaged(stack);
    }

    @Override
    public int getDamage(ItemStack stack) {
        return super.getDamage(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, damage);
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        return IGTToolImpl.definition$isCorrectToolForDrops(stack, state);
    }
}
