package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.RotorProperty;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TurbineRotorItem extends TagPrefixItem {

    public TurbineRotorItem(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties.durability(getMaxRotorDurability(material)), tagPrefix, material);
    }

    public int getRotorPower(ItemStack stack) {
        RotorProperty property = this.material.getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : property.getPower();
    }

    public int getRotorEfficiency(ItemStack stack) {
        RotorProperty property = this.material.getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : property.getEfficiency();
    }

    public float getAttackDamage(ItemStack stack) {
        RotorProperty property = this.material.getProperty(PropertyKey.ROTOR);
        return property == null ? 0 : property.getDamage();
    }

    public static int getMaxRotorDurability(Material material) {
        RotorProperty property = material.getProperty(PropertyKey.ROTOR);
        return property == null ? 0 : 800 * (int) Math.pow(property.getDurability(), 0.65);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.gtceu.turbine_rotor.tooltip.efficiency",
                getRotorEfficiency(stack)));
        tooltipComponents.add(Component.translatable("item.gtceu.turbine_rotor.tooltip.power", getRotorPower(stack)));
    }
}
