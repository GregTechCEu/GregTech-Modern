package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote IMaterialPartItem
 */
public interface IMaterialPartItem extends IItemComponent, IDurabilityBar, IAddInformation, ICustomDescriptionId {

    int getPartMaxDurability(ItemStack itemStack);

    @Nullable
    default CompoundTag getPartStatsTag(ItemStack itemStack) {
        return itemStack.getTagElement("GT.PartStats");
    }

    default CompoundTag getOrCreatePartStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateTagElement("GT.PartStats");
    }

    default Material getPartMaterial(ItemStack itemStack) {
        var compound = getPartStatsTag(itemStack);
        var defaultMaterial = GTMaterials.Neutronium;
        if (compound == null || !compound.contains("Material", Tag.TAG_STRING)) {
            return defaultMaterial;
        }
        var materialName = compound.getString("Material");
        var material = GTMaterials.get(materialName);
        if (material == null || !material.hasProperty(PropertyKey.INGOT)) {
            return defaultMaterial;
        }
        return material;
    }

    default void setPartMaterial(ItemStack itemStack, @Nonnull Material material) {
        if (!material.hasProperty(PropertyKey.INGOT))
            throw new IllegalArgumentException("Part material must have an Ingot!");
        var compound = getOrCreatePartStatsTag(itemStack);
        compound.putString("Material", material.getName());
    }

    default int getPartDamage(ItemStack itemStack) {
        var compound = getPartStatsTag(itemStack);
        if (compound == null || !compound.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            return 0;
        }
        return compound.getInt("Damage");
    }

    default void setPartDamage(ItemStack itemStack, int damage) {
        var compound = getOrCreatePartStatsTag(itemStack);
        compound.putInt("Damage", Math.min(getPartMaxDurability(itemStack), damage));
    }

    @Override
    default String getItemStackDisplayName(ItemStack itemStack) {
        var material = getPartMaterial(itemStack);
        return LocalizationUtils.format(itemStack.getItem().getDescriptionId()) + "-" +LocalizationUtils.format(material.getUnlocalizedName());
    }

    @Override
    default void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        var material = getPartMaterial(stack);
        var maxDurability = getPartMaxDurability(stack);
        var damage = getPartDamage(stack);
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.durability", maxDurability - damage, maxDurability));
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.primary_material", material.getLocalizedName()));
    }

    @Environment(EnvType.CLIENT)
    static ItemColor getItemStackColor() {
        return (itemStack, i) -> {
            if (itemStack.getItem() instanceof ComponentItem componentItem) {
                for (IItemComponent component : componentItem.getComponents()) {
                    if (component instanceof IMaterialPartItem materialPartItem) {
                        return materialPartItem.getPartMaterial(itemStack).getMaterialARGB();
                    }
                }
            }
            return -1;
        };
    }

    @Override
    default float getDurabilityForDisplay(ItemStack itemStack) {
        var maxDurability = getPartMaxDurability(itemStack);
        return (maxDurability - getPartDamage(itemStack)) * 1f / maxDurability;
    }

}
