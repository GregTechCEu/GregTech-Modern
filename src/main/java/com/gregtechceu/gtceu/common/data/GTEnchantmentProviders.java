package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.SingleEnchantment;

public class GTEnchantmentProviders {

    // spotless:off
    public static final ResourceKey<EnchantmentProvider> SILK_TOUCH = ResourceKey.create(Registries.ENCHANTMENT_PROVIDER, GTCEu.id("silk_touch"));

    public static void bootstrap(BootstrapContext<EnchantmentProvider> context) {
        HolderGetter<Enchantment> holdergetter = context.lookup(Registries.ENCHANTMENT);
        context.register(SILK_TOUCH, new SingleEnchantment(holdergetter.getOrThrow(Enchantments.SILK_TOUCH), ConstantInt.of(1)));
    }
    // spotless:on
}
