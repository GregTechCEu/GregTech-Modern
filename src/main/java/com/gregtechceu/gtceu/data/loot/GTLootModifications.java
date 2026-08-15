package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTEnchantments;
import com.gregtechceu.gtceu.common.loot.modifier.ApplyHardHammerEnchantmentModifier;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.advancements.critereon.ItemEnchantmentsPredicate.enchantments;
import static net.minecraft.advancements.critereon.ItemPredicate.Builder.item;
import static net.minecraft.world.level.storage.loot.predicates.AnyOfCondition.anyOf;
import static net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches;

public class GTLootModifications extends GlobalLootModifierProvider {

    public GTLootModifications(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, GTCEu.MOD_ID);
    }

    @Override
    protected void start() {
        // spotless:off
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        add("hard_hammer_enchant", ApplyHardHammerEnchantmentModifier.of(
                // disallow any tool enchanted with #gtceu:prevents_hammer_crushing (e.g. silk touch)
                toolMatches(item()
                        .withSubPredicate(ItemSubPredicates.ENCHANTMENTS,
                                enchantments(List.of(
                                        new EnchantmentPredicate(
                                                enchantments.getOrThrow(CustomTags.PREVENTS_HAMMER_CRUSHING),
                                                MinMaxBounds.Ints.ANY
                                        )
                                ))
                        )
                ).invert(),
                anyOf(
                        // require EITHER the hard hammer enchantment...
                        toolMatches(item()
                                .withSubPredicate(ItemSubPredicates.ENCHANTMENTS,
                                        enchantments(List.of(
                                                new EnchantmentPredicate(
                                                        enchantments.getOrThrow(GTEnchantments.HARD_HAMMER),
                                                        MinMaxBounds.Ints.atLeast(1)
                                                )
                                        ))
                                )
                        ),
                        // ...OR an actual hammer tool
                        toolMatches(item().of(CustomTags.TOOLS_HAMMER))
                )
        ));
        // spotless:on
    }
}
