package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.*;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import com.google.common.collect.Multimap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentItem extends Item
                           implements HeldItemUIFactory.IHeldItemUIHolder, IItemRendererProvider, IComponentItem {

    protected int burnTime = -1;

    @Getter
    protected List<IItemComponent> components;

    protected ComponentItem(Properties properties) {
        super(properties);
        components = new ArrayList<>();
    }

    public static ComponentItem create(Item.Properties properties) {
        return new ComponentItem(properties);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        for (IItemComponent component : components) {
            if (component instanceof IAddInformation addInformation) {
                addInformation.appendHoverText(stack, level, tooltipComponents, isAdvanced);
            }
        }
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IItemAttributes itemAttributes) {
                var result = itemAttributes.getAttributeModifiers(slot, stack);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IEnchantableItem enchantableItem) {
                return enchantableItem.isEnchantable(stack);
            }
        }
        return super.isEnchantable(stack);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IEnchantableItem enchantableItem) {
                return enchantableItem.getEnchantmentValue(stack);
            }
        }
        return super.getEnchantmentValue(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        for (IItemComponent component : components) {
            if (component instanceof IEnchantableItem enchantableItem) {
                return enchantableItem.canApplyAtEnchantingTable(stack, enchantment);
            }
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.use(this, level, player, usedHand);
                if (result.getResult() != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.use(level, player, usedHand);
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
    public UseAnim getUseAnimation(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                return interactionItem.getUseAnimation(stack);
            }
        }
        return super.getUseAnimation(stack);
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
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                // this will cancel the left click animation
                return interactionItem.onEntitySwing(stack, entity);
            }
        }
        // normal behavior
        return super.onEntitySwing(stack, entity);
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
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = false;
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                result |= interactionItem.hurtEnemy(stack, target, attacker);
            }
        }
        return result;
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

    @Override
    public String getDescriptionId(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                String langId = customDescriptionId.getItemDescriptionId(stack);
                if (langId != null) {
                    return langId;
                }
            }
        }
        return super.getDescriptionId(stack);
    }

    @Override
    @Nullable
    public ModularUI createUI(Player entityPlayer, HeldItemUIFactory.HeldItemHolder holder) {
        for (IItemComponent component : components) {
            if (component instanceof IItemUIFactory uiFactory) {
                return uiFactory.createUI(holder, entityPlayer);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomRenderer customRenderer) {
                return customRenderer.getRenderer();
            }
        }
        return null;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        for (IItemComponent component : components) {
            if (component instanceof IItemLifeCycle lifeCycle) {
                lifeCycle.inventoryTick(stack, level, entity, slotId, isSelected);
            }
        }
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return super.getCraftingRemainingItem(itemStack);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(stack) != ItemStack.EMPTY;
            }
        }
        return super.hasCraftingRemainingItem(stack);
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

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime;
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.getFoodProperties(stack, entity);
            }
        }
        return super.getFoodProperties(stack, entity);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public @Nullable FoodProperties getFoodProperties() {
        // If item has `foodProperties` from super, return it.
        if (super.isEdible()) return super.getFoodProperties();
        // If item has `IEdibleItem` components, return food stats from default stack
        if (isEdible()) return getFoodProperties(this.getDefaultInstance(), null);
        // Not edible, so null.
        return null;
    }

    @Override
    public boolean isEdible() {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.isEdible();
            }
        }
        return super.isEdible();
    }

    @Override
    public SoundEvent getEatingSound() {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.getEatingSound();
            }
        }
        return super.getEatingSound();
    }

    @Override
    public SoundEvent getDrinkingSound() {
        for (IItemComponent component : components) {
            if (component instanceof IEdibleItem foodBehavior) {
                return foodBehavior.getDrinkingSound();
            }
        }
        return super.getDrinkingSound();
    }

    public void burnTime(int burnTime) {
        this.burnTime = burnTime;
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
