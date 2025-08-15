package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTArmorItem extends ArmorItem {

    public final Material material;
    public final ArmorProperty armorProperty;

    public GTArmorItem(ArmorMaterial armorMaterial, ArmorItem.Type type, Properties properties,
                       Material material, ArmorProperty armorProperty) {
        super(armorMaterial, type, properties);
        this.material = material;
        this.armorProperty = armorProperty;
        if (Platform.isClient()) {
            ArmorItemRenderer.create(this, type);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            // TODO utilize 2nd material color
            if (index == 0 && itemStack.getItem() instanceof GTArmorItem item) {
                Material material = item.material;
                return material.getLayerARGB(index);
            }
            return -1;
        };
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.gtceu.armor." + type.getName();
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable(getDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ResourceLocation id = armorProperty.getCustomTextureGetter()
                .getCustomTexture(stack, entity, slot, Objects.equals(type, "overlay"));
        if (id != null) {
            return id.toString();
        }
        return null;
    }
}
