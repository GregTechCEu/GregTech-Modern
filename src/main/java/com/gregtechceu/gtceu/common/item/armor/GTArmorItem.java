package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class GTArmorItem extends ArmorItem {

    public final Material material;
    public final ArmorProperty armorProperty;

    public GTArmorItem(ArmorItem.Type type, Properties properties, Material material, ArmorProperty armorProperty) {
        super(armorProperty.getArmorMaterial(), type, properties);
        this.material = material;
        this.armorProperty = armorProperty;
        if (GTCEu.isClientSide()) {
            ArmorItemRenderer.create(this, type);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Override
    public String getDescriptionId() {
        String matSpecificKey = String.format("item.%s.%s_%s",
                material.getModid(), material.getName(), type.getName());
        if (Language.getInstance().has(matSpecificKey)) {
            return matSpecificKey;
        }
        return "item.gtceu.armor." + type.getName();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(getDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      ArmorMaterial.Layer layer, boolean innerModel) {
        return armorProperty.getCustomTextureGetter().getCustomTexture(stack, entity, slot, layer);
    }
}
