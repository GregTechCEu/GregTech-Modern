package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.modifier.ApplyHardHammerEnchantmentModifier;
import com.gregtechceu.gtceu.data.loot.GTLootModifications;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class GTEnchantments {

    /**
     * This enchant is actually handled in {@linkplain ApplyHardHammerEnchantmentModifier} with the conditions specified
     * in {@linkplain GTLootModifications GTLootModifications line 34-54}
     */
    public static final ResourceKey<Enchantment> HARD_HAMMER = create("hard_hammer");

    // spotless:off
    public static void bootstrap(BootstrapContext<Enchantment> ctx) {
        HolderGetter<DamageType> damageTypes = ctx.lookup(Registries.DAMAGE_TYPE);
        HolderGetter<Enchantment> enchantments = ctx.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> items = ctx.lookup(Registries.ITEM);
        HolderGetter<Block> blocks = ctx.lookup(Registries.BLOCK);

        register(ctx, HARD_HAMMER,
                Enchantment.enchantment(
                                Enchantment.definition(
                                        items.getOrThrow(CustomTags.MINING_LOOT_ENCHANTABLE_EXCEPT_HAMMERS),
                                        5,
                                        1,
                                        Enchantment.constantCost(20),
                                        Enchantment.constantCost(60),
                                        8,
                                        EquipmentSlotGroup.MAINHAND
                                )
                        )
                        .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.MINING_EXCLUSIVE))
        );
    }
    // spotless:on

    private static void register(BootstrapContext<Enchantment> context,
                                 ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    private static ResourceKey<Enchantment> create(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, GTCEu.id(path));
    }
}
