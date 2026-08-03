package com.gregtechceu.gtceu.common.loot.modifier;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.loot.GTGlobalLootModifiers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;

public class ApplyHardHammerEnchantmentModifier extends LootModifier {
    // spotless:off
    public static final MapCodec<ApplyHardHammerEnchantmentModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
            .apply(instance, ApplyHardHammerEnchantmentModifier::new));
    // spotless:on

    public static ApplyHardHammerEnchantmentModifier of(LootItemCondition.Builder... conditions) {
        LootItemCondition[] built = Arrays.stream(conditions)
                .map(LootItemCondition.Builder::build)
                .toArray(LootItemCondition[]::new);
        return new ApplyHardHammerEnchantmentModifier(built);
    }

    public ApplyHardHammerEnchantmentModifier(LootItemCondition... conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ToolHelper.applyHammerDropConversion(generatedLoot, context);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return GTGlobalLootModifiers.APPLY_HARD_HAMMER_ENCHANT.get();
    }
}
