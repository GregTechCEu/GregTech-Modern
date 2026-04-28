package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.color.ItemColor;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class GTArmorItem extends Item {

    public final Material material;
    public final ArmorProperty armorProperty;
    private final ArmorType type;

    public GTArmorItem(ArmorType type, Properties properties, Material material, ArmorProperty armorProperty) {
        super(properties.humanoidArmor(armorProperty.getArmorMaterial(), type));
        this.type = type;
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

    private String getArmorDescriptionId() {
        String matSpecificKey = String.format("item.%s.%s_%s",
                material.getModid(), material.getName(), type.getName());
        if (Language.getInstance().has(matSpecificKey)) {
            return matSpecificKey;
        }
        return "item.gtceu.armor." + type.getName();
    }

    public Component getDescription() {
        return Component.translatable(getArmorDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                EquipmentClientInfo.Layer layer, boolean innerModel) {
        return armorProperty.getCustomTextureGetter().getCustomTexture(stack, entity, slot, layer);
    }
}
