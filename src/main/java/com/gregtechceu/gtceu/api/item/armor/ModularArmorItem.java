package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
public class ModularArmorItem extends ArmorComponentItem implements IComponentItem {

    public ModularArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public boolean protectsAgainstFalls() {
        return false;
    }

    public boolean protectsAgainstFire() {
        return false;
    }

    public float getHeatResistance() {
        return 1.0f;
    }

    public void appendBuiltInTraits(ItemStack stack, List<Component> tooltip) {
        List<Component> traits = new ArrayList<>();
        if (protectsAgainstFalls()) traits.add(Component.translatable("tooltip.gtceu.armor.trait.fall"));
        if (protectsAgainstFire()) traits.add(Component.translatable("tooltip.gtceu.armor.trait.fire"));
        if (stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.freeze"));
        }
        if (getHeatResistance() < 1.0f) {
            traits.add(Component.translatable("tooltip.gtceu.armor.trait.heat",
                    Math.round((1.0f - getHeatResistance()) * 100)));
        }
        if (stack.is(CustomTags.PPE_ARMOR)) traits.add(Component.translatable("tooltip.gtceu.armor.trait.hazard"));
        if (traits.isEmpty()) return;
        tooltip.add(Component.translatable("tooltip.gtceu.armor.built_in_traits"));
        for (Component trait : traits) {
            tooltip.add(Component.literal(" - ").append(trait).withStyle(ChatFormatting.GRAY));
        }
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
    public boolean isPPE(ItemStack stack) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular != null) {
            for (var ctx : modular.getAllModuleInstances()) {
                if (ctx.getModule().isPPE(ctx)) return true;
            }
        }
        return super.isPPE(stack);
    }
}
