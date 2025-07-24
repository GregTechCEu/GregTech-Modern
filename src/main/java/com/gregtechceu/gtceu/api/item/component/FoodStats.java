package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Simple {@link IEdibleItem} implementation.
 */
public class FoodStats implements IEdibleItem, IInteractionItem, IAddInformation {

    protected final FoodProperties properties;

    protected final boolean isDrink;
    @Nullable
    protected final Supplier<ItemStack> containerItem;

    public FoodStats(FoodProperties properties, boolean isDrink, boolean hasPotionEffects,
                     @Nullable Supplier<ItemStack> containerItem) {
        this.properties = properties;
        this.isDrink = isDrink;
        this.containerItem = containerItem;
    }

    public FoodStats(FoodProperties properties, boolean isDrink, @Nullable Supplier<ItemStack> containerItem) {
        this.properties = properties;
        this.isDrink = isDrink;
        this.containerItem = containerItem;
    }

    public FoodStats(FoodProperties properties) {
        this.properties = properties;
        this.isDrink = false;
        this.containerItem = null;
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return properties;
    }

    @Override
    public boolean isEdible() {
        return true;
    }

    @Override
    public SoundEvent getEatingSound() {
        return isDrink ? getDrinkingSound() : IEdibleItem.super.getEatingSound();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isDrink ? UseAnim.DRINK : UseAnim.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        GTUtil.addPotionTooltip(properties.getEffects(), tooltipComponents);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack food, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;
        var stack = livingEntity.eat(level, food);
        if (containerItem != null && (player == null || !player.getAbilities().instabuild)) {
            var container = containerItem.get();
            if (stack.isEmpty()) {
                return container;
            }

            if (player != null) {
                if (!player.getInventory().add(container)) {
                    player.drop(container, true);
                }
            }
        }
        return stack;
    }
}
