package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        if (material.isNull() || !material.hasProperty(PropertyKey.INGOT)) {
            return defaultMaterial;
        }
        return material;
    }

    default void setPartMaterial(ItemStack itemStack, @NotNull Material material) {
        if (!material.hasProperty(PropertyKey.INGOT))
            throw new IllegalArgumentException("Part material must have an Ingot!");
        var compound = getOrCreatePartStatsTag(itemStack);
        compound.putString("Material", material.getResourceLocation().toString());
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
    @Nullable
    default Component getItemName(ItemStack stack) {
        var material = getPartMaterial(stack);
        return Component.translatable(stack.getDescriptionId(), material.getLocalizedName());
    }

    @Override
    default void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                                 List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        var material = getPartMaterial(stack);
        var maxDurability = getPartMaxDurability(stack);
        var damage = getPartDamage(stack);
        tooltipComponents
                .add(Component.translatable("metaitem.tool.tooltip.durability", maxDurability - damage, maxDurability));
        tooltipComponents
                .add(Component.translatable("metaitem.tool.tooltip.primary_material", material.getLocalizedName()));
    }

    @OnlyIn(Dist.CLIENT)
    static ItemColor getItemStackColor() {
        return (itemStack, i) -> {
            if (itemStack.getItem() instanceof IComponentItem componentItem) {
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
        return (float) (maxDurability - getPartDamage(itemStack)) / maxDurability;
    }

    @Override
    default int getMaxDurability(ItemStack stack) {
        return getPartMaxDurability(stack);
    }
}
