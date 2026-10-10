package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.capability.ModularItemStack;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GTUtil;
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
        this.defaultSlotGetter = stack -> {
            if (!stack.has(GTDataComponents.NETHERITE_PLATED)) return defaultSlots;
            List<ItemModuleSlot> platedSlots = new ArrayList<>(defaultSlots);
            platedSlots.add(GTItemModules.TIERED_SLOTS[maxTier]);
            platedSlots.add(GTItemModules.TIERED_SLOTS[maxTier]);
            return platedSlots;
        };
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(GTCapability.CAPABILITY_MODULAR_ITEM, (s, $) -> new ModularItemStack(s, defaultSlotGetter),
                item);
        for (var module : GTRegistries.ITEM_MODULES) {
            if (module instanceof CapabilityProviderItemModule<?> capProvider)
                capProvider.attachCapabilities(event, item);
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
        if (modularItem != null) {
            tooltipComponents.add(Component
                    .translatable("tooltip.gtceu.configure_modular_armor",
                            SyncedKeyMappings.MODULAR_ITEM_GUI.getKeyMapping().getKey().getDisplayName())
                    .withStyle(ChatFormatting.GRAY));
            List<ItemModuleSlot> slots = modularItem.getSlots();

            int maxTier = slots.stream()
                    .mapToInt(slot -> slot instanceof UniversalItemModuleSlot ? GTValues.MAX :
                            slot instanceof TieredItemModuleSlot tiered ? tiered.getTier() : -1)
                    .max().orElse(-1);
            if (maxTier >= 0) {
                tooltipComponents.add(Component.translatable("gui.gtceu.max_module_tier", GTValues.VNF[maxTier]));
            }

            if (stack.has(GTDataComponents.NETHERITE_PLATED)) {
                tooltipComponents.add(Component.translatable("tooltip.gtceu.netherite_plated")
                        .withStyle(ChatFormatting.GRAY));
            }

            if (!GTUtil.isShiftDown()) {
                tooltipComponents.add(Component.translatable("gtceu.tooltip.hold_shift"));
                return;
            }

            if (!slots.isEmpty()) tooltipComponents.add(Component.translatable("gui.gtceu.module_slots"));
            for (int slotI = 0; slotI < slots.size(); slotI++) {
                ItemModuleSlot slot = slots.get(slotI);
                if (slot == null) continue;
                ModuleContext moduleData = modularItem.getModuleContextForSlot(slotI);
                if (moduleData != null) {
                    tooltipComponents.add(Component.literal(" - ")
                            .append(moduleData.getModule().getDisplayName(moduleData)));

                    List<Component> moduleTooltips = new ArrayList<>();
                    moduleData.getModule().appendHoverText(moduleData, context, moduleTooltips, isAdvanced);

                    moduleTooltips.stream()
                            .map(c -> Component.literal("    ").append(c))
                            .forEach(tooltipComponents::add);
                } else {
                    tooltipComponents.add(Component.literal(" - ")
                            .append(Component.translatable("gui.gtceu.item_module.empty_module_slot")
                                    .withStyle(ChatFormatting.GRAY)));
                }
            }
        }
    }
}
