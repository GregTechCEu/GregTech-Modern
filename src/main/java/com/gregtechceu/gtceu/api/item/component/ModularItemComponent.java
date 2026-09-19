package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.capability.ModularItemStack;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModularItemComponent implements IItemComponent, IComponentCapability, IInteractionItem, IAddInformation {

    private final Function<ItemStack, List<ItemModuleSlot>> defaultSlotGetter;

    public ModularItemComponent(Function<ItemStack, List<ItemModuleSlot>> defaultSlotGetter) {
        this.defaultSlotGetter = defaultSlotGetter;
    }

    public ModularItemComponent(int slots, int maxTier) {
        List<ItemModuleSlot> defaultSlots = new ArrayList<>();
        for (int i = 0; i < slots; i++) defaultSlots.add(GTItemModules.TIERED_SLOTS[maxTier]);
        this.defaultSlotGetter = stack -> defaultSlots;
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(GTCapability.CAPABILITY_MODULAR_ITEM, (s, $) -> new ModularItemStack(s, defaultSlotGetter),
                item);
        for (var module : GTRegistries.ITEM_MODULES) {
            module.attachCapabilities(event, item);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(player.getItemInHand(usedHand));
        if (modularItem != null) {
            for (ModuleContext module : modularItem.getAllModuleInstances()) {
                InteractionResultHolder<ItemStack> result = module.getModule().use(module, level, player, usedHand);
                if (result.getResult() != InteractionResult.PASS) return result;
            }
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(context.getItemInHand());
        if (modularItem != null) {
            for (ModuleContext module : modularItem.getAllModuleInstances()) {
                InteractionResult result = module.getModule().useOn(module, context);
                if (result != InteractionResult.PASS) return result;
            }
        }
        return IInteractionItem.super.useOn(context);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(itemStack);
        if (modularItem != null) {
            for (ModuleContext module : modularItem.getAllModuleInstances()) {
                InteractionResult result = module.getModule().onItemUseFirst(module, context);
                if (result != InteractionResult.PASS) return result;
            }
        }
        return IInteractionItem.super.onItemUseFirst(itemStack, context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                  InteractionHand usedHand) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem != null) {
            for (ModuleContext module : modularItem.getAllModuleInstances()) {
                InteractionResult result = module.getModule().interactLivingEntity(module, player, interactionTarget,
                        usedHand);
                if (result != InteractionResult.PASS) return result;
            }
        }
        return IInteractionItem.super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return;

        tooltipComponents.add(Component
                .translatable("tooltip.gtceu.configure_modular_armor",
                        SyncedKeyMappings.MODULAR_ITEM_GUI.getKeyMapping().getKey().getDisplayName())
                .withStyle(ChatFormatting.GRAY));
        List<ItemModuleSlot> slots = modularItem.getSlots();
        if (!slots.isEmpty()) tooltipComponents.add(Component.translatable("metaarmor.tooltip.modifiers"));
        for (int slotI = 0; slotI < slots.size(); slotI++) {
            ItemModuleSlot slot = slots.get(slotI);
            if (slot == null) continue;
            ModuleContext moduleData = modularItem.getModuleContextForSlot(slotI);
            if (moduleData != null) {
                int prevIndex = tooltipComponents.size();
                moduleData.getModule().appendHoverText(moduleData, context, isAdvanced, tooltipComponents);
                if (tooltipComponents.size() > prevIndex) {
                    tooltipComponents.set(prevIndex, Component.translatable(
                            "metaarmor.tooltip.modifier",
                            slot.getDisplayName(),
                            tooltipComponents.get(prevIndex)));
                    for (int i = prevIndex + 1; i < tooltipComponents.size(); i++) {
                        tooltipComponents.set(i, Component.literal("    ").append(tooltipComponents.get(i)));
                    }
                }
            } else {
                tooltipComponents.add(Component.translatable(
                        "metaarmor.tooltip.modifier",
                        slot.getDisplayName(),
                        Component.translatable("metaarmor.tooltip.modifier.empty").withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
