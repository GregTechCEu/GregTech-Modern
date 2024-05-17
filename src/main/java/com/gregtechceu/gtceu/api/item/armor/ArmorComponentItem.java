package com.gregtechceu.gtceu.api.item.armor;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import lombok.Getter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ArmorComponentItem extends ArmorItem implements IComponentItem {
    @Getter
    private IArmorLogic armorLogic = new DummyArmorLogic();
    @Getter
    protected List<IItemComponent> components;

    public ArmorComponentItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties.durability(0));
        components = new ArrayList<>();
    }

    public ArmorComponentItem setArmorLogic(IArmorLogic armorLogic) {
        Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
        this.armorLogic = armorLogic;
        this.armorLogic.addToolComponents(this);
        return this;
    }

    public void attachComponents(IItemComponent... components) {
        this.components.addAll(Arrays.asList(components));
        for (IItemComponent component : components) {
            component.onAttached(this);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create();
        IArmorLogic armorLogic = getArmorLogic();
        multimap.putAll(armorLogic.getAttributeModifiers(slot, stack));
        multimap.putAll(super.getAttributeModifiers(slot, stack));
        return multimap;
    }

    @Override
    public ArmorItem.Type getType() {
        return armorLogic.getArmorType();
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return armorLogic.getArmorType().getSlot();
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);
        this.armorLogic.onArmorTick(level, player, stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 50;
    }

    public int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot) {
        return armorLogic.getArmorDisplay(player, armor, slot);
    }

    public void damageArmor(LivingEntity entity, @NotNull ItemStack stack, DamageSource source, int damage, EquipmentSlot slot) {
        armorLogic.damageArmor(entity, stack, source, damage, slot);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return armorLogic.getArmorModel(livingEntity, itemStack, equipmentSlot, original);
            }
        });
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return armorLogic.getArmorTexture(stack, entity, slot, type).toString();
    }

    ///////////////////////////////////////////
    /////   ALL component item things   ///////
    ///////////////////////////////////////////

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
    public <T> LazyOptional<T> getCapability(@NotNull final ItemStack itemStack, @NotNull final Capability<T> cap) {
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
}
