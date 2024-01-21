package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ComponentItem
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentItem extends Item implements HeldItemUIFactory.IHeldItemUIHolder, IItemRendererProvider, IItemUseFirst {

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
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
        return super.finishUsingItem(stack, level, livingEntity);
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
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
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
    public String getDescriptionId(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                return customDescriptionId.getItemStackDisplayName(stack);
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

    public <T> LazyOptional<T> getCapability(@Nonnull final ItemStack itemStack, @Nonnull final Capability<T> cap) {
        for (IItemComponent component : components) {
            if (component instanceof IComponentCapability componentCapability) {
                var value = componentCapability.getCapability(itemStack, cap);
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime;
    }

    public void burnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    /**
     * Attempts to get an fully charged variant of this electric item
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
}
