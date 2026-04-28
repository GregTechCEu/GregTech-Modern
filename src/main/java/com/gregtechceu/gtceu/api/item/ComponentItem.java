package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.factory.IGTHeldItemUI;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.*;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.ItemAbility;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ComponentItem extends Item implements IGTHeldItemUI, IComponentItem {

    @Getter
    protected List<IItemComponent> components;

    public ComponentItem(Properties properties) {
        super(properties);
        components = new ArrayList<>();
    }

    public void attachComponents(IItemComponent component) {
        this.components.add(component);
        component.onAttached(this);
    }

    public void attachComponents(IItemComponent... components) {
        this.components.addAll(Arrays.asList(components));
        for (IItemComponent component : components) {
            component.onAttached(this);
        }
    }

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        boolean found = false;
        for (IItemComponent component : components) {
            if (component instanceof ISubItemHandler subItemHandler) {
                subItemHandler.fillItemCategory(this, category, items);
                found = true;
            }
        }
        if (found) return;
        items.add(new ItemStack(this));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        List<Component> componentTooltips = new ArrayList<>();
        for (IItemComponent component : components) {
            if (component instanceof IAddInformation addInformation) {
                addInformation.appendHoverText(stack, context, componentTooltips, isAdvanced);
            }
        }
        componentTooltips.forEach(tooltipComponents);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.isBarVisible(stack);
            }
        }
        return super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.getBarWidth(stack);
            }
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.getBarColor(stack);
            }
        }
        return super.getBarColor(stack);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IItemAttributes itemAttributes) {
                var result = itemAttributes.getDefaultAttributeModifiers(stack);
                if (result != null && !result.modifiers().isEmpty()) {
                    return result;
                }
            }
        }
        return super.getDefaultAttributeModifiers(stack);
    }

    public boolean isEnchantable(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IEnchantableItem enchantableItem && enchantableItem.isEnchantable(stack)) {
                return true;
            }
        }
        return false;
    }

    public int getEnchantmentValue(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IEnchantableItem enchantableItem) {
                return enchantableItem.getEnchantmentValue(stack);
            }
        }
        return 0;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        for (IItemComponent component : components) {
            if (component instanceof IEnchantableItem enchantableItem &&
                    enchantableItem.supportsEnchantment(stack, enchantment)) {
                return true;
            }
        }
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility action) {
        for (IItemComponent component : components) {
            if (stack instanceof ItemStack itemStack &&
                    component instanceof IAbilityItem abilityItem && abilityItem.canPerformAction(itemStack, action)) {
                return true;
            }
        }
        return super.canPerformAction(stack, action);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.useOn(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.use(player.getItemInHand(usedHand), level, player, usedHand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.onItemUseFirst(itemStack, context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                stack = interactionItem.finishUsingItem(stack, level, livingEntity);
            }
        }
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                return interactionItem.getUseAnimation(stack);
            }
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                // this will cancel the left click animation
                return interactionItem.onEntitySwing(stack, entity, hand);
            }
        }
        // normal behavior
        return super.onEntitySwing(stack, entity, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                  InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.interactLivingEntity(stack, player, interactionTarget, usedHand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                interactionItem.hurtEnemy(stack, target, attacker);
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                Component name = customDescriptionId.getItemName(stack);
                if (name != null) {
                    return name;
                }
            }
        }
        return super.getName(stack);
    }

    public String getDescriptionId(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                String langId = customDescriptionId.getItemDescriptionId(stack);
                if (langId != null) {
                    return langId;
                }
            }
        }
        return super.getDescriptionId();
    }

    @Override
    @Nullable
    public ModularUI createUI(Player entityPlayer, GTHeldItemUIHolder holder) {
        for (IItemComponent component : components) {
            if (component instanceof IItemUIFactory uiFactory) {
                Object ui = uiFactory.createUI(holder, entityPlayer);
                return ui instanceof ModularUI modularUI ? modularUI : null;
            }
        }
        return null;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity,
                              @Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        for (IItemComponent component : components) {
            if (component instanceof IItemLifeCycle lifeCycle) {
                lifeCycle.inventoryTick(stack, level, entity, slot == null ? -1 : slot.ordinal(),
                        slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(stack) != ItemStack.EMPTY;
            }
        }
        return false;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        boolean result = false;
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                result |= interactionItem.sneakBypassUse(stack, level, pos, player);
            }
        }
        return result;
    }

    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.getFoodProperties(stack, entity);
            }
        }
        return stack.get(DataComponents.FOOD);
    }

    public SoundEvent getEatingSound() {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.getEatingSound();
            }
        }
        return SoundEvents.GENERIC_EAT.value();
    }

    public SoundEvent getDrinkingSound() {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.getDrinkingSound();
            }
        }
        return SoundEvents.GENERIC_DRINK.value();
    }

    /**
     * Attempts to get a fully charged variant of this electric item
     *
     * @param chargeAmount amount of charge
     * @return charged electric item stack
     * @throws java.lang.IllegalStateException if this item is not electric item
     */
    public ItemStack getChargedStack(long chargeAmount) {
        ItemStack itemStack = getDefaultInstance();
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem == null) {
            throw new IllegalStateException("Not an electric item.");
        }
        electricItem.charge(chargeAmount, Integer.MAX_VALUE, true, false);
        return itemStack;
    }

    public ItemStack getInfiniteChargedStack() {
        ItemStack itemStack = getDefaultInstance();
        IElectricItem iElectricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (!(iElectricItem instanceof ElectricItem electricItem)) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        electricItem.setInfiniteCharge(true);
        return itemStack;
    }
}
