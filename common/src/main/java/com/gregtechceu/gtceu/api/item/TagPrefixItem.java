package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote MaterialItem
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TagPrefixItem extends Item implements IItemRendererProvider {
    public final TagPrefix tagPrefix;
    public final Material material;

    protected TagPrefixItem(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties);
        this.tagPrefix = tagPrefix;
        this.material = material;
        TagPrefixItemRenderer.getOrCreate(tagPrefix.materialIconType(), material.getMaterialIconSet());
    }

    @ExpectPlatform
    public static TagPrefixItem create(Properties properties, TagPrefix tagPrefix, Material material) {
        throw new AssertionError();
    }

    public void onRegister() {

    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (index == 0) {
                if (itemStack.getItem() instanceof TagPrefixItem tagPrefixItem) {
                    return tagPrefixItem.material.getMaterialARGB();
                }
            }
            return -1;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (this.tagPrefix.tooltip() != null) {
            this.tagPrefix.tooltip().accept(material, tooltipComponents);
        }
    }

    @Override
    public String getDescriptionId() {
        return tagPrefix.getUnlocalizedName(material);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return tagPrefix.getUnlocalizedName(material);
    }

    @Override
    public Component getDescription() {
        return tagPrefix.getLocalizedName(material);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.tickCount % 20 == 0) {

                if (tagPrefix != TagPrefix.ingotHot || !material.hasProperty(PropertyKey.BLAST)) return;

                float heatDamage = ((material.getBlastTemperature() - 1750) / 1000.0F) + 2;
                ItemStack armor = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                // TODO armor
//                if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
//                    heatDamage *= ((ArmorMetaItem<?>) armor.getItem()).getItem(armor).getArmorLogic().getHeatResistance();
//                }
                if (heatDamage > 0.0) {
                    livingEntity.hurt(GTDamageTypes.HEAT.source(level), heatDamage);
                } else if (heatDamage < 0.0) {
                    livingEntity.hurt(GTDamageTypes.FROST.source(level), -heatDamage);
                }
            }
        }
    }

    public int getItemBurnTime() {
        DustProperty property = material == null ? null : material.getProperty(PropertyKey.DUST);
        if (property != null) return (int) (property.getBurnTime() * tagPrefix.getMaterialAmount(material) / GTValues.M);
        return -1;
    }

    // TODO BEACON PAYMENT

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return TagPrefixItemRenderer.getOrCreate(tagPrefix.materialIconType(), material.getMaterialIconSet());
    }
}
