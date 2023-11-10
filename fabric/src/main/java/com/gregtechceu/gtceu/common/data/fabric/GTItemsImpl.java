package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.lowdragmc.lowdraglib.LDLib;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote GTItemsImpl
 */
public class GTItemsImpl {
    public static <T extends ComponentItem> NonNullConsumer<T> burnTime(int burnTime) {
        return item -> FuelRegistry.INSTANCE.add(item, burnTime);
    }

    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate, Function<ItemStack, Float> property) {
        return item -> {
            if (LDLib.isClient()) {
                ItemProperties.register(item, predicate, (itemStack, c, l, i) -> property.apply(itemStack));
            }
        };
    }

    public static void registerToolTier(MaterialToolTier tier, ResourceLocation id, Collection<Tier> before, Collection<Tier> after) {
        // no-op
    }

    public static ResourceLocation getTierName(Tier tier) {
        return new ResourceLocation(tier.toString().toLowerCase(Locale.ROOT));
    }
}
