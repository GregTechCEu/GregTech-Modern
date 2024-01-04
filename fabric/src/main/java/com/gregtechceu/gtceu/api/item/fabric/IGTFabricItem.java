package com.gregtechceu.gtceu.api.item.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelReader;

/**
 * Extra methods not available in {@link net.fabricmc.fabric.api.item.v1.FabricItem}.
 * Mostly stack-sensitive variants of vanilla methods for tools.
 */
public interface IGTFabricItem {
    private Item self() {
        return (Item) this;
    }

    /**
     * Gets the maximum number of items that this stack should be able to hold. This
     * is a ItemStack (and thus NBT) sensitive version of {@link Item#getMaxStackSize()}.
     *
     * @param stack The ItemStack
     * @return The maximum size this item can be stacked to
     */
    default int getMaxStackSize(ItemStack stack) {
        return self().getMaxStackSize();
    }

    /**
     * Called before a block is broken. Return true to prevent default block
     * harvesting.
     *
     * Note: In SMP, this is called on both client and server sides!
     *
     * @param itemstack The current ItemStack
     * @param pos       Block's position in world
     * @param player    The Player that is wielding the item
     * @return True to prevent harvesting, false to continue as normal
     */
    default boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return false;
    }

    /**
     * Checks whether an item can be enchanted with a certain enchantment. This
     * applies specifically to enchanting an item in the enchanting table and is
     * called when retrieving the list of possible enchantments for an item.
     * Enchantments may additionally (or exclusively) be doing their own checks in
     * {@link Enchantment#canEnchant(ItemStack)};
     * check the individual implementation for reference. By default this will check
     * if the enchantment type is valid for this item type.
     *
     * @param stack       the item stack to be enchanted
     * @param enchantment the enchantment to be applied
     * @return true if the enchantment can be applied to this item
     */
    default boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category.canEnchant(stack.getItem());
    }

    /**
     * ItemStack sensitive version of {@link Item#getEnchantmentValue()}.
     *
     * @param stack The ItemStack
     * @return the enchantment value
     */
    default int getEnchantmentValue(ItemStack stack) {
        return self().getEnchantmentValue();
    }

    /**
     * Return if this itemstack is damaged. Note only called if
     * {@link ItemStack#isDamageableItem()} is true.
     *
     * @param stack the stack
     * @return if the stack is damaged
     */
    default boolean isDamaged(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    /**
     * Return the itemDamage represented by this ItemStack. Defaults to the Damage
     * entry in the stack NBT, but can be overridden here for other sources.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    default int getDamage(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt("Damage");
    }

    /**
     * Return the maxDamage for this ItemStack. Defaults to the maxDamage field in
     * this item, but can be overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    default int getMaxDamage(ItemStack stack) {
        return self().getMaxDamage();
    }

    /**
     * Set the damage for this itemstack. Note, this method is responsible for zero
     * checking.
     *
     * @param stack  the stack
     * @param damage the new damage value
     */
    default void setDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt("Damage", Math.max(0, damage));
    }

    /**
     * Can this Item disable a shield
     *
     * @param stack    The ItemStack
     * @param shield   The shield in question
     * @param entity   The LivingEntity holding the shield
     * @param attacker The LivingEntity holding the ItemStack
     * @return True if this ItemStack can disable the shield in question.
     */
    default boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return this instanceof AxeItem;
    }

    /**
     *
     * Should this item, when held, allow sneak-clicks to pass through to the
     * underlying block?
     *
     * @param level  The level
     * @param pos    Block position in level
     * @param player The Player that is wielding the item
     */
    default boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }
}
