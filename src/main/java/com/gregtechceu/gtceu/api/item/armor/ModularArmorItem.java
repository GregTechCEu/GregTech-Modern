package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true)
public class ModularArmorItem extends ArmorComponentItem implements IComponentItem {

    public ModularArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return super.getArmorTexture(stack, entity, slot, type);

        for (var module : modular.getAllModuleInstances()) {
            ResourceLocation texture = module.getModule().getArmorTexture(module, entity, slot, type);
            if (texture != null) return texture.toString();
        }
        return super.getArmorTexture(stack, entity, slot, type);
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
