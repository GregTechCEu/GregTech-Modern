package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.capability.ModularItemStack;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ICapabilityModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.common.data.GTItemModules;

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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack stack, @NotNull Capability<T> cap) {
        if (cap == GTCapability.CAPABILITY_MODULAR_ITEM)
            return GTCapability.CAPABILITY_MODULAR_ITEM.orEmpty(cap,
                    LazyOptional.of(() -> new ModularItemStack(stack, defaultSlotGetter)));
        else {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
            if (modularItem != null) {
                for (AppliedItemModule module : modularItem.getAppliedModules()) {
                    if (module.getModule() instanceof ICapabilityModule capabilityModule) {
                        LazyOptional<T> optional = capabilityModule.getCapability(module, cap);
                        if (optional.isPresent()) return optional;
                    }
                }
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(player.getItemInHand(usedHand));
        if (modularItem != null) {
            for (AppliedItemModule module : modularItem.getAppliedModules()) {
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
            for (AppliedItemModule module : modularItem.getAppliedModules()) {
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
            for (AppliedItemModule module : modularItem.getAppliedModules()) {
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
            for (AppliedItemModule module : modularItem.getAppliedModules()) {
                InteractionResult result = module.getModule().interactLivingEntity(module, player, interactionTarget,
                        usedHand);
                if (result != InteractionResult.PASS) return result;
            }
        }
        return IInteractionItem.super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem != null) {
            List<ItemModuleSlot> slots = modularItem.getSlots();
            if (!slots.isEmpty()) tooltipComponents.add(Component.translatable("metaarmor.tooltip.modifiers"));
            for (int slotI = 0; slotI < slots.size(); slotI++) {
                ItemModuleSlot slot = slots.get(slotI);
                if (slot == null) continue;
                AppliedItemModule module = modularItem.getModuleInSlot(slotI);
                if (module != null) {
                    int prevIndex = tooltipComponents.size();
                    module.appendHoverText(level, isAdvanced, tooltipComponents);
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
}
