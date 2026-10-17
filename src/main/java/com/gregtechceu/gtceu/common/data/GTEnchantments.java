package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.modifier.ApplyHardHammerEnchantmentModifier;
import com.gregtechceu.gtceu.data.loot.GTLootModifications;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;

import static net.minecraft.advancements.critereon.DamageSourcePredicate.Builder.damageType;
import static net.minecraft.advancements.critereon.EntityPredicate.Builder.entity;
import static net.minecraft.world.item.enchantment.Enchantment.*;
import static net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition.hasDamageSource;
import static net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition.hasProperties;

public class GTEnchantments {

    /**
     * This enchant is actually handled in {@linkplain ApplyHardHammerEnchantmentModifier} with the conditions specified
     * in {@linkplain GTLootModifications GTLootModifications line 34-54}
     */
    public static final ResourceKey<Enchantment> HARD_HAMMER = create("hard_hammer");
    public static final ResourceKey<Enchantment> DISJUNCTION = create("disjunction");

    // spotless:off
    public static void bootstrap(BootstrapContext<Enchantment> ctx) {
        HolderGetter<DamageType> damageTypes = ctx.lookup(Registries.DAMAGE_TYPE);
        HolderGetter<Enchantment> enchantments = ctx.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> items = ctx.lookup(Registries.ITEM);
        HolderGetter<Block> blocks = ctx.lookup(Registries.BLOCK);

        register(ctx, HARD_HAMMER, enchantment(
                        definition(
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
        register(ctx, DISJUNCTION, enchantment(
                        definition(
                                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                5,
                                5,
                                Enchantment.dynamicCost(5, 8),
                                Enchantment.dynamicCost(25, 8),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.DAMAGE,
                        new AddValue(LevelBasedValue.perLevel(2.5F)),
                        hasProperties(LootContext.EntityTarget.THIS, entity().entityType(EntityTypePredicate.of(CustomTags.SENSITIVE_TO_DISJUNCTION)))
                )
                .withEffect(EnchantmentEffectComponents.POST_ATTACK,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        // Weakness causes Endermen to not be able to teleport with GT being installed.
                        makeDisjunctionEffect(MobEffects.WEAKNESS),
                        hasProperties(LootContext.EntityTarget.THIS, entity().entityType(EntityTypePredicate.of(CustomTags.SENSITIVE_TO_DISJUNCTION)))
                                .and(hasDamageSource(damageType().isDirect(true)))
                )
                .withEffect(EnchantmentEffectComponents.POST_ATTACK,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        // They also get Poisoned. If you have this Enchant on an Arrow, you can kill the Ender Dragon easier.
                        //   ^ the above is a lie, they get slowness instead circa 2021.
                        makeDisjunctionEffect(MobEffects.MOVEMENT_SLOWDOWN),
                        hasProperties(LootContext.EntityTarget.THIS, entity().entityType(EntityTypePredicate.of(CustomTags.SENSITIVE_TO_DISJUNCTION)))
                                .and(hasDamageSource(damageType().isDirect(true)))
                )
        );
    }

    private static ApplyMobEffect makeDisjunctionEffect(Holder<MobEffect> effect) {
        return new ApplyMobEffect(
                HolderSet.direct(effect),
                LevelBasedValue.perLevel(10),
                LevelBasedValue.perLevel(10),
                new LevelBasedValue.Clamped(
                        new LevelBasedValue.Fraction(
                                LevelBasedValue.perLevel(5),
                                LevelBasedValue.constant(7)
                        ),
                        1, 255
                ),
                new LevelBasedValue.Clamped(
                        new LevelBasedValue.Fraction(
                                LevelBasedValue.perLevel(5),
                                LevelBasedValue.constant(7)
                        ),
                        1, 255
                )
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
