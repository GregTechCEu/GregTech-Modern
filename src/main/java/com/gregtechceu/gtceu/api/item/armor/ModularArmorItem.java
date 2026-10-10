package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true)
public class ModularArmorItem extends ArmorComponentItem implements IComponentItem {

    public ModularArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        return stack.has(GTDataComponents.NETHERITE_PLATED) ?
                Component.translatable("item.gtceu.netherite_plated", name) : name;
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      ArmorMaterial.Layer layer, boolean innerModel) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return super.getArmorTexture(stack, entity, slot, layer, innerModel);

        for (var module : modular.getAllModuleInstances()) {
            ResourceLocation texture = module.getModule().getArmorTexture(module, entity, slot, layer, innerModel);
            if (texture != null) return texture;
        }
        return super.getArmorTexture(stack, entity, slot, layer, innerModel);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        for (IItemComponent component : components) {
            if (component instanceof IItemLifeCycle lifeCycle) {
                lifeCycle.inventoryTick(stack, level, entity, slotId, isSelected);
            }
        }
    }
}
