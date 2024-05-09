package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote IMaterialPartItem
 */
public interface IMaterialPartItem extends IItemComponent, IDurabilityBar, IAddInformation, ICustomDescriptionId {

    int getPartMaxDurability(ItemStack itemStack);

    default Material getPartMaterial(ItemStack itemStack) {
        var stats = itemStack.get(GTDataComponents.PART_STATS);
        var defaultMaterial = GTMaterials.Neutronium;
        if (stats == null) {
            return defaultMaterial;
        }
        if (stats.material == null || !stats.material.hasProperty(PropertyKey.INGOT)) {
            return defaultMaterial;
        }
        return stats.material;
    }

    default void setPartMaterial(ItemStack itemStack, @NotNull Material material) {
        if (!material.hasProperty(PropertyKey.INGOT))
            throw new IllegalArgumentException("Part material must have an Ingot!");
        itemStack.update(GTDataComponents.PART_STATS, new PartStats(GTMaterials.Neutronium, 0), stats -> stats.setMaterial(material));
    }

    default int getPartDamage(ItemStack itemStack) {
        var stats = itemStack.get(GTDataComponents.PART_STATS);
        if (stats == null) {
            return 0;
        }
        return stats.damage;
    }

    default void setPartDamage(ItemStack itemStack, int damage) {
        itemStack.update(GTDataComponents.PART_STATS, new PartStats(GTMaterials.Neutronium, 0), stats -> stats.setDamage(damage));
    }

    @Override
    default String getItemStackDisplayName(ItemStack itemStack) {
        var material = getPartMaterial(itemStack);
        return LocalizationUtils.format(material.getUnlocalizedName()) + " " + LocalizationUtils.format(itemStack.getItem().getDescriptionId());
    }

    @Override
    default void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
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
        return (maxDurability - getPartDamage(itemStack)) * 1f / maxDurability;
    }


    record PartStats(Material material, int damage) {
        public static final Codec<PartStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTCEuAPI.materialManager.codec().fieldOf("material").forGetter(PartStats::material),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("damage").forGetter(PartStats::damage)
        ).apply(instance, PartStats::new));
        public static final StreamCodec<ByteBuf, PartStats> STREAM_CODEC = StreamCodec.composite(
            GTCEuAPI.materialManager.streamCodec(), PartStats::material,
            ByteBufCodecs.VAR_INT, PartStats::damage,
            PartStats::new
        );

        public PartStats setMaterial(Material material) {
            return new PartStats(material, damage);
        }

        public PartStats setDamage(int damage) {
            return new PartStats(material, damage);
        }
    }

}
